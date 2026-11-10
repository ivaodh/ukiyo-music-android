package app.ukiyo.music.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ukiyo.music.playback.PlayerUiState
import app.ukiyo.music.ui.glass.ukiyoGlass
import app.ukiyo.music.ui.theme.UkiyoGold
import app.ukiyo.music.ui.theme.UkiyoMuted
import app.ukiyo.music.ui.theme.UkiyoPeach
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState

private data class DockTab(val route: String, val icon: ImageVector, val label: String)

private val dockTabs = listOf(
    DockTab("home", Icons.Rounded.Home, "Home"),
    DockTab("search", Icons.Rounded.Search, "Search"),
    DockTab("library", Icons.Rounded.LibraryMusic, "Library"),
    DockTab("downloads", Icons.Rounded.Download, "Downloads")
)

@Composable
fun FloatingDock(
    hazeState: HazeState,
    player: PlayerUiState,
    currentRoute: String,
    onTab: (String) -> Unit,
    onOpenPlayer: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(32.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .ukiyoGlass(hazeState = hazeState, shape = shape)
            .padding(6.dp)
    ) {
        if (player.mediaId != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .clickable(onClick = onOpenPlayer)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1D1D1D)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!player.artworkUri.isNullOrBlank()) {
                            AsyncImage(
                                model = player.artworkUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        } else {
                            Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = UkiyoMuted)
                        }
                    }
                    ProgressRing(
                        progress = if (player.durationMs > 0) {
                            (player.positionMs.toFloat() / player.durationMs.toFloat()).coerceIn(0f, 1f)
                        } else 0f
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = player.title.ifBlank { "Ukiyo Music" },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = player.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = UkiyoMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onPrevious, enabled = player.hasPrevious) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = if (player.hasPrevious) MaterialTheme.colorScheme.onSurface else UkiyoMuted.copy(alpha = 0.35f))
                }
                IconButton(onClick = onPlayPause) {
                    Icon(
                        imageVector = if (player.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (player.isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onNext, enabled = player.hasNext) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = if (player.hasNext) MaterialTheme.colorScheme.onSurface else UkiyoMuted.copy(alpha = 0.35f))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .padding(horizontal = 4.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            dockTabs.forEach { tab ->
                val active = currentRoute == tab.route
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (active) UkiyoPeach.copy(alpha = 0.16f) else Color.Transparent)
                        .clickable { onTab(tab.route) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (active) UkiyoGold else UkiyoMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressRing(progress: Float) {
    Canvas(Modifier.size(50.dp)) {
        val stroke = 2.dp.toPx()
        val inset = stroke / 2f + 1.dp.toPx()
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = size.minDimension / 2f - inset,
            style = Stroke(width = stroke)
        )
        drawArc(
            color = UkiyoPeach,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}
