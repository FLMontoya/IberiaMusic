package com.plexus.iberiamusic.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.presentation.R

/** Connects [SearchScreen] to its [SearchViewModel]. */
@Composable
fun SearchRoute(
    viewModel: SearchViewModel,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
  val query by viewModel.query.collectAsStateWithLifecycle()
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SearchScreen(
      query = query,
      uiState = uiState,
      onQueryChange = viewModel::onQueryChange,
      onTrackClick = onTrackClick,
      modifier = modifier,
  )
}

@Composable
fun SearchScreen(
    query: String,
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
  val keyboard = LocalSoftwareKeyboardController.current

  Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
    Text(
        text = stringResource(R.string.search_title),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.size(16.dp))
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.search_label)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.size(8.dp))
    SearchBody(uiState = uiState, onTrackClick = onTrackClick)
  }
}

@Composable
private fun SearchBody(uiState: SearchUiState, onTrackClick: (Track) -> Unit) {
  when (uiState) {
    SearchUiState.Idle -> StatusMessage(stringResource(R.string.search_idle_hint))
    SearchUiState.Loading -> LoadingMessage()
    SearchUiState.Empty ->
        StatusMessage(stringResource(R.string.search_empty), LiveRegionMode.Polite)
    SearchUiState.Error ->
        StatusMessage(
            text = stringResource(R.string.search_error),
            liveRegionMode = LiveRegionMode.Assertive,
            color = MaterialTheme.colorScheme.error,
        )
    is SearchUiState.Content ->
        LazyColumn(modifier = Modifier.fillMaxSize()) {
          items(uiState.tracks) { track -> TrackRow(track, onClick = { onTrackClick(track) }) }
        }
  }
}

@Composable
private fun StatusMessage(
    text: String,
    liveRegionMode: LiveRegionMode? = null,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
  Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge,
      color = color,
      modifier =
          Modifier.padding(vertical = 16.dp).semantics {
            if (liveRegionMode != null) liveRegion = liveRegionMode
          },
  )
}

@Composable
private fun LoadingMessage() {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
          Modifier.padding(vertical = 16.dp).semantics(mergeDescendants = true) {
            liveRegion = LiveRegionMode.Polite
          },
  ) {
    CircularProgressIndicator(modifier = Modifier.size(24.dp))
    Spacer(Modifier.width(12.dp))
    Text(text = stringResource(R.string.search_loading), style = MaterialTheme.typography.bodyLarge)
  }
}

@Composable
private fun TrackRow(track: Track, onClick: () -> Unit) {
  val subtitle =
      track.albumName?.let { stringResource(R.string.track_subtitle, track.artistName, it) }
          ?: track.artistName

  Column(
      verticalArrangement = Arrangement.Center,
      modifier =
          Modifier.fillMaxWidth()
              .heightIn(min = 48.dp)
              .semantics(mergeDescendants = true) {}
              .clickable(
                  onClickLabel = stringResource(R.string.track_open_detail),
                  role = Role.Button,
                  onClick = onClick,
              )
              .padding(vertical = 8.dp),
  ) {
    Text(text = track.name, style = MaterialTheme.typography.bodyLarge)
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
