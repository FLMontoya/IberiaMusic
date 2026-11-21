package com.plexus.iberiamusic.data.remote.mapper

import com.plexus.iberiamusic.data.remote.dto.ITunesSearchResponseDto
import com.plexus.iberiamusic.data.remote.dto.ITunesTrackDto
import com.plexus.iberiamusic.domain.model.Track
import java.time.DateTimeException
import java.time.LocalDate

/*
 * iTunes serves the same cover at any size by changing the size in its URL, but the search results
 * only carry the 100 px thumbnail, which would look blurry in the detail. This is a fact about
 * iTunes, so it lives here and not in the UI.
 */
private const val THUMBNAIL_SIZE = "100x100bb"
private const val LARGE_SIZE = "600x600bb"
private const val ISO_DATE_LENGTH = 10

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
      releaseDate = releaseDate?.toLocalDateOrNull(),
  )
}

/**
 * iTunes sends a timestamp such as `1975-10-31T07:00:00Z`, but only the day is meaningful: it is
 * stored at 07:00 UTC for a release dated in the US Pacific time, so the day part is the intended
 * date whatever time zone the device is in. Returns null when it is not a date.
 */
private fun String.toLocalDateOrNull(): LocalDate? =
    try {
      LocalDate.parse(take(ISO_DATE_LENGTH))
    } catch (_: DateTimeException) {
      null
    }

internal fun ITunesSearchResponseDto.toTracks(): List<Track> = results.mapNotNull { it.toTrack() }
