package com.plexus.iberiamusic.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.usecase.SearchTracksUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/**
 * Turns what the user types into a [SearchUiState]. Every change of the query shows
 * [SearchUiState.Loading] at once, waits [DEBOUNCE_MILLIS] and then searches, all inside
 * [flatMapLatest]: a new query cancels the one in flight, so results of a superseded query are
 * never emitted and never sit next to the new text.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(private val searchTracks: SearchTracksUseCase) : ViewModel() {

  /** A search to run. [immediate] skips the debounce; [nonce] makes repeated requests distinct. */
  private data class Request(val text: String, val immediate: Boolean = false, val nonce: Int = 0)

  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query.asStateFlow()

  private val request = MutableStateFlow(Request(""))
  private var searchCount = 0

  val uiState: StateFlow<SearchUiState> =
      request
          .flatMapLatest { search(it) }
          .stateIn(
              viewModelScope,
              SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
              SearchUiState.Idle,
          )

  fun onQueryChange(text: String) {
    _query.value = text
    request.value = Request(text)
  }

  /** Searches the current query right away (the keyboard's search action); also retries errors. */
  fun onSearch() {
    request.value = Request(_query.value, immediate = true, nonce = ++searchCount)
  }

  private fun search(request: Request): Flow<SearchUiState> = flow {
    if (request.text.isBlank()) {
      emit(SearchUiState.Idle)
      return@flow
    }
    emit(SearchUiState.Loading)
    if (!request.immediate) delay(DEBOUNCE_MILLIS)
    emit(searchTracks(request.text).toUiState())
  }

  private fun SearchResult.toUiState(): SearchUiState =
      when (this) {
        is SearchResult.Success -> SearchUiState.Content(tracks)
        SearchResult.Empty -> SearchUiState.Empty
        is SearchResult.Error -> SearchUiState.Error
      }

  companion object {
    const val DEBOUNCE_MILLIS = 300L
    private const val STOP_TIMEOUT_MILLIS = 5_000L
  }
}
