package com.plexus.iberiamusic.domain.repository

import com.plexus.iberiamusic.domain.model.SearchResult

/** Source of track search results. Implementations never throw for expected failures. */
interface SearchRepository {
  suspend fun search(query: String): SearchResult
}
