package com.plexus.iberiamusic.data.repository

import com.plexus.iberiamusic.data.cache.InMemorySearchCache
import com.plexus.iberiamusic.data.remote.ITunesApi
import com.plexus.iberiamusic.data.remote.mapper.toTracks
import com.plexus.iberiamusic.domain.logging.ErrorLogger
import com.plexus.iberiamusic.domain.model.SearchFailure
import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.domain.repository.SearchRepository
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/**
 * Serves searches from the cache while valid; otherwise queries iTunes and refreshes the cache. The
 * cache key ignores case and surrounding spaces. A failure becomes a [SearchResult.Error] that says
 * why it happened, and is reported to [errorLogger] (without the search text) so it leaves a trace.
 */
class SearchRepositoryImpl(
    private val api: ITunesApi,
    private val cache: InMemorySearchCache,
    private val errorLogger: ErrorLogger,
) : SearchRepository {

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
      val reason = failure.toSearchFailure()
      errorLogger.log("Search failed ($reason)", failure)
      SearchResult.Error(failure, reason)
    }
  }

  /**
   * No answer at all is a network problem; an error status or a payload that cannot be decoded is
   * the service's. Anything else is unexpected, but it is still shown as an error and logged, since
   * letting it crash the app would not help the user.
   */
  private fun Exception.toSearchFailure(): SearchFailure =
      when (this) {
        is IOException -> SearchFailure.NETWORK
        is HttpException,
        is SerializationException -> SearchFailure.SERVER
        else -> SearchFailure.UNKNOWN
      }

  private fun List<Track>.toResult(): SearchResult =
      if (isEmpty()) SearchResult.Empty else SearchResult.Success(this)
}
