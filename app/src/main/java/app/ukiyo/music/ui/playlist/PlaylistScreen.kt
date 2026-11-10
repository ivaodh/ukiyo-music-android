package app.ukiyo.music.ui.playlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ukiyo.music.data.api.Track
import app.ukiyo.music.PlaylistState
import app.ukiyo.music.ui.components.EmptyState
import app.ukiyo.music.ui.components.LoadingState
import app.ukiyo.music.ui.components.TrackRow
import app.ukiyo.music.ui.theme.UkiyoMuted

@Composable
fun PlaylistScreen(
    state: PlaylistState,
    onTrack: (Track) -> Unit,
    onPlayAll: () -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = UkiyoMuted)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = state.title.ifBlank { "Playlist" },
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${state.tracks.size} songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UkiyoMuted
                )
            }
            IconButton(onClick = onPlayAll, enabled = state.tracks.isNotEmpty()) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play all", tint = MaterialTheme.colorScheme.primary)
            }
        }

        when {
            state.loading -> LoadingState()
            !state.error.isNullOrBlank() -> EmptyState(state.error)
            state.tracks.isEmpty() -> EmptyState("No songs in this playlist.")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                itemsIndexed(state.tracks, key = { index, track -> "${track.key}:${track.videoId}:$index" }) { _, track ->
                    TrackRow(track = track, onClick = { onTrack(track) })
                }
            }
        }
    }
}
