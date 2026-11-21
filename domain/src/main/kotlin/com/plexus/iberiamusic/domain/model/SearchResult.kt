package com.plexus.iberiamusic.domain.model

/** Outcome of a track search. Failures are values, not exceptions. */
sealed interface SearchResult {
  data class Success(val tracks: List<Track>) : SearchResult

  data object Empty : SearchResult

  /** The search failed for [reason]; [cause] keeps the original exception for diagnosis. */
  data class Error(val cause: Throwable, val reason: SearchFailure = SearchFailure.UNKNOWN) :
      SearchResult
}
