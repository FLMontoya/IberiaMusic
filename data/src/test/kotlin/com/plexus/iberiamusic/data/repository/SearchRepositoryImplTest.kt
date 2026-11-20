package com.plexus.iberiamusic.data.repository

import com.plexus.iberiamusic.data.cache.InMemorySearchCache
import com.plexus.iberiamusic.data.remote.ITunesApi
import com.plexus.iberiamusic.data.remote.dto.ITunesSearchResponseDto
import com.plexus.iberiamusic.data.remote.dto.ITunesTrackDto
import com.plexus.iberiamusic.data.time.TimeProvider
import com.plexus.iberiamusic.domain.model.SearchResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class SearchRepositoryImplTest {

  private var now = 0L
  private val clock: TimeProvider = mockk { every { nowMillis() } answers { now } }
  private val cache = InMemorySearchCache(timeProvider = clock)
  private val api: ITunesApi = mockk()
  private val repository = SearchRepositoryImpl(api, cache)

  @Test
  fun `GIVEN a cache miss WHEN search THEN calls the network and returns the mapped tracks`() =
      runTest {
        coEvery { api.search(any()) } returns responseOf(1L, 2L)

        val result = repository.search("queen")

        assertEquals(listOf(1L, 2L), idsOf(result))
        coVerify(exactly = 1) { api.search("queen") }
        confirmVerified(api)
      }

  @Test
  fun `GIVEN a valid cache WHEN search THEN it is served without calling the network`() = runTest {
    coEvery { api.search(any()) } returns responseOf(1L, 2L)
    repository.search("queen")

    val result = repository.search("queen")

    assertEquals(listOf(1L, 2L), idsOf(result))
    coVerify(exactly = 1) { api.search("queen") }
    confirmVerified(api)
  }

  @Test
  fun `GIVEN an expired cache WHEN search THEN goes back to the network and refreshes the entry`() =
      runTest {
        coEvery { api.search(any()) } returns responseOf(1L, 2L) andThen responseOf(3L)
        repository.search("queen")
        now += InMemorySearchCache.DEFAULT_TTL_MILLIS

        val refreshed = repository.search("queen")
        val cached = repository.search("queen")

        assertEquals(listOf(3L), idsOf(refreshed))
        assertEquals(listOf(3L), idsOf(cached))
        coVerify(exactly = 2) { api.search("queen") }
        confirmVerified(api)
      }

  @Test
  fun `GIVEN different queries WHEN search THEN they do not share cache entries`() = runTest {
    coEvery { api.search(any()) } returns responseOf(1L)

    repository.search("queen")
    repository.search("bowie")

    coVerify(exactly = 1) { api.search("queen") }
    coVerify(exactly = 1) { api.search("bowie") }
    confirmVerified(api)
  }

  @Test
  fun `GIVEN queries differing only in case or spaces WHEN search THEN they share a cache entry`() =
      runTest {
        coEvery { api.search(any()) } returns responseOf(1L)
        repository.search("Queen")

        repository.search("  queen ")
        repository.search("QUEEN")

        coVerify(exactly = 1) { api.search(any()) }
        confirmVerified(api)
      }

  @Test
  fun `GIVEN tracks with a repeated id WHEN search THEN each is returned once`() = runTest {
    coEvery { api.search(any()) } returns responseOf(1L, 2L, 1L)

    val result = repository.search("queen")

    assertEquals(listOf(1L, 2L), idsOf(result))
  }

  @Test
  fun `GIVEN no results WHEN search THEN maps to Empty`() = runTest {
    coEvery { api.search(any()) } returns ITunesSearchResponseDto()

    assertEquals(SearchResult.Empty, repository.search("zzzz"))
  }

  @Test
  fun `GIVEN a network failure WHEN search THEN returns Error and is not cached`() = runTest {
    val failure = IOException("offline")
    coEvery { api.search(any()) } throws failure andThen responseOf(1L)

    val first = repository.search("queen")
    val second = repository.search("queen")

    assertEquals(SearchResult.Error(failure), first)
    assertIs<SearchResult.Success>(second)
    coVerify(exactly = 2) { api.search("queen") }
    confirmVerified(api)
  }

  @Test
  fun `GIVEN a cancellation WHEN search THEN it is propagated and not turned into Error`() =
      runTest {
        coEvery { api.search(any()) } throws CancellationException("cancelled")

        val thrown = assertFailsWith<CancellationException> { repository.search("queen") }

        assertEquals("cancelled", thrown.message)
      }

  private fun idsOf(result: SearchResult): List<Long> =
      assertIs<SearchResult.Success>(result).tracks.map { it.id }

  private fun responseOf(vararg ids: Long) =
      ITunesSearchResponseDto(
          resultCount = ids.size,
          results =
              ids.map {
                ITunesTrackDto(trackId = it, trackName = "Song $it", artistName = "Artist")
              },
      )
}
