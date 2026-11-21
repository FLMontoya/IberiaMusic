package com.plexus.iberiamusic.presentation.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import com.plexus.iberiamusic.domain.model.SearchResult
import com.plexus.iberiamusic.domain.repository.SearchRepository
import com.plexus.iberiamusic.domain.usecase.SearchTracksUseCase
import com.plexus.iberiamusic.presentation.search.SearchViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w1200dp-h800dp")
class IberiaMusicAppWideScreenTest {

  @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun `content keeps a readable width and stays centered on wide screens`() {
    val repository: SearchRepository = mockk()
    coEvery { repository.search(any()) } returns SearchResult.Empty
    val viewModel = SearchViewModel(SearchTracksUseCase(repository))
    rule.setContent { IberiaMusicApp(viewModel) }

    val bounds = rule.onNode(hasSetTextAction()).getUnclippedBoundsInRoot()

    assertTrue(bounds.width <= 840.dp, "field is ${bounds.width} wide")
    assertTrue(bounds.left > 100.dp, "field starts at ${bounds.left}")
  }
}
