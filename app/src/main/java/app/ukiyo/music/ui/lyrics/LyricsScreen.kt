package app.ukiyo.music.ui.lyrics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.ukiyo.music.data.api.Track
import app.ukiyo.music.LyricsState
import app.ukiyo.music.ui.components.EmptyState
import app.ukiyo.music.ui.components.LoadingState
import app.ukiyo.music.ui.currentLrcIndex
import app.ukiyo.music.ui.parseLrc
import app.ukiyo.music.ui.theme.UkiyoMuted
import app.ukiyo.music.ui.theme.UkiyoPeach

@Composable
fun LyricsScreen(
    track: Track?,
    state: LyricsState,
    positionMs: Long,
    onLoad: (Track) -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(track?.key) {
        if (track != null) onLoad(track)
    }

    val lines = remember(state.lyrics?.syncedLrc) { parseLrc(state.lyrics?.syncedLrc) }
    val activeIndex = remember(lines, positionMs) { currentLrcIndex(lines, positionMs) }
    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            listState.animateScrollToItem(activeIndex.coerceAtMost((lines.size - 1).coerceAtLeast(0)))
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = UkiyoMuted)
            }
            Column(Modifier.weight(1f)) {
                Text("Lyrics", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = track?.title ?: "No track playing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UkiyoMuted,
                    maxLines = 1
                )
            }
        }

        when {
            track == null -> EmptyState("Play a song to see lyrics.")
            state.loading -> LoadingState()
            lines.isNotEmpty() -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 48.dp)
            ) {
                itemsIndexed(lines) { index, line ->
                    Text(
                        text = line.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (index == activeIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == activeIndex) UkiyoPeach else UkiyoMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp)
                    )
                }
            }
            !state.lyrics?.plainLyrics.isNullOrBlank() -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp)
            ) {
                item {
                    Text(
                        text = state.lyrics?.plainLyrics.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            else -> EmptyState(state.error ?: "No lyrics found for this song.")
        }
    }
}
