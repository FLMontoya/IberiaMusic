package com.plexus.iberiamusic.domain.model

/** Outcome of a track search. Failures are values, not exceptions. */
sealed interface SearchResult {
  data class Success(val tracks: List<Track>) : SearchResult

  data object Empty : SearchResult

  data class Error(val cause: Throwable) : SearchResult
}
