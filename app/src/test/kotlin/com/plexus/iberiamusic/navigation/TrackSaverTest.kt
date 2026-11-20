package com.plexus.iberiamusic.navigation

import com.plexus.iberiamusic.domain.model.Track
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
            genre = "Rock",
            durationMillis = 354_000L,
            releaseDate = "1975-10-31T07:00:00Z",
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
}
