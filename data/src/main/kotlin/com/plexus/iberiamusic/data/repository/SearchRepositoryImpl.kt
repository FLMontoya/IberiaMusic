package com.plexus.iberiamusic.data.repository

import com.plexus.iberiamusic.data.cache.InMemorySearchCache
import com.plexus.iberiamusic.data.remote.ITunesApi
import com.plexus.iberiamusic.data.remote.mapper.toTracks
import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.domain.repository.SearchRepository
import kotlin.coroutines.cancellation.CancellationException

/**
 * Serves searches from the cache while valid; otherwise queries iTunes and refreshes the cache. The
 * cache key ignores case and surrounding spaces.
 */
class SearchRepositoryImpl(private val api: ITunesApi, private val cache: InMemorySearchCache) :
    SearchRepository {

  override suspend fun search(query: String): SearchResult {
    val key = query.trim().lowercase()
    cache.get(key)?.let {
      return it.toResult()
    }

    return try {
      // The UI uses the track id as the list key, which must be unique.
      val tracks = api.search(query).toTracks().distinctBy { it.id }
      cache.put(key, tracks)
      tracks.toResult()
    } catch (cancellation: CancellationException) {
      throw cancellation
    } catch (failure: Exception) {
      SearchResult.Error(failure)
    }
  }

  private fun List<Track>.toResult(): SearchResult =
      if (isEmpty()) SearchResult.Empty else SearchResult.Success(this)
}
