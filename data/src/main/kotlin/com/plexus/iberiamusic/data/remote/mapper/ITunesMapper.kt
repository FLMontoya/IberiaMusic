package com.plexus.iberiamusic.data.remote.mapper

import com.plexus.iberiamusic.data.remote.dto.ITunesSearchResponseDto
import com.plexus.iberiamusic.data.remote.dto.ITunesTrackDto
import com.plexus.iberiamusic.domain.model.Track

/** Returns null when the result lacks an id, a name or an artist, since it cannot be shown. */
internal fun ITunesTrackDto.toTrack(): Track? {
  val id = trackId ?: return null
  val name = trackName ?: return null
  val artist = artistName ?: return null
  return Track(
      id = id,
      name = name,
      artistName = artist,
      albumName = collectionName,
      artworkUrl = artworkUrl100,
      genre = primaryGenreName,
      durationMillis = trackTimeMillis,
      releaseDate = releaseDate,
  )
}

internal fun ITunesSearchResponseDto.toTracks(): List<Track> = results.mapNotNull { it.toTrack() }
