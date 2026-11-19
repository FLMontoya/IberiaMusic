package com.plexus.iberiamusic.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.usecase.SearchTracksUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/**
 * Turns what the user types into a [SearchUiState]. A new query cancels the search in flight
 * ([flatMapLatest]), so results of a superseded query are never emitted.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(private val searchTracks: SearchTracksUseCase) : ViewModel() {

  private val _query = MutableStateFlow("")
  val query: StateFlow<String> = _query.asStateFlow()

  val uiState: StateFlow<SearchUiState> =
      _query
          .debounce(DEBOUNCE_MILLIS)
          .distinctUntilChanged()
          .flatMapLatest { text -> search(text) }
          .stateIn(
              viewModelScope,
              SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
              SearchUiState.Idle,
          )

  fun onQueryChange(text: String) {
    _query.value = text
  }

  private fun search(text: String): Flow<SearchUiState> = flow {
    if (text.isBlank()) {
      emit(SearchUiState.Idle)
      return@flow
    }
    emit(SearchUiState.Loading)
    emit(searchTracks(text).toUiState())
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
