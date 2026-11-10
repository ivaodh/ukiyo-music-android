package app.ukiyo.music.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import app.ukiyo.music.data.api.Track
import app.ukiyo.music.playback.PlayerUiState
import app.ukiyo.music.ui.components.Artwork
import app.ukiyo.music.ui.formatDuration
import app.ukiyo.music.ui.theme.UkiyoBlack
import app.ukiyo.music.ui.theme.UkiyoMuted
import app.ukiyo.music.ui.theme.UkiyoPeach
import app.ukiyo.music.ui.visual.MeshGradientBackground
import app.ukiyo.music.ui.visual.rememberArtworkMeshPalette
import coil.compose.AsyncImage

@Composable
fun NowPlayingScreen(
    state: PlayerUiState,
    track: Track?,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleLike: () -> Unit,
    onDownload: () -> Unit,
    onLyrics: () -> Unit,
    onQueue: () -> Unit
) {
    var dragPosition by remember { mutableStateOf<Float?>(null) }
    val duration = state.durationMs.coerceAtLeast(0L)
    val sliderValue = dragPosition ?: state.positionMs.coerceIn(0L, duration.coerceAtLeast(1L)).toFloat()
    val mesh = rememberArtworkMeshPalette(state.artworkUri)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UkiyoBlack)
    ) {
        MeshGradientBackground(
            palette = mesh,
            modifier = Modifier.fillMaxSize(),
            trackKey = state.mediaId,
            animated = state.artworkUri != null
        )

        if (!state.artworkUri.isNullOrBlank()) {
            AsyncImage(
                model = state.artworkUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(470.dp)
                    .align(Alignment.TopCenter)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(470.dp)
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.45f),
                            0.22f to Color.Transparent,
                            0.58f to UkiyoBlack.copy(alpha = 0.35f),
                            0.78f to UkiyoBlack.copy(alpha = 0.82f),
                            1f to UkiyoBlack
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Close", tint = Color.White.copy(alpha = 0.85f))
                }
                Text(
                    text = "Now playing",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.size(40.dp))
            }

            Spacer(Modifier.height(365.dp))

            Text(
                text = state.title.ifBlank { "Nothing playing" },
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = state.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            state.album?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(18.dp))
            Slider(
                value = sliderValue,
                onValueChange = { dragPosition = it },
                onValueChangeFinished = {
                    dragPosition?.let { onSeek(it.toLong()) }
                    dragPosition = null
                },
                valueRange = 0f..duration.coerceAtLeast(1L).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = UkiyoPeach,
                    activeTrackColor = UkiyoPeach,
                    inactiveTrackColor = Color(0xFF3A342C)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = formatDuration(state.positionMs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = formatDuration(state.durationMs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (state.shuffle) UkiyoPeach else UkiyoMuted
                    )
                }
                IconButton(onClick = onPrevious, enabled = state.hasPrevious || state.positionMs > 0) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(38.dp))
                }
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(72.dp)
                        .background(UkiyoPeach, androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint = UkiyoBlack,
                        modifier = Modifier.size(38.dp)
                    )
                }
                IconButton(onClick = onNext, enabled = state.hasNext) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(38.dp))
                }
                IconButton(onClick = onCycleRepeat) {
                    Icon(
                        imageVector = if (state.repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = if (state.repeatMode == Player.REPEAT_MODE_OFF) UkiyoMuted else UkiyoPeach
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onToggleLike, enabled = track != null) {
                    Icon(Icons.Rounded.FavoriteBorder, contentDescription = "Like", tint = UkiyoMuted)
                }
                IconButton(onClick = onDownload, enabled = track != null) {
                    Icon(Icons.Rounded.Download, contentDescription = "Download", tint = UkiyoMuted)
                }
                IconButton(onClick = onLyrics, enabled = track != null) {
                    Icon(Icons.Rounded.Lyrics, contentDescription = "Lyrics", tint = UkiyoMuted)
                }
                IconButton(onClick = onQueue) {
                    Icon(Icons.Rounded.QueueMusic, contentDescription = "Queue", tint = UkiyoMuted)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
