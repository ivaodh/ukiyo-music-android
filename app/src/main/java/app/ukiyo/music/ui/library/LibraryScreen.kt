package app.ukiyo.music.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ukiyo.music.data.api.Track
import app.ukiyo.music.data.api.UserPlaylist
import app.ukiyo.music.LibraryState
import app.ukiyo.music.ui.components.Artwork
import app.ukiyo.music.ui.components.EmptyState
import app.ukiyo.music.ui.components.LoadingState
import app.ukiyo.music.ui.components.SectionHeader
import app.ukiyo.music.ui.components.TrackRow
import app.ukiyo.music.ui.theme.UkiyoMuted

@Composable
fun LibraryScreen(
    state: LibraryState,
    onTrack: (Track, List<Track>) -> Unit,
    onOpenTracks: (String, List<Track>) -> Unit,
    onOpenPlaylist: (UserPlaylist) -> Unit,
    onSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    LaunchedEffect(Unit) { onRefresh() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 18.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSettings) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = UkiyoMuted)
                }
            }
        }

        if (state.loading && state.liked.isEmpty() && state.history.isEmpty()) {
            item { LoadingState() }
        }

        if (state.liked.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Liked Songs",
                    action = "See all",
                    onAction = { onOpenTracks("Liked Songs", state.liked) }
                )
            }
            itemsIndexed(state.liked.take(10), key = { index, track -> "liked:${track.key}:$index" }) { _, track ->
                TrackRow(track = track, onClick = { onTrack(track, state.liked) })
            }
        }

        if (state.history.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recently Played",
                    action = "See all",
                    onAction = { onOpenTracks("Recently Played", state.history) }
                )
            }
            itemsIndexed(state.history.take(10), key = { index, track -> "history:${track.key}:$index" }) { _, track ->
                TrackRow(track = track, onClick = { onTrack(track, state.history) })
            }
        }

        if (state.playlists.isNotEmpty()) {
            item { SectionHeader("Playlists") }
            items(state.playlists, key = { "playlist:${it.id}" }) { playlist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPlaylist(playlist) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Artwork(
                        url = playlist.thumbnails.firstOrNull(),
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = playlist.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Playlist",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UkiyoMuted
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.QueueMusic,
                        contentDescription = null,
                        tint = UkiyoMuted
                    )
                }
            }
        }

        if (!state.loading && state.liked.isEmpty() && state.history.isEmpty() && state.playlists.isEmpty()) {
            item { EmptyState("Your library is empty. Play something to get started.") }
        }

        if (!state.error.isNullOrBlank()) {
            item {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
