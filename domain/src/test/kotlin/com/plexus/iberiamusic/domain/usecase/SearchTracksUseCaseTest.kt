package com.plexus.iberiamusic.domain.usecase

import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.domain.repository.SearchRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SearchTracksUseCaseTest {

  private val repository: SearchRepository = mockk()
  private val useCase = SearchTracksUseCase(repository)

  @Test
  fun `GIVEN a blank query WHEN invoked THEN returns Empty without calling the repository`() =
      runTest {
        val result = useCase("   ")

        assertEquals(SearchResult.Empty, result)
        confirmVerified(repository)
      }

  @Test
  fun `GIVEN a padded query WHEN invoked THEN the repository receives it trimmed`() = runTest {
    coEvery { repository.search("queen") } returns SearchResult.Empty

    useCase("  queen  ")

    coVerify(exactly = 1) { repository.search("queen") }
    confirmVerified(repository)
  }

  @Test
  fun `GIVEN the repository succeeds WHEN invoked THEN returns its success`() = runTest {
    val expected =
        SearchResult.Success(listOf(Track(id = 1L, name = "Song", artistName = "Artist")))
    coEvery { repository.search("song") } returns expected

    assertEquals(expected, useCase("song"))

    coVerify(exactly = 1) { repository.search("song") }
    confirmVerified(repository)
  }

  @Test
  fun `GIVEN the repository fails WHEN invoked THEN returns its error`() = runTest {
    val expected = SearchResult.Error(IllegalStateException("boom"))
    coEvery { repository.search("song") } returns expected

    assertEquals(expected, useCase("song"))

    coVerify(exactly = 1) { repository.search("song") }
    confirmVerified(repository)
  }
}
