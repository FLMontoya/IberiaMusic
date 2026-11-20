package com.plexus.iberiamusic.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import coil3.compose.AsyncImage
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.presentation.R
import com.plexus.iberiamusic.presentation.theme.SearchFieldContainer
import com.plexus.iberiamusic.presentation.theme.SearchFieldLabel
import com.plexus.iberiamusic.presentation.theme.SearchFieldText
import com.plexus.iberiamusic.presentation.theme.SpotifyGreen

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
      onSearch = viewModel::onSearch,
      onTrackClick = onTrackClick,
      modifier = modifier,
  )
}

@Composable
fun SearchScreen(
    query: String,
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
  val keyboard = LocalSoftwareKeyboardController.current
  val interactionSource = remember { MutableInteractionSource() }
  val focused by interactionSource.collectIsFocusedAsState()

  Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
    Text(
        text = stringResource(R.string.search_title),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.size(16.dp))
    TextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.search_label)) },
        singleLine = true,
        shape = CircleShape,
        colors = searchFieldColors(),
        interactionSource = interactionSource,
        // The search icon is decorative: the label already says what the field is for.
        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
          if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
              Icon(
                  imageVector = Icons.Filled.Clear,
                  contentDescription = stringResource(R.string.search_clear),
              )
            }
          }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions =
            KeyboardActions(
                onSearch = {
                  onSearch()
                  keyboard?.hide()
                },
            ),
        // The green ring is the visible keyboard/touch focus indicator on the white pill.
        modifier =
            Modifier.fillMaxWidth().border(if (focused) 3.dp else 0.dp, SpotifyGreen, CircleShape),
    )
    Spacer(Modifier.size(8.dp))
    SearchBody(uiState = uiState, query = query, onTrackClick = onTrackClick, onRetry = onSearch)
  }
}

/** White pill with dark text; the indicator line is off because the focus ring replaces it. */
@Composable
private fun searchFieldColors() =
    TextFieldDefaults.colors(
        focusedContainerColor = SearchFieldContainer,
        unfocusedContainerColor = SearchFieldContainer,
        focusedTextColor = SearchFieldText,
        unfocusedTextColor = SearchFieldText,
        cursorColor = SearchFieldText,
        focusedLabelColor = SearchFieldLabel,
        unfocusedLabelColor = SearchFieldLabel,
        focusedLeadingIconColor = SearchFieldText,
        unfocusedLeadingIconColor = SearchFieldText,
        focusedTrailingIconColor = SearchFieldText,
        unfocusedTrailingIconColor = SearchFieldText,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
    )

@Composable
private fun SearchBody(
    uiState: SearchUiState,
    query: String,
    onTrackClick: (Track) -> Unit,
    onRetry: () -> Unit,
) {
  when (uiState) {
    SearchUiState.Idle -> StatusMessage(stringResource(R.string.search_idle_hint))
    SearchUiState.Loading -> LoadingMessage()
    SearchUiState.Empty ->
        StatusMessage(
            stringResource(R.string.search_empty, query.trim()),
            LiveRegionMode.Polite,
        )
    SearchUiState.Error -> {
      StatusMessage(
          text = stringResource(R.string.search_error),
          liveRegionMode = LiveRegionMode.Assertive,
          color = MaterialTheme.colorScheme.error,
          icon = Icons.Filled.Warning,
      )
      // TextButton already guarantees a 48dp x 48dp touch target.
      TextButton(onClick = onRetry) { Text(stringResource(R.string.search_retry)) }
    }
    is SearchUiState.Content ->
        LazyColumn(modifier = Modifier.fillMaxSize()) {
          items(uiState.tracks, key = { it.id }) { track ->
            TrackRow(track, onClick = { onTrackClick(track) })
          }
        }
  }
}

@Composable
private fun StatusMessage(
    text: String,
    liveRegionMode: LiveRegionMode? = null,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    icon: ImageVector? = null,
) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
          Modifier.padding(vertical = 16.dp).semantics(mergeDescendants = true) {
            if (liveRegionMode != null) liveRegion = liveRegionMode
          },
  ) {
    // The text carries the meaning; the icon only reinforces it, so it is decorative.
    if (icon != null) {
      Icon(imageVector = icon, contentDescription = null, tint = color)
      Spacer(Modifier.width(12.dp))
    }
    Text(text = text, style = MaterialTheme.typography.bodyLarge, color = color)
  }
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

  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
          Modifier.fillMaxWidth()
              .heightIn(min = 72.dp)
              .semantics(mergeDescendants = true) {}
              .clickable(
                  onClickLabel = stringResource(R.string.track_open_detail),
                  role = Role.Button,
                  onClick = onClick,
              )
              .padding(vertical = 8.dp),
  ) {
    // The cover only decorates: the row already says the title and the artist.
    AsyncImage(
        model = track.artworkUrl,
        contentDescription = null,
        modifier =
            Modifier.size(56.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
    )
    Spacer(Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = track.name, style = MaterialTheme.typography.titleMedium)
      Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
