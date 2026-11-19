package com.plexus.iberiamusic.presentation.search

import com.plexus.iberiamusic.domain.model.Track

/** Everything the search screen can be showing, exposed through a single state holder. */
sealed interface SearchUiState {
  /** Nothing typed yet. */
  data object Idle : SearchUiState

  data object Loading : SearchUiState

  data class Content(val tracks: List<Track>) : SearchUiState

  data object Empty : SearchUiState

  data object Error : SearchUiState
}
