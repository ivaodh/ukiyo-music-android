package app.ukiyo.music.ui.downloads

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ukiyo.music.data.download.DownloadEntity
import app.ukiyo.music.data.download.DownloadRepository
import app.ukiyo.music.ui.components.Artwork
import app.ukiyo.music.ui.components.EmptyState
import app.ukiyo.music.ui.formatBytes
import app.ukiyo.music.ui.theme.UkiyoMuted
import app.ukiyo.music.ui.theme.UkiyoPeach
import app.ukiyo.music.ui.theme.UkiyoSurfaceRaised

@Composable
fun DownloadsScreen(
    downloads: List<DownloadEntity>,
    totalBytes: Long,
    onPlay: (DownloadEntity) -> Unit,
    onDelete: (String) -> Unit,
    onClearAll: () -> Unit,
    onSettings: () -> Unit
) {
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
                    text = "Downloads",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSettings) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = UkiyoMuted)
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${formatBytes(totalBytes)} of ${formatBytes(DownloadRepository.MAX_BYTES)} used",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UkiyoMuted
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { (totalBytes.toFloat() / DownloadRepository.MAX_BYTES.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = UkiyoPeach,
                    trackColor = UkiyoSurfaceRaised
                )
                if (downloads.isNotEmpty()) {
                    TextButton(onClick = onClearAll, modifier = Modifier.padding(top = 4.dp)) {
                        Text("Clear all downloads")
                    }
                }
            }
        }

        if (downloads.isEmpty()) {
            item { EmptyState("No downloads yet. Use the download button on a song to save it for offline listening.") }
        }

        items(downloads, key = { it.trackKey }) { entity ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlay(entity) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Artwork(url = entity.thumbnailUrl, modifier = Modifier.size(52.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = entity.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${entity.artist} • ${entity.quality.uppercase()} • ${formatBytes(entity.sizeBytes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UkiyoMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { onPlay(entity) }) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = UkiyoPeach)
                }
                IconButton(onClick = { onDelete(entity.trackKey) }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = UkiyoMuted)
                }
            }
        }
    }
}
