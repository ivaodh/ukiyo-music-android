package app.ukiyo.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ukiyo.music.data.api.FeedPlaylist
import app.ukiyo.music.data.api.Track
import app.ukiyo.music.data.api.UserPlaylist
import app.ukiyo.music.HomeState
import app.ukiyo.music.ui.components.Artwork
import app.ukiyo.music.ui.components.EmptyState
import app.ukiyo.music.ui.components.HorizontalTrackList
import app.ukiyo.music.ui.components.LoadingState
import app.ukiyo.music.ui.components.PlaylistCard
import app.ukiyo.music.ui.components.SectionHeader
import app.ukiyo.music.ui.components.TrackRow
import app.ukiyo.music.ui.theme.UkiyoBlack
import app.ukiyo.music.ui.theme.UkiyoGold
import app.ukiyo.music.ui.theme.UkiyoMuted
import app.ukiyo.music.ui.visual.MeshGradientBackground
import app.ukiyo.music.ui.visual.rememberArtworkMeshPalette
import java.util.Calendar

@Composable
fun HomeScreen(
    state: HomeState,
    userEmail: String,
    onTrack: (Track, List<Track>) -> Unit,
    onFeedPlaylist: (FeedPlaylist) -> Unit,
    onUserPlaylist: (UserPlaylist) -> Unit,
    onSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Late night listening"
    }

    val artUrl = state.data?.recent?.firstOrNull()?.thumbnailUrl
        ?: state.data?.liked?.firstOrNull()?.thumbnailUrl
        ?: state.data?.feed?.firstOrNull()?.thumbnailUrl
    val mesh = rememberArtworkMeshPalette(artUrl)

    Box(Modifier.fillMaxSize()) {
        MeshGradientBackground(
            palette = mesh,
            modifier = Modifier.fillMaxSize(),
            trackKey = artUrl,
            animated = artUrl != null
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            UkiyoBlack.copy(alpha = 0.45f),
                            UkiyoBlack.copy(alpha = 0.92f)
                        )
                    )
                )
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Ukiyo",
                        style = MaterialTheme.typography.displaySmall,
                        color = UkiyoGold,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "$greeting, ${userEmail.substringBefore('@')}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UkiyoMuted
                    )
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = UkiyoMuted)
                }
            }
        }

        if (state.loading && state.data == null) {
            item { LoadingState() }
        }

        val recent = state.data?.recent.orEmpty()
        if (recent.isNotEmpty()) {
            item { SectionHeader("Recently played") }
            item {
                HorizontalTrackList(tracks = recent.take(12), onTrack = { onTrack(it, recent) })
            }
        }

        val liked = state.data?.liked.orEmpty()
        if (liked.isNotEmpty()) {
            item { SectionHeader("Liked songs") }
            item {
                HorizontalTrackList(tracks = liked.take(12), onTrack = { onTrack(it, liked) })
            }
        }

        val playlists = state.data?.playlists.orEmpty()
        if (playlists.isNotEmpty()) {
            item { SectionHeader("Your playlists") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        UserPlaylistCard(playlist = playlist, onClick = { onUserPlaylist(playlist) })
                    }
                }
            }
        }

        val feed = state.data?.feed.orEmpty()
        if (feed.isNotEmpty()) {
            item { SectionHeader("Made for you") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(feed, key = { index, playlist -> "${playlist.id}:$index" }) { _, playlist ->
                        PlaylistCard(playlist = playlist, onClick = { onFeedPlaylist(playlist) })
                    }
                }
            }
        }

        if (!state.error.isNullOrBlank()) {
            item {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        if (!state.loading && state.data != null && recent.isEmpty() && liked.isEmpty()) {
            item { EmptyState("Start listening to build your Ukiyo home.") }
        }

        item {
            Text(
                text = "Tap to refresh",
                style = MaterialTheme.typography.bodyMedium,
                color = UkiyoMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .clickable { onRefresh() }
            )
        }
        }
    }
}

@Composable
private fun UserPlaylistCard(playlist: UserPlaylist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Artwork(
            url = playlist.thumbnails.firstOrNull(),
            modifier = Modifier.size(140.dp),
            corner = 16
        )
        Spacer(Modifier.height(8.dp))
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
}
