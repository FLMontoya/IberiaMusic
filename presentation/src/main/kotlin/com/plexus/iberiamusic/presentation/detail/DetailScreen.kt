package com.plexus.iberiamusic.presentation.detail

import androidx.annotation.StringRes
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.plexus.iberiamusic.domain.model.Track
import com.plexus.iberiamusic.presentation.R

/** Details of a [Track]. Fields the track does not have are left out. Scrolls on large fonts. */
@Composable
fun DetailScreen(track: Track, onBack: () -> Unit, modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
    TextButton(onClick = onBack) { Text(stringResource(R.string.detail_back)) }
    Spacer(Modifier.size(8.dp))
    Text(
        text = track.name,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.size(16.dp))
    DetailField(R.string.detail_artist, track.artistName)
    track.albumName?.let { DetailField(R.string.detail_album, it) }
    track.genre?.let { DetailField(R.string.detail_genre, it) }
    track.durationMillis?.let { DetailField(R.string.detail_duration, formatDuration(it)) }
    track.releaseDate?.let { DetailField(R.string.detail_release_date, formatReleaseDate(it)) }
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
