package com.plexus.iberiamusic

import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
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
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.domain.repository.SearchRepository
import com.plexus.iberiamusic.domain.usecase.SearchTracksUseCase
import com.plexus.iberiamusic.presentation.R
import com.plexus.iberiamusic.presentation.navigation.IberiaMusicApp
import com.plexus.iberiamusic.presentation.search.SearchViewModel
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Accessibility checks on the real app, running on a device: the automatic Accessibility Test
 * Framework checks (touch target size, contrast, labels) on every state, plus the semantics that
 * TalkBack, large fonts and keyboard navigation depend on.
 */
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
          releaseDate = LocalDate.of(1975, 10, 31),
      )

  @Before
  fun setUp() {
    rule.enableAccessibilityChecks()
  }

  private fun string(id: Int, vararg args: Any) = rule.activity.getString(id, *args)

  private fun show(result: SearchResult, fontScale: Float? = null) {
    coEvery { repository.search(any()) } returns result
    val viewModel = SearchViewModel(SearchTracksUseCase(repository))
    rule.setContent {
      val density = LocalDensity.current
      val scaled = fontScale?.let { Density(density.density, fontScale = it) } ?: density
      CompositionLocalProvider(LocalDensity provides scaled) {
        IberiaMusicApp(searchViewModel = viewModel)
      }
    }
  }

  private fun waitForText(text: String) {
    rule.waitUntil(WAIT_MILLIS) {
      rule.onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun searchFor(text: String, expectedOnScreen: String) {
    rule.onNode(hasSetTextAction()).performTextInput(text)
    waitForText(expectedOnScreen)
  }

  private fun openDetail() {
    searchFor("queen", track.name)
    rule.onNode(hasClickAction() and hasText(track.name)).performClick()
    waitForText(string(R.string.detail_back))
  }

  /** Sends a real key event to the window, like a hardware keyboard would. */
  private fun pressKey(keyCode: Int) {
    InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(keyCode)
    rule.waitForIdle()
  }

  private fun SemanticsNodeInteraction.isFocused(): Boolean =
      fetchSemanticsNode().config.getOrNull(SemanticsProperties.Focused) == true

  /** The touch area (not just the drawn size) must be at least 48dp x 48dp. */
  private fun SemanticsNodeInteraction.assertTouchTargetIsAtLeast48dp() {
    val density = rule.activity.resources.displayMetrics.density
    val bounds = fetchSemanticsNode().touchBoundsInRoot
    val minimumPx = MIN_TOUCH_TARGET_DP * density - TOLERANCE_PX
    check(bounds.width >= minimumPx && bounds.height >= minimumPx) {
      "Touch target is ${bounds.width / density} x ${bounds.height / density} dp, below 48 x 48"
    }
  }

  private fun liveRegion(mode: LiveRegionMode) =
      SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, mode)

  @Test
  fun idleStatePassesTheAccessibilityChecks() {
    show(SearchResult.Empty)

    rule.onNodeWithText(string(R.string.search_idle_hint)).assertIsDisplayed()
    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun resultsStatePassesTheAccessibilityChecks() {
    show(SearchResult.Success(listOf(track)))

    searchFor("queen", track.name)

    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun emptyStateIsAnnouncedPolitelyAndPassesTheChecks() {
    show(SearchResult.Empty)

    searchFor("zzzz", string(R.string.search_empty, "zzzz"))

    rule
        .onNodeWithText(string(R.string.search_empty, "zzzz"))
        .assert(liveRegion(LiveRegionMode.Polite))
    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun errorStateIsAnnouncedAssertivelyAndOffersARetryOfAtLeast48dp() {
    show(SearchResult.Error(IOException("offline")))

    searchFor("queen", string(R.string.search_error))

    rule.onNodeWithText(string(R.string.search_error)).assert(liveRegion(LiveRegionMode.Assertive))
    rule
        .onNode(hasClickAction() and hasText(string(R.string.search_retry)))
        .assertTouchTargetIsAtLeast48dp()
    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun searchScreenExposesATitleHeadingAndALabelledSearchField() {
    show(SearchResult.Empty)

    rule
        .onNode(
            hasText(string(R.string.search_title)) and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading),
        )
        .assertExists()
    rule.onNodeWithText(string(R.string.search_label)).assertIsDisplayed()
    rule.onNode(hasSetTextAction()).assert(hasImeAction(ImeAction.Search))
  }

  @Test
  fun eachResultRowIsASingleMergedElement() {
    show(SearchResult.Success(listOf(track)))

    searchFor("queen", track.name)

    rule
        .onNode(
            hasClickAction() and
                hasText(track.name) and
                hasText(string(R.string.track_subtitle, "Queen", "A Night at the Opera")),
        )
        .assertExists()
    rule.onAllNodes(hasClickAction() and hasText(track.name)).assertCountEquals(1)
  }

  @Test
  fun clearButtonHasADescriptionAndAtLeast48dp() {
    show(SearchResult.Empty)

    rule.onNode(hasSetTextAction()).performTextInput("queen")

    rule
        .onNode(hasClickAction() and hasContentDescription(string(R.string.search_clear)))
        .assertTouchTargetIsAtLeast48dp()
  }

  @Test
  fun detailScreenPassesTheChecksAndGroupsLabelAndValue() {
    show(SearchResult.Success(listOf(track)))

    openDetail()

    rule
        .onNode(hasText(track.name) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        .assertExists()
    rule.onNode(hasText(string(R.string.detail_artist)) and hasText("Queen")).assertExists()
    rule
        .onNode(hasText(string(R.string.detail_album)) and hasText("A Night at the Opera"))
        .assertExists()
    rule
        .onNode(hasClickAction() and hasText(string(R.string.detail_back)))
        .assertTouchTargetIsAtLeast48dp()
    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun loadingStateIsAnnouncedPolitelyAndPassesTheChecks() {
    show(SearchResult.Empty)
    // Re-stubbed so the search never completes and the screen stays in the loading state.
    coEvery { repository.search(any()) } coAnswers { CompletableDeferred<SearchResult>().await() }

    rule.onNode(hasSetTextAction()).performTextInput("queen")
    waitForText(string(R.string.search_loading))

    rule.onNodeWithText(string(R.string.search_loading)).assert(liveRegion(LiveRegionMode.Polite))
    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun detailGroupsEveryFieldAndShowsADashForTheMissingOnes() {
    val sparse = Track(id = 2L, name = "Heroes", artistName = "Bowie")
    show(SearchResult.Success(listOf(sparse)))
    searchFor("bowie", sparse.name)
    rule.onNode(hasClickAction() and hasText(sparse.name)).performClick()
    waitForText(string(R.string.detail_back))

    val dash = string(R.string.detail_value_missing)
    rule.onNode(hasText(string(R.string.detail_artist)) and hasText("Bowie")).assertExists()
    rule.onNode(hasText(string(R.string.detail_album)) and hasText(dash)).assertExists()
    rule.onNode(hasText(string(R.string.detail_genre)) and hasText(dash)).assertExists()
    rule.onNode(hasText(string(R.string.detail_duration)) and hasText(dash)).assertExists()
    rule.onNode(hasText(string(R.string.detail_release_date)) and hasText(dash)).assertExists()
    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun contentStaysReachableWithTheFontAt200Percent() {
    show(SearchResult.Success(listOf(track)), fontScale = 2f)

    rule.onNodeWithText(string(R.string.search_title)).assertIsDisplayed()
    searchFor("queen", track.name)
    rule.onNode(hasClickAction() and hasText(track.name)).assertIsDisplayed()
    rule.onRoot().tryPerformAccessibilityChecks()
  }

  @Test
  fun keyboardCanReachAResultAndOpenItsDetail() {
    show(SearchResult.Success(listOf(track)))
    searchFor("queen", track.name)
    val row = rule.onNode(hasClickAction() and hasText(track.name))

    rule.onNode(hasSetTextAction()).requestFocus()
    var presses = 0
    while (!row.isFocused() && presses < MAX_TAB_PRESSES) {
      pressKey(KeyEvent.KEYCODE_TAB)
      presses++
    }
    check(row.isFocused()) { "The result row was not reachable with the Tab key" }

    pressKey(KeyEvent.KEYCODE_ENTER)

    waitForText(string(R.string.detail_back))
  }

  private companion object {
    const val WAIT_MILLIS = 5_000L
    const val MAX_TAB_PRESSES = 6
    const val MIN_TOUCH_TARGET_DP = 48f
    const val TOLERANCE_PX = 1f
  }
}
