package app.ukiyo.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.ukiyo.music.ui.theme.UkiyoBlack
import app.ukiyo.music.ui.theme.UkiyoMuted
import app.ukiyo.music.ui.theme.UkiyoPeach

@Composable
fun MaintenanceScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UkiyoBlack)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ukiyo Music",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = UkiyoMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 20.dp)
        )
        Button(onClick = onRetry) {
            Text("Try again")
        }
    }
}

@Composable
fun UpdateScreen(onUpdate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UkiyoBlack)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Update Ukiyo Music",
            style = MaterialTheme.typography.displaySmall,
            color = UkiyoPeach,
            textAlign = TextAlign.Center
        )
        Text(
            text = "A newer version is required to continue. Update from Google Play.",
            style = MaterialTheme.typography.bodyLarge,
            color = UkiyoMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 20.dp)
        )
        Button(onClick = onUpdate) {
            Text("Open Google Play")
        }
    }
}
