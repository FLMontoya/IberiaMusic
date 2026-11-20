package com.plexus.iberiamusic.data.remote.mapper

import com.plexus.iberiamusic.data.remote.dto.ITunesSearchResponseDto
import com.plexus.iberiamusic.data.remote.dto.ITunesTrackDto
import com.plexus.iberiamusic.domain.model.Track
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ITunesMapperTest {

  private val fullDto =
      ITunesTrackDto(
          trackId = 42L,
          trackName = "Bohemian Rhapsody",
          artistName = "Queen",
          collectionName = "A Night at the Opera",
          artworkUrl100 = "https://example.com/art.jpg",
          primaryGenreName = "Rock",
          trackTimeMillis = 354_000L,
          releaseDate = "1975-10-31T07:00:00Z",
      )

  @Test
  fun `maps every field of a complete dto`() {
    val track = fullDto.toTrack()

    assertEquals(
        Track(
            id = 42L,
            name = "Bohemian Rhapsody",
            artistName = "Queen",
            albumName = "A Night at the Opera",
            artworkUrl = "https://example.com/art.jpg",
            genre = "Rock",
            durationMillis = 354_000L,
            releaseDate = "1975-10-31T07:00:00Z",
        ),
        track,
    )
  }

  @Test
  fun `keeps optional fields null when the dto omits them`() {
    val track = ITunesTrackDto(trackId = 1L, trackName = "Song", artistName = "Artist").toTrack()

    assertEquals(Track(id = 1L, name = "Song", artistName = "Artist"), track)
  }

  @Test
  fun `drops a dto without track id`() {
    assertNull(fullDto.copy(trackId = null).toTrack())
  }

  @Test
  fun `drops a dto without track name`() {
    assertNull(fullDto.copy(trackName = null).toTrack())
  }

  @Test
  fun `drops a dto without artist name`() {
    assertNull(fullDto.copy(artistName = null).toTrack())
  }

  @Test
  fun `response mapping skips invalid results and keeps order`() {
    val response =
        ITunesSearchResponseDto(
            resultCount = 3,
            results = listOf(fullDto, fullDto.copy(trackId = null), fullDto.copy(trackId = 43L)),
        )

    assertEquals(listOf(42L, 43L), response.toTracks().map { it.id })
  }

  @Test
  fun `decodes an iTunes payload ignoring unknown fields`() {
    val payload =
        """
        {"resultCount":1,"results":[{"wrapperType":"track","kind":"song","trackId":7,
        "trackName":"Song","artistName":"Artist","collectionName":"Album",
        "artworkUrl100":"https://example.com/a.jpg","primaryGenreName":"Pop",
        "trackTimeMillis":180000,"releaseDate":"2020-01-01T08:00:00Z","country":"USA"}]}
        """
            .trimIndent()

    val response = Json {
      ignoreUnknownKeys = true
    }
        .decodeFromString<ITunesSearchResponseDto>(payload)

    assertEquals(
        Track(
            id = 7L,
            name = "Song",
            artistName = "Artist",
            albumName = "Album",
            artworkUrl = "https://example.com/a.jpg",
            genre = "Pop",
            durationMillis = 180_000L,
            releaseDate = "2020-01-01T08:00:00Z",
        ),
        response.toTracks().single(),
    )
  }
}
