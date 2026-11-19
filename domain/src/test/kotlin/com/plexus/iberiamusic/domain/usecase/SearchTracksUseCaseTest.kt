package com.plexus.iberiamusic.domain.usecase

import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.domain.repository.FakeSearchRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTracksUseCaseTest {

  private val repository = FakeSearchRepository()
  private val useCase = SearchTracksUseCase(repository)

  @Test
  fun `blank query returns Empty without calling the repository`() = runTest {
    val result = useCase("   ")

    assertEquals(SearchResult.Empty, result)
    assertTrue(repository.receivedQueries.isEmpty())
  }

  @Test
  fun `query is trimmed before reaching the repository`() = runTest {
    useCase("  queen  ")

    assertEquals(listOf("queen"), repository.receivedQueries)
  }

  @Test
  fun `returns the repository success`() = runTest {
    val expected =
        SearchResult.Success(listOf(Track(id = 1L, name = "Song", artistName = "Artist")))
    repository.result = expected

    assertEquals(expected, useCase("song"))
  }

  @Test
  fun `returns the repository error`() = runTest {
    val expected = SearchResult.Error(IllegalStateException("boom"))
    repository.result = expected

    assertEquals(expected, useCase("song"))
  }
}
