package app.ukiyo.music.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "ukiyo_settings")

class SettingsRepository(private val context: Context) {
    private val qualityKey = stringPreferencesKey("quality")
    private val downloadWifiOnlyKey = booleanPreferencesKey("download_wifi_only")

    val quality: Flow<String> = context.settingsDataStore.data.map { it[qualityKey] ?: "eq+" }
    val downloadWifiOnly: Flow<Boolean> = context.settingsDataStore.data.map { it[downloadWifiOnlyKey] ?: true }

    suspend fun currentQuality(): String = quality.first()

    suspend fun setQuality(value: String) {
        context.settingsDataStore.edit { it[qualityKey] = value }
    }

    suspend fun setDownloadWifiOnly(value: Boolean) {
        context.settingsDataStore.edit { it[downloadWifiOnlyKey] = value }
    }
}
