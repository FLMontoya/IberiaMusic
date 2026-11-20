package com.plexus.iberiamusic

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plexus.iberiamusic.navigation.IberiaMusicApp
import com.plexus.iberiamusic.presentation.search.SearchViewModel

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // The UI is always dark, so the bars always get light icons whatever the system theme is.
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
    )
    val container = (application as IberiaMusicApplication).container
    setContent {
      val searchViewModel: SearchViewModel = viewModel(factory = container.searchViewModelFactory)
      IberiaMusicApp(searchViewModel)
    }
  }
}
