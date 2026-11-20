package com.plexus.iberiamusic.accessibility

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTouchHeightIsEqualTo
import androidx.compose.ui.test.assertTouchWidthIsEqualTo
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasImeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.domain.repository.SearchRepository
import com.plexus.iberiamusic.domain.usecase.SearchTracksUseCase
import com.plexus.iberiamusic.navigation.IberiaMusicApp
import com.plexus.iberiamusic.presentation.R
import com.plexus.iberiamusic.presentation.search.SearchViewModel
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs the whole app on a device and checks the accessibility checklist of the project: the
 * automatic Accessibility Test Framework checks (touch targets, contrast, labels) on every state of
 * both screens, plus the semantics TalkBack relies on (headings, grouping, live regions,
 * descriptions), the 200% font scale and keyboard navigation.
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityInstrumentedTest {

  @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

  private val repository: SearchRepository = mockk()

  private val track =
      Track(
          id = 1L,
          name = "Bohemian Rhapsody",
          artistName = "Queen",
          albumName = "A Night at the Opera",
          genre = "Rock",
          durationMillis = 354_000L,
          releaseDate = "1975-10-31T07:00:00Z",
      )

  @Before
  fun setUp() {
    rule.enableAccessibilityChecks()
  }

  private fun string(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

  private fun launch(fontScale: Float? = null) {
    val viewModel = SearchViewModel(SearchTracksUseCase(repository))
    rule.setContent {
      val density = LocalDensity.current
      val scaled = fontScale?.let { Density(density.density, fontScale = it) } ?: density
      CompositionLocalProvider(LocalDensity provides scaled) { IberiaMusicApp(viewModel) }
    }
  }

  private fun type(text: String) {
    rule.onNode(hasSetTextAction()).performTextInput(text)
  }

  private fun waitForText(text: String) {
    rule.waitUntil(timeoutMillis = WAIT_MILLIS) {
      rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun rowMatcher() = hasClickAction() and hasText(track.name)

  private fun showResults() {
    coEvery { repository.search(any()) } returns SearchResult.Success(listOf(track))
    launch()
    type("queen")
    waitForText(track.name)
  }

  private fun openDetail() {
    showResults()
    rule.onNode(rowMatcher()).performClick()
    waitForText(string(R.string.detail_back))
  }

  private fun liveRegion(mode: LiveRegionMode) =
      SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, mode)

  private val heading = SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

  // --- Automatic Accessibility Test Framework checks on every state ---------------------------

  @Test
  fun idleSearchScreenPassesAccessibilityChecks() {
    launch()

    rule.onNodeWithText(string(R.string.search_idle_hint)).assertIsDisplayed()
    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun loadingStatePassesAccessibilityChecks() {
    coEvery { repository.search(any()) } coAnswers { CompletableDeferred<SearchResult>().await() }
    launch()
    type("queen")

    waitForText(string(R.string.search_loading))
    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun resultsStatePassesAccessibilityChecks() {
    showResults()

    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun emptyStatePassesAccessibilityChecks() {
    coEvery { repository.search(any()) } returns SearchResult.Empty
    launch()
    type("zzzz")

    waitForText(string(R.string.search_empty, "zzzz"))
    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun errorStatePassesAccessibilityChecks() {
    coEvery { repository.search(any()) } returns SearchResult.Error(IOException("offline"))
    launch()
    type("queen")

    waitForText(string(R.string.search_error))
    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun detailScreenPassesAccessibilityChecks() {
    openDetail()

    rule.onRoot().tryPerformAccessibilityChecks()
  }

  // --- Semantics that TalkBack relies on ---------------------------------------------------------

  @Test
  fun searchTitleIsAHeading() {
    launch()

    rule.onNode(hasText(string(R.string.search_title)) and heading).assertExists()
  }

  @Test
  fun searchFieldHasAVisibleLabelAndTheSearchImeAction() {
    launch()

    rule.onNodeWithText(string(R.string.search_label)).assertIsDisplayed()
    rule.onNode(hasSetTextAction()).assert(hasImeAction(ImeAction.Search))
  }

  @Test
  fun eachResultRowIsOneMergedElementWithAllItsText() {
    showResults()

    rule
        .onNode(
            rowMatcher() and
                hasText(string(R.string.track_subtitle, "Queen", "A Night at the Opera")),
        )
        .assertExists()
  }

  @Test
  fun clearButtonIsDescribedAndMeetsTheMinimumTouchTarget() {
    showResults()

    rule
        .onNode(hasClickAction() and hasContentDescription(string(R.string.search_clear)))
        .assertTouchHeightIsEqualTo(48.dp)
        .assertTouchWidthIsEqualTo(48.dp)
  }

  @Test
  fun emptyStateIsAnnouncedPolitely() {
    coEvery { repository.search(any()) } returns SearchResult.Empty
    launch()
    type("zzzz")

    waitForText(string(R.string.search_empty, "zzzz"))
    rule
        .onNodeWithText(string(R.string.search_empty, "zzzz"))
        .assert(liveRegion(LiveRegionMode.Polite))
  }

  @Test
  fun errorIsAnnouncedAssertivelyAndOffersARetryWithAMinimumTouchTarget() {
    coEvery { repository.search(any()) } returns SearchResult.Error(IOException("offline"))
    launch()
    type("queen")

    waitForText(string(R.string.search_error))
    rule.onNodeWithText(string(R.string.search_error)).assert(liveRegion(LiveRegionMode.Assertive))
    // Material draws the text button 40dp tall but extends its touch area to 48dp.
    rule
        .onNode(hasClickAction() and hasText(string(R.string.search_retry)))
        .assertTouchHeightIsEqualTo(48.dp)
        .assertWidthIsAtLeast(48.dp)
  }

  @Test
  fun detailTitleIsAHeadingAndEachFieldIsOneMergedElement() {
    openDetail()

    rule.onNode(hasText(track.name) and heading).assertExists()
    rule.onNode(hasText(string(R.string.detail_artist)) and hasText("Queen")).assertExists()
    rule
        .onNode(hasText(string(R.string.detail_album)) and hasText("A Night at the Opera"))
        .assertExists()
    rule.onNode(hasText(string(R.string.detail_genre)) and hasText("Rock")).assertExists()
    rule.onNode(hasText(string(R.string.detail_duration)) and hasText("5:54")).assertExists()
  }

  @Test
  fun detailBackButtonMeetsTheMinimumTouchTarget() {
    openDetail()

    // Material draws the text button 40dp tall but extends its touch area to 48dp.
    rule
        .onNode(hasClickAction() and hasText(string(R.string.detail_back)))
        .assertTouchHeightIsEqualTo(48.dp)
        .assertWidthIsAtLeast(48.dp)
  }

  // --- Font scale and keyboard
  // ---------------------------------------------------------------------

  @Test
  fun contentStaysReachableWithTheFontAt200Percent() {
    coEvery { repository.search(any()) } returns SearchResult.Success(listOf(track))
    launch(fontScale = 2f)
    type("queen")
    waitForText(track.name)

    rule.onNodeWithText(string(R.string.search_title)).assertIsDisplayed()
    rule.onNode(rowMatcher()).assertIsDisplayed()
    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun resultRowIsFocusableAndEnterOpensItsDetail() {
    showResults()

    // A row that can take focus and be activated with Enter is operable from a keyboard.
    rule
        .onNode(rowMatcher())
        .assert(SemanticsMatcher.keyIsDefined(SemanticsActions.RequestFocus))
        .requestFocus()
        .assertIsFocused()
    rule.onNode(rowMatcher()).performKeyInput { pressKey(Key.Enter) }

    waitForText(string(R.string.detail_back))
    rule.onNode(hasText(track.name) and heading).assertExists()
  }

  private companion object {
    const val WAIT_MILLIS = 5_000L
  }
}
