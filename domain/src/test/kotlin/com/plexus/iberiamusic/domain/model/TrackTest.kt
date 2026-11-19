package com.plexus.iberiamusic.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrackTest {

  @Test
  fun `optional fields default to null`() {
    val track = Track(id = 1L, name = "Song", artistName = "Artist")

    assertNull(track.albumName)
    assertNull(track.artworkUrl)
    assertNull(track.genre)
    assertNull(track.durationMillis)
    assertNull(track.releaseDate)
  }

  @Test
  fun `tracks with the same values are equal`() {
    val first = Track(id = 1L, name = "Song", artistName = "Artist", albumName = "Album")
    val second = Track(id = 1L, name = "Song", artistName = "Artist", albumName = "Album")

    assertEquals(first, second)
  }
}
