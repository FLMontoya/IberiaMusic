package com.plexus.iberiamusic.presentation.search

import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.domain.usecase.SearchTracksUseCase
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

  private val dispatcher = StandardTestDispatcher()
  private val repository = FakeSearchRepository()
  private lateinit var viewModel: SearchViewModel

  private val queenTracks = listOf(Track(id = 1L, name = "Bohemian Rhapsody", artistName = "Queen"))
  private val bowieTracks = listOf(Track(id = 2L, name = "Heroes", artistName = "Bowie"))

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
    viewModel = SearchViewModel(SearchTracksUseCase(repository))
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun TestScope.collectStates(): List<SearchUiState> {
    val states = mutableListOf<SearchUiState>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.uiState.collect { states += it }
    }
    return states
  }

  private fun TestScope.type(text: String, thenWait: Long = SearchViewModel.DEBOUNCE_MILLIS) {
    viewModel.onQueryChange(text)
    advanceTimeBy(thenWait)
    runCurrent()
  }

  @Test
  fun `initial state is Idle`() =
      runTest(dispatcher) {
        val states = collectStates()

        assertEquals(listOf<SearchUiState>(SearchUiState.Idle), states)
      }

  @Test
  fun `query text follows what the user types`() =
      runTest(dispatcher) {
        collectStates()

        viewModel.onQueryChange("que")

        assertEquals("que", viewModel.query.value)
      }

  @Test
  fun `does not search until the debounce elapses`() =
      runTest(dispatcher) {
        collectStates()

        type("queen", thenWait = SearchViewModel.DEBOUNCE_MILLIS - 1)

        assertTrue(repository.receivedQueries.isEmpty())
      }

  @Test
  fun `typing quickly only searches the last query`() =
      runTest(dispatcher) {
        collectStates()

        type("q", thenWait = 100)
        type("qu", thenWait = 100)
        type("queen")

        assertEquals(listOf("queen"), repository.receivedQueries)
      }

  @Test
  fun `shows Loading while searching and then the Content`() =
      runTest(dispatcher) {
        val gate = repository.hold("queen")
        val states = collectStates()

        type("queen")
        assertEquals(SearchUiState.Loading, states.last())

        gate.complete(SearchResult.Success(queenTracks))
        runCurrent()
        assertEquals(SearchUiState.Content(queenTracks), states.last())
      }

  @Test
  fun `typing shows Loading at once so earlier results are not left next to the new text`() =
      runTest(dispatcher) {
        repository.results["queen"] = SearchResult.Success(queenTracks)
        val states = collectStates()
        type("queen")

        viewModel.onQueryChange("queens")
        runCurrent()

        assertEquals(SearchUiState.Loading, states.last())
      }

  @Test
  fun `the search action searches without waiting for the debounce`() =
      runTest(dispatcher) {
        repository.results["queen"] = SearchResult.Success(queenTracks)
        val states = collectStates()

        viewModel.onQueryChange("queen")
        viewModel.onSearch()
        runCurrent()

        assertEquals(listOf("queen"), repository.receivedQueries)
        assertEquals(SearchUiState.Content(queenTracks), states.last())
      }

  @Test
  fun `the search action retries a failed search`() =
      runTest(dispatcher) {
        repository.results["queen"] = SearchResult.Error(IOException("offline"))
        val states = collectStates()
        type("queen")
        repository.results["queen"] = SearchResult.Success(queenTracks)

        viewModel.onSearch()
        runCurrent()

        assertEquals(SearchUiState.Content(queenTracks), states.last())
      }

  @Test
  fun `the search action with a blank query stays Idle`() =
      runTest(dispatcher) {
        val states = collectStates()

        viewModel.onSearch()
        runCurrent()

        assertEquals(SearchUiState.Idle, states.last())
        assertTrue(repository.receivedQueries.isEmpty())
      }

  @Test
  fun `a cancelled search never emits its results`() =
      runTest(dispatcher) {
        val staleGate = repository.hold("queen")
        repository.results["bowie"] = SearchResult.Success(bowieTracks)
        val states = collectStates()

        type("queen")
        type("bowie")
        staleGate.complete(SearchResult.Success(queenTracks))
        runCurrent()

        assertTrue(states.none { it == SearchUiState.Content(queenTracks) })
        assertEquals(SearchUiState.Content(bowieTracks), states.last())
      }

  @Test
  fun `no results show Empty`() =
      runTest(dispatcher) {
        repository.results["zzzz"] = SearchResult.Empty
        val states = collectStates()

        type("zzzz")

        assertEquals(SearchUiState.Empty, states.last())
      }

  @Test
  fun `a failed search shows Error`() =
      runTest(dispatcher) {
        repository.results["queen"] = SearchResult.Error(IOException("offline"))
        val states = collectStates()

        type("queen")

        assertEquals(SearchUiState.Error, states.last())
      }

  @Test
  fun `clearing the query goes back to Idle`() =
      runTest(dispatcher) {
        repository.results["queen"] = SearchResult.Success(queenTracks)
        val states = collectStates()

        type("queen")
        type("")

        assertEquals(SearchUiState.Idle, states.last())
      }
}
