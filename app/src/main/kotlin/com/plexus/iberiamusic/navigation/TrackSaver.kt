package com.plexus.iberiamusic.navigation

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.plexus.iberiamusic.domain.model.Track

/**
 * Keeps the selected [Track] across process death, so the detail screen can be restored along with
 * the back stack. [Track] is not Parcelable (the domain module has no Android dependencies).
 */
internal val TrackSaver: Saver<Track?, Any> =
    listSaver(save = { saveTrack(it) }, restore = { restoreTrack(it) })

internal fun saveTrack(track: Track?): List<Any?> =
    if (track == null) {
      emptyList()
    } else {
      listOf(
          track.id,
          track.name,
          track.artistName,
          track.albumName,
          track.artworkUrl,
          track.genre,
          track.durationMillis,
          track.releaseDate,
      )
    }

internal fun restoreTrack(saved: List<Any?>): Track? {
  if (saved.isEmpty()) return null
  return Track(
      id = saved[0] as Long,
      name = saved[1] as String,
      artistName = saved[2] as String,
      albumName = saved[3] as String?,
      artworkUrl = saved[4] as String?,
      genre = saved[5] as String?,
      durationMillis = saved[6] as Long?,
      releaseDate = saved[7] as String?,
  )
}
