package com.plexus.iberiamusic.presentation.navigation

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
          track.largeArtworkUrl,
      )
    }

internal fun restoreTrack(saved: List<Any?>): Track? {
  return Track(
      id = saved.getOrNull(0) as? Long ?: return null,
      name = saved.getOrNull(1) as? String ?: return null,
      artistName = saved.getOrNull(2) as? String ?: return null,
      albumName = saved.getOrNull(3) as? String,
      artworkUrl = saved.getOrNull(4) as? String,
      genre = saved.getOrNull(5) as? String,
      durationMillis = saved.getOrNull(6) as? Long,
      releaseDate = saved.getOrNull(7) as? String,
      largeArtworkUrl = saved.getOrNull(8) as? String,
  )
}
