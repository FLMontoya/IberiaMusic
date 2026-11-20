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
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
) {

  init {
    require(maxEntries > 0) { "maxEntries must be positive" }
  }

  private class Entry(val tracks: List<Track>, val storedAtMillis: Long)

  // Access-ordered, so iteration starts at the least recently used entry.
  private val entries = LinkedHashMap<String, Entry>(INITIAL_CAPACITY, LOAD_FACTOR, true)

  /** Returns the cached tracks for [query], or null when absent or expired. */
  @Synchronized
  fun get(query: String): List<Track>? {
    val entry = entries[query] ?: return null
    if (isExpired(entry)) {
      entries.remove(query)
      return null
    }
    return entry.tracks
  }

  /** A clock that went backwards cannot be trusted, so the entry is treated as expired. */
  private fun isExpired(entry: Entry): Boolean {
    val elapsed = timeProvider.nowMillis() - entry.storedAtMillis
    return elapsed < 0 || elapsed >= ttlMillis
  }

  /**
   * Stores [tracks] for [query], replacing any previous entry and restarting its TTL. Expired
   * entries are purged first, then the least recently used ones are evicted beyond [maxEntries].
   */
  @Synchronized
  fun put(query: String, tracks: List<Track>) {
    entries.values.removeAll { isExpired(it) }
    entries[query] = Entry(tracks, timeProvider.nowMillis())
    while (entries.size > maxEntries) {
      val eldest = entries.keys.iterator()
      eldest.next()
      eldest.remove()
    }
  }

  companion object {
    const val DEFAULT_TTL_MILLIS: Long = 3 * 60 * 1000L
    const val DEFAULT_MAX_ENTRIES: Int = 50
    private const val INITIAL_CAPACITY = 16
    private const val LOAD_FACTOR = 0.75f
  }
}
