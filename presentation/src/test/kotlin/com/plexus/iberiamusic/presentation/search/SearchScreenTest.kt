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
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertTouchWidthIsEqualTo
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasImeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.plexus.iberiamusic.domain.model.SearchFailure
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
      onSearch: () -> Unit = {},
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
              onSearch = onSearch,
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
    val onQueryChange = mockk<(String) -> Unit>(relaxed = true)
    show(onQueryChange = onQueryChange)

    rule.onNode(hasSetTextAction()).performTextInput("queen")

    verify(exactly = 1) { onQueryChange("queen") }
    confirmVerified(onQueryChange)
  }

  @Test
  fun `the keyboard search action reports a search`() {
    val onSearch = mockk<() -> Unit>(relaxed = true)
    show(query = "queen", onSearch = onSearch)

    rule.onNode(hasSetTextAction()).performImeAction()

    verify(exactly = 1) { onSearch() }
    confirmVerified(onSearch)
  }

  @Test
  fun `there is no clear button while the query is empty`() {
    show(query = "")

    rule.onNode(hasContentDescription(string(R.string.search_clear))).assertDoesNotExist()
  }

  @Test
  fun `clear button empties the query and meets the minimum touch target`() {
    val onQueryChange = mockk<(String) -> Unit>(relaxed = true)
    show(query = "queen", onQueryChange = onQueryChange)

    val clear =
        rule.onNode(hasClickAction() and hasContentDescription(string(R.string.search_clear)))
    clear.assertTouchHeightIsEqualTo(48.dp).assertTouchWidthIsEqualTo(48.dp)
    clear.performClick()

    verify(exactly = 1) { onQueryChange("") }
    confirmVerified(onQueryChange)
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
    show(SearchUiState.Empty, query = "zzzz")

    rule
        .onNodeWithText(string(R.string.search_empty, "zzzz"))
        .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
  }

  @Test
  fun `error state is announced assertively`() {
    show(SearchUiState.Error(SearchFailure.NETWORK))

    rule
        .onNodeWithText(string(R.string.search_error_network))
        .assert(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive),
        )
  }

  @Test
  fun `a network failure tells the user to check the connection`() {
    show(SearchUiState.Error(SearchFailure.NETWORK))

    rule.onNodeWithText(string(R.string.search_error_network)).assertIsDisplayed()
    rule.onNodeWithText(string(R.string.search_error_server)).assertDoesNotExist()
  }

  @Test
  fun `a server failure blames the service and not the connection`() {
    show(SearchUiState.Error(SearchFailure.SERVER))

    rule.onNodeWithText(string(R.string.search_error_server)).assertIsDisplayed()
    rule.onNodeWithText(string(R.string.search_error_network)).assertDoesNotExist()
  }

  @Test
  fun `an unknown failure gives a generic message`() {
    show(SearchUiState.Error(SearchFailure.UNKNOWN))

    rule.onNodeWithText(string(R.string.search_error_unknown)).assertIsDisplayed()
  }

  @Test
  fun `every kind of failure still offers to retry`() {
    show(SearchUiState.Error(SearchFailure.SERVER))

    rule.onNode(hasClickAction() and hasText(string(R.string.search_retry))).assertIsDisplayed()
  }

  @Test
  fun `retry button is shown in the error state`() {
    show(SearchUiState.Error(SearchFailure.NETWORK))

    rule.onNode(hasClickAction() and hasText(string(R.string.search_retry))).assertIsDisplayed()
  }

  @Test
  fun `no retry button outside the error state`() {
    show(SearchUiState.Idle)

    rule.onNode(hasText(string(R.string.search_retry))).assertDoesNotExist()
  }

  @Test
  fun `retry button reports a search and meets the minimum touch target`() {
    val onSearch = mockk<() -> Unit>(relaxed = true)
    show(SearchUiState.Error(SearchFailure.NETWORK), query = "queen", onSearch = onSearch)

    val retry = rule.onNode(hasClickAction() and hasText(string(R.string.search_retry)))
    retry.assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
    retry.performClick()

    verify(exactly = 1) { onSearch() }
    confirmVerified(onSearch)
  }

  @Test
  fun `error message stays an assertive live region next to the retry button`() {
    show(SearchUiState.Error(SearchFailure.NETWORK))

    rule
        .onNodeWithText(string(R.string.search_error_network))
        .assert(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Assertive),
        )
    rule.onNode(hasText(string(R.string.search_retry))).assertIsDisplayed()
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
  fun `artwork is decorative so the row has no content description of its own`() {
    val withArtwork = track.copy(artworkUrl = "https://example.com/100x100bb.jpg")
    show(SearchUiState.Content(listOf(withArtwork)))

    rule.onAllNodes(hasClickAction() and hasText("Bohemian Rhapsody")).assertCountEquals(1)
    rule
        .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription))
        .assertCountEquals(0)
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
    val onTrackClick = mockk<(Track) -> Unit>(relaxed = true)
    show(SearchUiState.Content(listOf(track)), onTrackClick = onTrackClick)

    rule.onNode(hasClickAction() and hasText("Bohemian Rhapsody")).performClick()

    verify(exactly = 1) { onTrackClick(track) }
    confirmVerified(onTrackClick)
  }

  @Test
  fun `content stays visible with the font scaled to 200 percent`() {
    show(SearchUiState.Content(listOf(track)), fontScale = 2f)

    rule.onNodeWithText(string(R.string.search_label)).assertIsDisplayed()
    rule.onNode(hasClickAction() and hasText("Bohemian Rhapsody")).assertIsDisplayed()
  }
}
