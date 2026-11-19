package com.plexus.iberiamusic.presentation.search

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasImeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.presentation.R
import com.plexus.iberiamusic.presentation.theme.IberiaMusicTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchScreenTest {

  @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

  private val track =
      Track(id = 1L, name = "Bohemian Rhapsody", artistName = "Queen", albumName = "A Night")

  private fun string(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

  private fun show(
      state: SearchUiState = SearchUiState.Idle,
      query: String = "",
      fontScale: Float? = null,
      onQueryChange: (String) -> Unit = {},
      onTrackClick: (Track) -> Unit = {},
  ) {
    rule.setContent {
      IberiaMusicTheme {
        val density = LocalDensity.current
        val scaled = fontScale?.let { Density(density.density, fontScale = it) } ?: density
        CompositionLocalProvider(LocalDensity provides scaled) {
          SearchScreen(
              query = query,
              uiState = state,
              onQueryChange = onQueryChange,
              onTrackClick = onTrackClick,
          )
        }
      }
    }
  }

  @Test
  fun `title is exposed as a heading`() {
    show()

    rule
        .onNode(
            hasText(string(R.string.search_title)) and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading),
        )
        .assertExists()
  }

  @Test
  fun `text field shows a visible label and uses the Search ime action`() {
    show()

    rule.onNodeWithText(string(R.string.search_label)).assertIsDisplayed()
    rule.onNode(hasSetTextAction()).assert(hasImeAction(ImeAction.Search))
  }

  @Test
  fun `typing notifies the new query`() {
    var received = ""
    show(onQueryChange = { received = it })

    rule.onNode(hasSetTextAction()).performTextInput("queen")

    assertEquals("queen", received)
  }

  @Test
  fun `idle state shows the hint`() {
    show(SearchUiState.Idle)

    rule.onNodeWithText(string(R.string.search_idle_hint)).assertIsDisplayed()
  }

  @Test
  fun `loading state is announced politely`() {
    show(SearchUiState.Loading)

    rule
        .onNodeWithText(string(R.string.search_loading))
        .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
  }

  @Test
  fun `empty state is announced politely`() {
    show(SearchUiState.Empty)

    rule
        .onNodeWithText(string(R.string.search_empty))
        .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
  }

  @Test
  fun `error state is announced assertively`() {
    show(SearchUiState.Error)

    rule
        .onNodeWithText(string(R.string.search_error))
        .assert(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive),
        )
  }

  @Test
  fun `each result row is a single merged element with all its text`() {
    show(SearchUiState.Content(listOf(track)))

    rule
        .onNode(
            hasClickAction() and
                hasText("Bohemian Rhapsody") and
                hasText(string(R.string.track_subtitle, "Queen", "A Night")),
        )
        .assertExists()
    rule.onAllNodes(hasClickAction() and hasText("Bohemian Rhapsody")).assertCountEquals(1)
  }

  @Test
  fun `result row meets the minimum touch target`() {
    show(SearchUiState.Content(listOf(track)))

    rule
        .onNode(hasClickAction() and hasText("Bohemian Rhapsody"))
        .assertHeightIsAtLeast(48.dp)
        .assertWidthIsAtLeast(48.dp)
  }

  @Test
  fun `clicking a row reports the track`() {
    var clicked: Track? = null
    show(SearchUiState.Content(listOf(track)), onTrackClick = { clicked = it })

    rule.onNode(hasClickAction() and hasText("Bohemian Rhapsody")).performClick()

    assertEquals(track, clicked)
  }

  @Test
  fun `content stays visible with the font scaled to 200 percent`() {
    show(SearchUiState.Content(listOf(track)), fontScale = 2f)

    rule.onNodeWithText(string(R.string.search_label)).assertIsDisplayed()
    rule.onNode(hasClickAction() and hasText("Bohemian Rhapsody")).assertIsDisplayed()
  }
}
