package com.plexus.iberiamusic.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.domain.repository.SearchRepository
import com.plexus.iberiamusic.domain.usecase.SearchTracksUseCase
import com.plexus.iberiamusic.presentation.R
import com.plexus.iberiamusic.presentation.search.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class IberiaMusicAppTest {

  @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

  private val dispatcher = StandardTestDispatcher()
  private val track =
      Track(id = 1L, name = "Bohemian Rhapsody", artistName = "Queen", genre = "Rock")

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun string(id: Int) = rule.activity.getString(id)

  private fun settle() {
    dispatcher.scheduler.advanceTimeBy(1_000)
    dispatcher.scheduler.runCurrent()
    rule.waitForIdle()
  }

  private fun showApp() {
    val repository =
        object : SearchRepository {
          override suspend fun search(query: String): SearchResult =
              SearchResult.Success(listOf(track))
        }
    val viewModel = SearchViewModel(SearchTracksUseCase(repository))
    rule.setContent { IberiaMusicApp(searchViewModel = viewModel) }
    settle()
  }

  private fun searchAndOpenTrack() {
    rule.onNode(hasSetTextAction()).performTextInput("queen")
    settle()
    rule.onNode(hasClickAction() and hasText(track.name)).performClick()
    settle()
  }

  @Test
  fun `starts on the search screen`() {
    showApp()

    rule.onNodeWithText(string(R.string.search_title)).assertIsDisplayed()
  }

  @Test
  fun `opening a result shows its detail`() {
    showApp()

    searchAndOpenTrack()

    rule.onNode(hasText(string(R.string.detail_genre)) and hasText("Rock")).assertIsDisplayed()
  }

  @Test
  fun `back from the detail returns to the search results`() {
    showApp()
    searchAndOpenTrack()

    rule.onNode(hasClickAction() and hasText(string(R.string.detail_back))).performClick()
    settle()

    rule.onNode(hasClickAction() and hasText(track.name)).assertIsDisplayed()
  }
}
