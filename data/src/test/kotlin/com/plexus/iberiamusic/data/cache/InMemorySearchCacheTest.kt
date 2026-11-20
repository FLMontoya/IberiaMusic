package com.plexus.iberiamusic.data.cache

import com.plexus.iberiamusic.data.time.TimeProvider
import com.plexus.iberiamusic.domain.model.Track
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class InMemorySearchCacheTest {

  private var now = 0L
  private val clock: TimeProvider = mockk { every { nowMillis() } answers { now } }
  private val cache = InMemorySearchCache(timeProvider = clock)
  private val tracks = listOf(Track(id = 1L, name = "Song", artistName = "Artist"))

  @Test
  fun `GIVEN an unknown query WHEN get THEN returns null`() {
    assertNull(cache.get("queen"))
  }

  @Test
  fun `GIVEN a stored query WHEN get THEN returns the stored tracks`() {
    cache.put("queen", tracks)

    assertEquals(tracks, cache.get("queen"))
  }

  @Test
  fun `GIVEN a stored query WHEN just before the TTL elapses THEN it is still valid`() {
    cache.put("queen", tracks)

    now += InMemorySearchCache.DEFAULT_TTL_MILLIS - 1

    assertEquals(tracks, cache.get("queen"))
  }

  @Test
  fun `GIVEN a stored query WHEN the TTL elapses THEN it expires`() {
    cache.put("queen", tracks)

    now += InMemorySearchCache.DEFAULT_TTL_MILLIS

    assertNull(cache.get("queen"))
  }

  @Test
  fun `GIVEN the default configuration THEN the TTL is three minutes`() {
    assertEquals(3 * 60 * 1000L, InMemorySearchCache.DEFAULT_TTL_MILLIS)
  }

  @Test
  fun `GIVEN an expired entry WHEN put again THEN stores fresh data with a new TTL`() {
    cache.put("queen", tracks)
    now += InMemorySearchCache.DEFAULT_TTL_MILLIS
    val fresh = listOf(Track(id = 2L, name = "Other", artistName = "Artist"))

    cache.put("queen", fresh)
    now += InMemorySearchCache.DEFAULT_TTL_MILLIS - 1

    assertEquals(fresh, cache.get("queen"))
  }

  @Test
  fun `GIVEN a clock that goes backwards WHEN get THEN the entry is not served`() {
    now += 10_000
    cache.put("queen", tracks)

    now -= 5_000

    assertNull(cache.get("queen"))
  }

  @Test
  fun `GIVEN a full cache WHEN put THEN evicts the least recently used entry`() {
    val small = InMemorySearchCache(timeProvider = clock, maxEntries = 2)
    small.put("a", tracks)
    small.put("b", tracks)
    small.get("a") // "b" is now the least recently used

    small.put("c", tracks)

    assertEquals(tracks, small.get("a"))
    assertNull(small.get("b"))
    assertEquals(tracks, small.get("c"))
  }

  @Test
  fun `GIVEN many puts THEN never holds more entries than the maximum`() {
    val small = InMemorySearchCache(timeProvider = clock, maxEntries = 3)

    (1..10).forEach { small.put("q$it", tracks) }

    assertEquals(3, (1..10).count { small.get("q$it") != null })
  }

  @Test
  fun `GIVEN an expired entry never read again WHEN put THEN the purge frees its slot`() {
    val small = InMemorySearchCache(timeProvider = clock, maxEntries = 2)
    small.put("expiring", tracks)
    now += 100_000
    small.put("live", tracks)
    small.get("expiring") // now "live" is the least recently used
    now += InMemorySearchCache.DEFAULT_TTL_MILLIS - 100_000

    // "expiring" is expired but never read again; the purge frees its slot so "live" survives.
    small.put("new", tracks)

    assertEquals(tracks, small.get("live"))
    assertEquals(tracks, small.get("new"))
  }

  @Test
  fun `GIVEN two queries THEN they are cached independently`() {
    cache.put("queen", tracks)

    assertNull(cache.get("bowie"))
  }
}
