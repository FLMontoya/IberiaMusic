package com.plexus.iberiamusic.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.presentation.detail.DetailScreen
import com.plexus.iberiamusic.presentation.search.SearchRoute
import com.plexus.iberiamusic.presentation.search.SearchViewModel
import com.plexus.iberiamusic.presentation.theme.IberiaMusicTheme

private const val SEARCH_ROUTE = "search"
private const val DETAIL_ROUTE = "detail"

/** App-level navigation: search results lead to the detail of the chosen track. */
@Composable
fun IberiaMusicApp(searchViewModel: SearchViewModel) {
  IberiaMusicTheme {
    val navController = rememberNavController()
    var selectedTrack by rememberSaveable(stateSaver = TrackSaver) { mutableStateOf<Track?>(null) }

    Scaffold { innerPadding ->
      NavHost(
          navController = navController,
          startDestination = SEARCH_ROUTE,
          modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding).imePadding(),
      ) {
        composable(SEARCH_ROUTE) {
          SearchRoute(
              viewModel = searchViewModel,
              onTrackClick = { track ->
                selectedTrack = track
                // singleTop: a double tap must not stack two detail screens.
                navController.navigate(DETAIL_ROUTE) { launchSingleTop = true }
              },
          )
        }
        composable(DETAIL_ROUTE) {
          val track = selectedTrack
          if (track == null) {
            LaunchedEffect(Unit) { navController.popBackStack() }
          } else {
            DetailScreen(track = track, onBack = { navController.popBackStack() })
          }
        }
      }
    }
  }
}
