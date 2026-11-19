package com.plexus.iberiamusic.domain.usecase

import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.repository.SearchRepository

/** Searches tracks by free text. A blank query yields [SearchResult.Empty] with no lookup. */
class SearchTracksUseCase(private val repository: SearchRepository) {

  suspend operator fun invoke(query: String): SearchResult {
    val normalized = query.trim()
    if (normalized.isEmpty()) return SearchResult.Empty
    return repository.search(normalized)
  }
}
