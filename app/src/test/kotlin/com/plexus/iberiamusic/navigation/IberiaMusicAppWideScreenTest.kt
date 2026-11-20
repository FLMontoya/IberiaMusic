package com.plexus.iberiamusic.navigation

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
import org.junit.Assert.assertTrue
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
    val repository =
        object : SearchRepository {
          override suspend fun search(query: String): SearchResult = SearchResult.Empty
        }
    rule.setContent { IberiaMusicApp(SearchViewModel(SearchTracksUseCase(repository))) }

    val bounds = rule.onNode(hasSetTextAction()).getUnclippedBoundsInRoot()

    assertTrue("field is ${bounds.width} wide", bounds.width <= 840.dp)
    assertTrue("field starts at ${bounds.left}", bounds.left > 100.dp)
  }
}
