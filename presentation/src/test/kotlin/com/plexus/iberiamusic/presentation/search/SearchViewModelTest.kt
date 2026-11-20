package com.plexus.iberiamusic.presentation.search

import app.cash.turbine.test
import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.domain.repository.SearchRepository
import com.plexus.iberiamusic.domain.usecase.SearchTracksUseCase
import com.plexus.iberiamusic.presentation.testing.MainCoroutinesRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import java.io.IOException
import kotlin.test.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

  @get:Rule val coroutinesRule = MainCoroutinesRule()

  private val repository: SearchRepository = mockk()
  private val viewModel by lazy { SearchViewModel(SearchTracksUseCase(repository)) }

  private val queenTracks = listOf(Track(id = 1L, name = "Bohemian Rhapsody", artistName = "Queen"))
  private val bowieTracks = listOf(Track(id = 2L, name = "Heroes", artistName = "Bowie"))

  @Test
  fun `GIVEN a new ViewModel THEN the state is Idle`() = coroutinesRule.runTest {
    viewModel.uiState.test {
      assertEquals(SearchUiState.Idle, awaitItem())
      expectNoEvents()
    }

    confirmVerified(repository)
  }

  @Test
  fun `GIVEN the user types WHEN the query changes THEN the query follows the text`() =
      coroutinesRule.runTest {
        viewModel.query.test {
          assertEquals("", awaitItem())

          viewModel.onQueryChange("que")

          assertEquals("que", awaitItem())
        }
      }

  @Test
  fun `GIVEN the debounce has not elapsed WHEN typing THEN it does not search yet`() =
      coroutinesRule.runTest {
        coEvery { repository.search("queen") } returns SearchResult.Empty

        viewModel.uiState.test {
          assertEquals(SearchUiState.Idle, awaitItem())

          viewModel.onQueryChange("queen")
          assertEquals(SearchUiState.Loading, awaitItem())
          advanceTimeBy(SearchViewModel.DEBOUNCE_MILLIS - 1)
          runCurrent()

          expectNoEvents()
          coVerify(exactly = 0) { repository.search(any()) }
          cancelAndIgnoreRemainingEvents()
        }
      }

  @Test
  fun `GIVEN the user types quickly WHEN the debounce elapses THEN only the last query is searched`() =
      coroutinesRule.runTest {
        coEvery { repository.search("queen") } returns SearchResult.Success(queenTracks)

        viewModel.uiState.test {
          assertEquals(SearchUiState.Idle, awaitItem())

          viewModel.onQueryChange("q")
          advanceTimeBy(100)
          viewModel.onQueryChange("qu")
          advanceTimeBy(100)
          viewModel.onQueryChange("queen")

          assertEquals(SearchUiState.Loading, awaitItem())
          assertEquals(SearchUiState.Content(queenTracks), awaitItem())
        }

        coVerify(exactly = 1) { repository.search("queen") }
        confirmVerified(repository)
      }

  @Test
  fun `GIVEN a slow search WHEN it completes THEN shows Loading and then the Content`() =
      coroutinesRule.runTest {
        val gate = CompletableDeferred<SearchResult>()
        coEvery { repository.search("queen") } coAnswers { gate.await() }

        viewModel.uiState.test {
          assertEquals(SearchUiState.Idle, awaitItem())

          viewModel.onQueryChange("queen")
          assertEquals(SearchUiState.Loading, awaitItem())
          advanceTimeBy(SearchViewModel.DEBOUNCE_MILLIS)
          runCurrent()
          expectNoEvents()

          gate.complete(SearchResult.Success(queenTracks))
          assertEquals(SearchUiState.Content(queenTracks), awaitItem())
        }
      }

  @Test
  fun `GIVEN earlier results WHEN the user keeps typing THEN Loading replaces them at once`() =
      coroutinesRule.runTest {
        coEvery { repository.search("queen") } returns SearchResult.Success(queenTracks)
        coEvery { repository.search("queens") } returns SearchResult.Empty

        viewModel.uiState.test {
          assertEquals(SearchUiState.Idle, awaitItem())
          viewModel.onQueryChange("queen")
          assertEquals(SearchUiState.Loading, awaitItem())
          assertEquals(SearchUiState.Content(queenTracks), awaitItem())

          viewModel.onQueryChange("queens")

          assertEquals(SearchUiState.Loading, awaitItem())
          cancelAndIgnoreRemainingEvents()
        }
      }

  @Test
  fun `GIVEN a typed query WHEN the search action is used THEN it searches without the debounce`() =
      coroutinesRule.runTest {
        coEvery { repository.search("queen") } returns SearchResult.Success(queenTracks)

        viewModel.uiState.test {
          assertEquals(SearchUiState.Idle, awaitItem())

          viewModel.onQueryChange("queen")
          viewModel.onSearch()

          assertEquals(SearchUiState.Loading, awaitItem())
          assertEquals(SearchUiState.Content(queenTracks), awaitItem())
          assertEquals(0L, testScheduler.currentTime)
        }

        coVerify(exactly = 1) { repository.search("queen") }
        confirmVerified(repository)
      }

  @Test
  fun `GIVEN a failed search WHEN the search action is used THEN it retries`() =
      coroutinesRule.runTest {
        coEvery { repository.search("queen") } returnsMany
            listOf(SearchResult.Error(IOException("offline")), SearchResult.Success(queenTracks))

        viewModel.uiState.test {
          assertEquals(SearchUiState.Idle, awaitItem())
          viewModel.onQueryChange("queen")
          assertEquals(SearchUiState.Loading, awaitItem())
          assertEquals(SearchUiState.Error, awaitItem())

          viewModel.onSearch()

          assertEquals(SearchUiState.Loading, awaitItem())
          assertEquals(SearchUiState.Content(queenTracks), awaitItem())
        }

        coVerify(exactly = 2) { repository.search("queen") }
        confirmVerified(repository)
      }

  @Test
  fun `GIVEN a blank query WHEN the search action is used THEN it stays Idle`() =
      coroutinesRule.runTest {
        viewModel.uiState.test {
          assertEquals(SearchUiState.Idle, awaitItem())

          viewModel.onSearch()
          runCurrent()

          expectNoEvents()
        }

        confirmVerified(repository)
      }

  @Test
  fun `GIVEN a search in flight WHEN a new query is typed THEN the old results are never emitted`() =
      coroutinesRule.runTest {
        val staleGate = CompletableDeferred<SearchResult>()
        coEvery { repository.search("queen") } coAnswers { staleGate.await() }
        coEvery { repository.search("bowie") } returns SearchResult.Success(bowieTracks)

        viewModel.uiState.test {
          assertEquals(SearchUiState.Idle, awaitItem())
          viewModel.onQueryChange("queen")
          assertEquals(SearchUiState.Loading, awaitItem())
          advanceTimeBy(SearchViewModel.DEBOUNCE_MILLIS)
          runCurrent()

          viewModel.onQueryChange("bowie")
          staleGate.complete(SearchResult.Success(queenTracks))

          assertEquals(SearchUiState.Content(bowieTracks), awaitItem())
          expectNoEvents()
        }
      }

  @Test
  fun `GIVEN no results WHEN searching THEN shows Empty`() = coroutinesRule.runTest {
    coEvery { repository.search("zzzz") } returns SearchResult.Empty

    viewModel.uiState.test {
      assertEquals(SearchUiState.Idle, awaitItem())

      viewModel.onQueryChange("zzzz")

      assertEquals(SearchUiState.Loading, awaitItem())
      assertEquals(SearchUiState.Empty, awaitItem())
    }
  }

  @Test
  fun `GIVEN a failing search WHEN searching THEN shows Error`() = coroutinesRule.runTest {
    coEvery { repository.search("queen") } returns SearchResult.Error(IOException("offline"))

    viewModel.uiState.test {
      assertEquals(SearchUiState.Idle, awaitItem())

      viewModel.onQueryChange("queen")

      assertEquals(SearchUiState.Loading, awaitItem())
      assertEquals(SearchUiState.Error, awaitItem())
    }
  }

  @Test
  fun `GIVEN results on screen WHEN the query is cleared THEN goes back to Idle`() =
      coroutinesRule.runTest {
        coEvery { repository.search("queen") } returns SearchResult.Success(queenTracks)

        viewModel.uiState.test {
          assertEquals(SearchUiState.Idle, awaitItem())
          viewModel.onQueryChange("queen")
          assertEquals(SearchUiState.Loading, awaitItem())
          assertEquals(SearchUiState.Content(queenTracks), awaitItem())

          viewModel.onQueryChange("")

          assertEquals(SearchUiState.Idle, awaitItem())
        }
      }
}
