package com.plexus.iberiamusic.data.cache

import com.plexus.iberiamusic.data.time.SystemTimeProvider
import com.plexus.iberiamusic.data.time.TimeProvider
import com.plexus.iberiamusic.domain.model.Track

/**
 * In-memory cache of search results keyed by query, where each entry is valid for [ttlMillis]
 * counted from the moment it was stored. Expired entries are never returned.
 */
class InMemorySearchCache(
    private val timeProvider: TimeProvider = SystemTimeProvider,
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
) {

  private class Entry(val tracks: List<Track>, val storedAtMillis: Long)

  private val entries = HashMap<String, Entry>()

  /** Returns the cached tracks for [query], or null when absent or expired. */
  @Synchronized
  fun get(query: String): List<Track>? {
    val entry = entries[query] ?: return null
    if (timeProvider.nowMillis() - entry.storedAtMillis >= ttlMillis) {
      entries.remove(query)
      return null
    }
    return entry.tracks
  }

  /** Stores [tracks] for [query], replacing any previous entry and restarting its TTL. */
  @Synchronized
  fun put(query: String, tracks: List<Track>) {
    entries[query] = Entry(tracks, timeProvider.nowMillis())
  }

  companion object {
    const val DEFAULT_TTL_MILLIS: Long = 3 * 60 * 1000L
  }
}
