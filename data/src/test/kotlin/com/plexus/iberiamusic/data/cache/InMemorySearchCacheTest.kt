package com.plexus.iberiamusic.data.cache

import com.plexus.iberiamusic.data.time.FakeTimeProvider
import com.plexus.iberiamusic.domain.model.Track
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemorySearchCacheTest {

  private val clock = FakeTimeProvider()
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
    clock.advanceBy(10_000)
    cache.put("queen", tracks)

    clock.advanceBy(-5_000)

    assertNull(cache.get("queen"))
  }

  @Test
  fun `queries are cached independently`() {
    cache.put("queen", tracks)

    assertNull(cache.get("bowie"))
  }
}
