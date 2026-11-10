package app.ukiyo.music.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.ukiyo.music.data.api.Track
import app.ukiyo.music.SearchState
import app.ukiyo.music.ui.components.EmptyState
import app.ukiyo.music.ui.components.LoadingState
import app.ukiyo.music.ui.components.TrackRow
import app.ukiyo.music.ui.theme.UkiyoMuted

@Composable
fun SearchScreen(
    state: SearchState,
    onQueryChange: (String) -> Unit,
    onTrack: (Track) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 10.dp)
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            placeholder = { Text("Songs, artists, albums") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        if (state.loading) {
            LoadingState()
        } else if (!state.error.isNullOrBlank()) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        } else if (state.query.isNotBlank() && state.results.isEmpty()) {
            EmptyState("No songs found for \"${state.query}\"")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
        ) {
            itemsIndexed(state.results, key = { index, track -> "${track.key}:$index" }) { _, track ->
                TrackRow(track = track, onClick = { onTrack(track) })
            }
        }
    }
}
