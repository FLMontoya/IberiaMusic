package com.plexus.iberiamusic.domain.repository

import com.plexus.iberiamusic.domain.model.SearchResult

/** Test double that records the queries it receives and answers with a configurable result. */
class FakeSearchRepository(var result: SearchResult = SearchResult.Empty) : SearchRepository {

  val receivedQueries = mutableListOf<String>()

  override suspend fun search(query: String): SearchResult {
    receivedQueries += query
    return result
  }
}
