package app.ukiyo.music.data.config

import app.ukiyo.music.data.api.UkiyoApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppConfig(
    val enabled: Boolean = true,
    val minVersionCode: Int = 1,
    val maintenanceMessage: String = "Ukiyo Music is temporarily unavailable.",
    val defaultQuality: String = "eq+",
    val maxDevices: Int = 3
)

class ConfigRepository(private val api: UkiyoApi) {
    private val _config = MutableStateFlow(AppConfig())
    val config: StateFlow<AppConfig> = _config.asStateFlow()

    suspend fun fetch(): AppConfig {
        return runCatching {
            val response = api.config()
            AppConfig(
                enabled = response.enabled ?: true,
                minVersionCode = response.minVersionCode ?: 1,
                maintenanceMessage = response.maintenanceMessage ?: "Ukiyo Music is temporarily unavailable.",
                defaultQuality = response.defaultQuality ?: "eq+",
                maxDevices = response.maxDevices ?: 3
            ).also { _config.value = it }
        }.getOrElse { _config.value }
    }
}
