package com.plexus.iberiamusic.data.repository

import com.plexus.iberiamusic.data.cache.InMemorySearchCache
import com.plexus.iberiamusic.data.remote.FakeITunesApi
import com.plexus.iberiamusic.data.remote.dto.ITunesSearchResponseDto
import com.plexus.iberiamusic.data.remote.dto.ITunesTrackDto
import com.plexus.iberiamusic.data.time.FakeTimeProvider
import com.plexus.iberiamusic.domain.model.SearchResult
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SearchRepositoryImplTest {

  private val clock = FakeTimeProvider()
  private val cache = InMemorySearchCache(timeProvider = clock)
  private val api = FakeITunesApi(response = responseOf(1L, 2L))
  private val repository = SearchRepositoryImpl(api, cache)

  @Test
  fun `cache miss calls the network and returns the mapped tracks`() = runTest {
    val result = repository.search("queen")

    assertEquals(1, api.callCount)
    assertEquals(listOf(1L, 2L), (result as SearchResult.Success).tracks.map { it.id })
  }

  @Test
  fun `valid cache is served without calling the network`() = runTest {
    repository.search("queen")

    val result = repository.search("queen")

    assertEquals(1, api.callCount)
    assertEquals(listOf(1L, 2L), (result as SearchResult.Success).tracks.map { it.id })
  }

  @Test
  fun `expired cache goes back to the network and refreshes the entry`() = runTest {
    repository.search("queen")
    clock.advanceBy(InMemorySearchCache.DEFAULT_TTL_MILLIS)
    api.response = responseOf(3L)

    val refreshed = repository.search("queen")
    val cached = repository.search("queen")

    assertEquals(2, api.callCount)
    assertEquals(listOf(3L), (refreshed as SearchResult.Success).tracks.map { it.id })
    assertEquals(listOf(3L), (cached as SearchResult.Success).tracks.map { it.id })
  }

  @Test
  fun `different queries do not share cache entries`() = runTest {
    repository.search("queen")
    repository.search("bowie")

    assertEquals(2, api.callCount)
  }

  @Test
  fun `no results map to Empty`() = runTest {
    api.response = ITunesSearchResponseDto()

    assertEquals(SearchResult.Empty, repository.search("zzzz"))
  }

  @Test
  fun `network failure returns Error and is not cached`() = runTest {
    val failure = IOException("offline")
    api.failure = failure

    val first = repository.search("queen")
    api.failure = null
    val second = repository.search("queen")

    assertEquals(SearchResult.Error(failure), first)
    assertTrue(second is SearchResult.Success)
    assertEquals(2, api.callCount)
  }

  @Test
  fun `cancellation is propagated and not turned into Error`() = runTest {
    api.failure = CancellationException("cancelled")

    try {
      repository.search("queen")
      fail("CancellationException should have been rethrown")
    } catch (expected: CancellationException) {
      assertEquals("cancelled", expected.message)
    }
  }

  private fun responseOf(vararg ids: Long) =
      ITunesSearchResponseDto(
          resultCount = ids.size,
          results =
              ids.map {
                ITunesTrackDto(trackId = it, trackName = "Song $it", artistName = "Artist")
              },
      )
}
