package app.ukiyo.music.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.ukiyo.music.BuildConfig
import app.ukiyo.music.data.config.AppConfig
import app.ukiyo.music.ui.theme.UkiyoBlack
import app.ukiyo.music.ui.theme.UkiyoMuted
import app.ukiyo.music.ui.theme.UkiyoPeach
import app.ukiyo.music.ui.theme.UkiyoSurfaceRaised

@Composable
fun SettingsScreen(
    quality: String,
    onQuality: (String) -> Unit,
    downloadWifiOnly: Boolean,
    onDownloadWifiOnly: (Boolean) -> Unit,
    config: AppConfig,
    userEmail: String,
    onRefreshConfig: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = UkiyoMuted)
            }
            Text("Settings", style = MaterialTheme.typography.titleLarge)
        }

        SettingsSection(title = "Playback quality") {
            QualityRow(
                title = "EQ+",
                subtitle = "320 kbps. Best balance for daily listening.",
                selected = quality == "eq+",
                onClick = { onQuality("eq+") }
            )
            QualityRow(
                title = "FLAC",
                subtitle = "Lossless. Uses more data and storage.",
                selected = quality == "eq",
                onClick = { onQuality("eq") }
            )
            QualityRow(
                title = "AAC",
                subtitle = "Data saver. 128 kbps pure audio.",
                selected = quality == "aac",
                onClick = { onQuality("aac") }
            )
        }

        SettingsSection(title = "Downloads") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Wi-Fi only", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Only download when connected to Wi-Fi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UkiyoMuted
                    )
                }
                Switch(checked = downloadWifiOnly, onCheckedChange = onDownloadWifiOnly)
            }
            Text(
                text = "Storage limit: 5 GB (oldest downloads are removed automatically).",
                style = MaterialTheme.typography.bodyMedium,
                color = UkiyoMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        SettingsSection(title = "Account") {
            Text(
                text = userEmail,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            TextButton(onClick = onRefreshConfig, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("Refresh server config")
            }
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = UkiyoSurfaceRaised,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Sign out")
            }
        }

        SettingsSection(title = "About") {
            InfoRow("App version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            InfoRow("Package", BuildConfig.APPLICATION_ID)
            InfoRow("Server", if (config.enabled) "Online" else "Maintenance")
            InfoRow("Device limit", "${config.maxDevices} devices")
            Text(
                text = "Admin tools are available in the Ukiyo web app only.",
                style = MaterialTheme.typography.bodyMedium,
                color = UkiyoMuted,
                modifier = Modifier.padding(16.dp)
            )
        }

        SettingsSection(title = "Credits") {
            val uriHandler = LocalUriHandler.current
            Text(
                text = "Ukiyo Music is licensed under GPLv3. UI patterns are adapted from BitChord, also GPLv3.",
                style = MaterialTheme.typography.bodyMedium,
                color = UkiyoMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
            TextButton(
                onClick = { uriHandler.openUri("https://github.com/kushagrasinghx/BitChord") },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("BitChord upstream")
            }
            TextButton(
                onClick = { uriHandler.openUri("https://github.com/ivaodh/ukiyo-music-android") },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Ukiyo source code")
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = UkiyoMuted,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 6.dp)
    )
    Surface(color = UkiyoSurfaceRaised, modifier = Modifier.fillMaxWidth()) {
        Column { content() }
    }
}

@Composable
private fun QualityRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = UkiyoMuted)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = UkiyoMuted, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider(color = UkiyoBlack, thickness = 1.dp)
}
