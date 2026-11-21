package com.plexus.iberiamusic.presentation.detail

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.presentation.R
import com.plexus.iberiamusic.presentation.theme.IberiaMusicTheme
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DetailScreenTest {

  @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

  private val fullTrack =
      Track(
          id = 1L,
          name = "Bohemian Rhapsody",
          artistName = "Queen",
          albumName = "A Night at the Opera",
          genre = "Rock",
          durationMillis = 354_000L,
          releaseDate = "1975-10-31T07:00:00Z",
      )

  private fun string(id: Int) = rule.activity.getString(id)

  private fun show(track: Track, fontScale: Float? = null, onBack: () -> Unit = {}) {
    rule.setContent {
      IberiaMusicTheme {
        val density = LocalDensity.current
        val scaled = fontScale?.let { Density(density.density, fontScale = it) } ?: density
        CompositionLocalProvider(LocalDensity provides scaled) {
          DetailScreen(track = track, onBack = onBack)
        }
      }
    }
  }

  private fun field(labelRes: Int, value: String) = hasText(string(labelRes)) and hasText(value)

  @Test
  fun `track name is exposed as a heading`() {
    show(fullTrack)

    rule
        .onNode(
            hasText("Bohemian Rhapsody") and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading),
        )
        .assertExists()
  }

  @Test
  fun `shows every field as one element joining its label and value`() {
    show(fullTrack)

    rule.onNode(field(R.string.detail_artist, "Queen")).assertExists()
    rule.onNode(field(R.string.detail_album, "A Night at the Opera")).assertExists()
    rule.onNode(field(R.string.detail_genre, "Rock")).assertExists()
    rule.onNode(field(R.string.detail_duration, "5:54")).assertExists()
    rule
        .onNode(field(R.string.detail_release_date, formatReleaseDate("1975-10-31T07:00:00Z")))
        .assertExists()
  }

  @Test
  fun `shows a dash for the fields the track does not have`() {
    show(fullTrack.copy(albumName = null, genre = null))
    val dash = string(R.string.detail_value_missing)

    rule.onNode(field(R.string.detail_artist, "Queen")).assertExists()
    rule.onNode(field(R.string.detail_album, dash)).assertExists()
    rule.onNode(field(R.string.detail_genre, dash)).assertExists()
    rule.onNode(field(R.string.detail_duration, "5:54")).assertExists()
  }

  @Test
  fun `shows a dash in every optional field when all are missing`() {
    show(Track(id = 2L, name = "Song", artistName = "Artist"))
    val dash = string(R.string.detail_value_missing)

    rule.onNode(field(R.string.detail_artist, "Artist")).assertExists()
    rule.onNode(field(R.string.detail_album, dash)).assertExists()
    rule.onNode(field(R.string.detail_genre, dash)).assertExists()
    rule.onNode(field(R.string.detail_duration, dash)).assertExists()
    rule.onNode(field(R.string.detail_release_date, dash)).assertExists()
    rule.onAllNodes(hasText(dash)).assertCountEquals(4)
  }

  @Test
  fun `artwork is described with the track name`() {
    show(
        fullTrack.copy(
            artworkUrl = "https://example.com/100x100bb.jpg",
            largeArtworkUrl = "https://example.com/600x600bb.jpg",
        ),
    )

    rule
        .onNode(
            hasContentDescription(
                rule.activity.getString(R.string.detail_artwork, "Bohemian Rhapsody"),
            ),
        )
        .assertExists()
  }

  @Test
  fun `artwork still shows when the track only has the thumbnail`() {
    show(fullTrack.copy(artworkUrl = "https://example.com/100x100bb.jpg"))

    rule
        .onNode(
            hasContentDescription(
                rule.activity.getString(R.string.detail_artwork, "Bohemian Rhapsody"),
            ),
        )
        .assertExists()
  }

  @Test
  fun `no artwork element when the track has no artwork`() {
    show(fullTrack)

    rule
        .onNode(
            hasContentDescription(
                rule.activity.getString(R.string.detail_artwork, "Bohemian Rhapsody"),
            ),
        )
        .assertDoesNotExist()
  }

  @Test
  fun `back button reports the click`() {
    val onBack = mockk<() -> Unit>(relaxed = true)
    show(fullTrack, onBack = onBack)

    rule.onNode(hasClickAction() and hasText(string(R.string.detail_back))).performClick()

    verify(exactly = 1) { onBack() }
    confirmVerified(onBack)
  }

  @Test
  fun `back button meets the minimum touch target`() {
    show(fullTrack)

    rule
        .onNode(hasClickAction() and hasText(string(R.string.detail_back)))
        .assertHeightIsAtLeast(48.dp)
        .assertWidthIsAtLeast(48.dp)
  }

  @Test
  fun `content stays reachable with the font scaled to 200 percent`() {
    show(fullTrack, fontScale = 2f)

    rule.onNodeWithText("Bohemian Rhapsody").assertIsDisplayed()
    rule.onNode(hasClickAction() and hasText(string(R.string.detail_back))).assertIsDisplayed()
  }
}
