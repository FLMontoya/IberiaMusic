package com.plexus.iberiamusic.data.remote.mapper

import com.plexus.iberiamusic.data.remote.dto.ITunesSearchResponseDto
import com.plexus.iberiamusic.data.remote.dto.ITunesTrackDto
import com.plexus.iberiamusic.domain.model.Track

/*
 * iTunes serves the same cover at any size by changing the size in its URL, but the search results
 * only carry the 100 px thumbnail, which would look blurry in the detail. This is a fact about
 * iTunes, so it lives here and not in the UI.
 */
private const val THUMBNAIL_SIZE = "100x100bb"
private const val LARGE_SIZE = "600x600bb"

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
      largeArtworkUrl = artworkUrl100?.replace(THUMBNAIL_SIZE, LARGE_SIZE),
      genre = primaryGenreName,
      durationMillis = trackTimeMillis,
      releaseDate = releaseDate,
  )
}

internal fun ITunesSearchResponseDto.toTracks(): List<Track> = results.mapNotNull { it.toTrack() }
