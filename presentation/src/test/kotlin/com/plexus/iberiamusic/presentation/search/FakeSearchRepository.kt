package com.plexus.iberiamusic.presentation.search

import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.repository.SearchRepository
import kotlinx.coroutines.CompletableDeferred

/** Test double: answers from [results], or suspends on a gate created with [hold]. */
class FakeSearchRepository : SearchRepository {

  val receivedQueries = mutableListOf<String>()
  val results = mutableMapOf<String, SearchResult>()
  private val gates = mutableMapOf<String, CompletableDeferred<SearchResult>>()

  /** Makes the next searches for [query] suspend until the returned deferred is completed. */
  fun hold(query: String): CompletableDeferred<SearchResult> =
      CompletableDeferred<SearchResult>().also { gates[query] = it }

  override suspend fun search(query: String): SearchResult {
    receivedQueries += query
    return gates[query]?.await() ?: results[query] ?: SearchResult.Empty
  }
}
