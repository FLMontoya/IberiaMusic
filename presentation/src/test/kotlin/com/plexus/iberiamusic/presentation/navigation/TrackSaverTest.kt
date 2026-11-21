package com.plexus.iberiamusic.presentation.navigation

import com.plexus.iberiamusic.domain.model.Track
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class TrackSaverTest {

  @Test
  fun `a track with every field survives a save and restore`() {
    val track =
        Track(
            id = 1L,
            name = "Bohemian Rhapsody",
            artistName = "Queen",
            albumName = "A Night at the Opera",
            artworkUrl = "https://example.com/art.jpg",
            largeArtworkUrl = "https://example.com/art-large.jpg",
            genre = "Rock",
            durationMillis = 354_000L,
            releaseDate = LocalDate.of(1975, 10, 31),
        )

    assertEquals(track, restoreTrack(saveTrack(track)))
  }

  @Test
  fun `a track with only the required fields survives a save and restore`() {
    val track = Track(id = 2L, name = "Song", artistName = "Artist")

    assertEquals(track, restoreTrack(saveTrack(track)))
  }

  @Test
  fun `no selection is saved as nothing and restored as no selection`() {
    assertNull(restoreTrack(saveTrack(null)))
  }

  @Test
  fun `a saved state with a wrong type in a required field is restored as no selection`() {
    val saved = saveTrack(Track(id = 3L, name = "Song", artistName = "Artist")).toMutableList()
    saved[0] = "not a number"

    assertNull(restoreTrack(saved))
  }

  @Test
  fun `a saved state with a missing required field is restored as no selection`() {
    assertNull(restoreTrack(listOf(4L, "Song")))
  }

  @Test
  fun `a wrong type in an optional field is dropped and the track is still restored`() {
    val track = Track(id = 5L, name = "Song", artistName = "Artist", genre = "Rock")
    val saved = saveTrack(track).toMutableList()
    saved[6] = "not a number"

    assertEquals(track, restoreTrack(saved))
  }

  @Test
  fun `a release date saved with the wrong type is dropped and the track is still restored`() {
    val track = Track(id = 6L, name = "Song", artistName = "Artist")
    val saved = saveTrack(track.copy(releaseDate = LocalDate.of(2020, 1, 1))).toMutableList()
    saved[7] = "2020-01-01"

    assertEquals(track, restoreTrack(saved))
  }

  @Test
  fun `an impossible release date is dropped instead of crashing the restore`() {
    val track = Track(id = 7L, name = "Song", artistName = "Artist")
    val saved = saveTrack(track.copy(releaseDate = LocalDate.of(2020, 1, 1))).toMutableList()
    saved[7] = Long.MAX_VALUE

    assertEquals(track, restoreTrack(saved))
  }
}
