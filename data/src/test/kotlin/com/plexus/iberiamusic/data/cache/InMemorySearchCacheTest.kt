package com.plexus.iberiamusic.data.cache

import com.plexus.iberiamusic.data.time.FakeTimeProvider
import com.plexus.iberiamusic.data.time.TimeProvider
import com.plexus.iberiamusic.domain.model.Track
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemorySearchCacheTest {

  private val clock = FakeTimeProvider()
  private var mockNow = 0L
  private val mockClock = mockk<TimeProvider> { every { nowMillis() } answers { mockNow } }
  private val cache = InMemorySearchCache(timeProvider = clock)
  private val tracks = listOf(Track(id = 1L, name = "Song", artistName = "Artist"))

  @Test
  fun `miss returns null for an unknown query`() {
    assertNull(cache.get("queen"))
  }

  @Test
  fun `hit returns the stored tracks`() {
    cache.put("queen", tracks)

    assertEquals(tracks, cache.get("queen"))
  }

  @Test
  fun `entry is still valid just before the TTL elapses`() {
    cache.put("queen", tracks)

    clock.advanceBy(InMemorySearchCache.DEFAULT_TTL_MILLIS - 1)

    assertEquals(tracks, cache.get("queen"))
  }

  @Test
  fun `entry expires once the TTL elapses`() {
    cache.put("queen", tracks)

    clock.advanceBy(InMemorySearchCache.DEFAULT_TTL_MILLIS)

    assertNull(cache.get("queen"))
  }

  @Test
  fun `default TTL is three minutes`() {
    assertEquals(3 * 60 * 1000L, InMemorySearchCache.DEFAULT_TTL_MILLIS)
  }

  @Test
  fun `putting again after expiry stores fresh data with a new TTL`() {
    cache.put("queen", tracks)
    clock.advanceBy(InMemorySearchCache.DEFAULT_TTL_MILLIS)
    val fresh = listOf(Track(id = 2L, name = "Other", artistName = "Artist"))

    cache.put("queen", fresh)
    clock.advanceBy(InMemorySearchCache.DEFAULT_TTL_MILLIS - 1)

    assertEquals(fresh, cache.get("queen"))
  }

  @Test
  fun `entry is not served when the clock goes backwards`() {
    val backwards = InMemorySearchCache(timeProvider = mockClock)
    mockNow += 10_000
    backwards.put("queen", tracks)

    mockNow -= 5_000

    assertNull(backwards.get("queen"))
  }

  @Test
  fun `evicts the least recently used entry beyond the maximum`() {
    val small = InMemorySearchCache(timeProvider = mockClock, maxEntries = 2)
    small.put("a", tracks)
    small.put("b", tracks)
    small.get("a") // "b" is now the least recently used

    small.put("c", tracks)

    assertEquals(tracks, small.get("a"))
    assertNull(small.get("b"))
    assertEquals(tracks, small.get("c"))
  }

  @Test
  fun `never holds more entries than the maximum`() {
    val small = InMemorySearchCache(timeProvider = mockClock, maxEntries = 3)

    (1..10).forEach { small.put("q$it", tracks) }

    assertEquals(3, (1..10).count { small.get("q$it") != null })
  }

  @Test
  fun `writing purges expired entries that were never read again`() {
    val small = InMemorySearchCache(timeProvider = mockClock, maxEntries = 2)
    small.put("expiring", tracks)
    mockNow += 100_000
    small.put("live", tracks)
    small.get("expiring") // now "live" is the least recently used
    mockNow += InMemorySearchCache.DEFAULT_TTL_MILLIS - 100_000

    // "expiring" is expired but never read again; the purge frees its slot so "live" survives.
    small.put("new", tracks)

    assertEquals(tracks, small.get("live"))
    assertEquals(tracks, small.get("new"))
  }

  @Test
  fun `queries are cached independently`() {
    cache.put("queen", tracks)

    assertNull(cache.get("bowie"))
  }
}
