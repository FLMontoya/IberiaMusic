package com.plexus.iberiamusic.presentation.detail

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.presentation.R

private val ARTWORK_SIZE = 240.dp

/** Details of a [Track]. Fields the track does not have show a dash. Scrolls on large fonts. */
@Composable
fun DetailScreen(track: Track, onBack: () -> Unit, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
    TextButton(onClick = onBack) { Text(stringResource(R.string.detail_back)) }
    Spacer(Modifier.size(8.dp))
    (track.largeArtworkUrl ?: track.artworkUrl)?.let { url ->
      AsyncImage(
          model = url,
          contentDescription = stringResource(R.string.detail_artwork, track.name),
          modifier =
              Modifier.size(ARTWORK_SIZE)
                  .align(Alignment.CenterHorizontally)
                  .clip(MaterialTheme.shapes.medium)
                  .background(MaterialTheme.colorScheme.surfaceVariant),
      )
      Spacer(Modifier.size(16.dp))
    }
    Text(
        text = track.name,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.size(16.dp))
    val missing = stringResource(R.string.detail_value_missing)
    DetailField(R.string.detail_artist, track.artistName)
    DetailField(R.string.detail_album, track.albumName ?: missing)
    DetailField(R.string.detail_genre, track.genre ?: missing)
    DetailField(
        R.string.detail_duration,
        track.durationMillis?.let { formatDuration(it) } ?: missing,
    )
    DetailField(
        R.string.detail_release_date,
        track.releaseDate?.let { formatReleaseDate(it) } ?: missing,
    )
  }
}

/** Label and value read together by screen readers as a single element. */
@Composable
private fun DetailField(@StringRes labelRes: Int, value: String) {
  Column(
      modifier =
          Modifier.fillMaxWidth().padding(vertical = 8.dp).semantics(mergeDescendants = true) {},
  ) {
    Text(
        text = stringResource(labelRes),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(text = value, style = MaterialTheme.typography.bodyLarge)
  }
}
