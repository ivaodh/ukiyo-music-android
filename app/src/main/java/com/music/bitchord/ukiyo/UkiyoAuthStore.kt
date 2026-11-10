package com.music.bitchord.ukiyo

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.music.bitchord.BuildConfig
import com.music.bitchord.auth.EncryptedPrefs
import com.music.bitchord.auth.SignedIn
import com.music.bitchord.data.ukiyo.UkiyoSession
import com.music.bitchord.data.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest

@Serializable
private data class LoginRequest(val email: String, val password: String)

@Serializable
private data class UserDto(val id: String? = null, val email: String? = null, val role: String? = null)

@Serializable
private data class LoginResponse(
    val ok: Boolean? = null,
    val token: String? = null,
    val refreshToken: String? = null,
    val user: UserDto? = null,
    val error: String? = null,
    val message: String? = null,
)

@Serializable
private data class DeviceRequest(
    val fingerprint: String,
    val deviceName: String,
    val appVersion: String,
)

@Serializable
private data class DeviceResponse(
    val ok: Boolean? = null,
    val deviceCount: Int? = null,
    val maxDevices: Int? = null,
    val error: String? = null,
    val message: String? = null,
)

class UkiyoAuthStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = EncryptedPrefs.open(appContext, "ukiyo_auth", "ukiyo_auth_plain")
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    init {
        UkiyoSession.baseUrl = BuildConfig.UKIYO_API_BASE_URL
        UkiyoSession.token = accessToken
        UkiyoSession.deviceFingerprint = deviceFingerprint
        if (isLoggedIn) {
            SignedIn.state(appContext).value = true
            applyUkiyoContentSettings()
        }
    }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        private set(value) = prefs.edit().putString(KEY_ACCESS, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        private set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

    var email: String?
        get() = prefs.getString(KEY_EMAIL, null)
        private set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    var deviceFingerprint: String?
        get() = prefs.getString(KEY_DEVICE, null)
        private set(value) = prefs.edit().putString(KEY_DEVICE, value).apply()

    val isLoggedIn: Boolean
        get() = !accessToken.isNullOrBlank() && !email.isNullOrBlank()

    suspend fun login(email: String, password: String): String? = withContext(Dispatchers.IO) {
        val body = json.encodeToString(LoginRequest.serializer(), LoginRequest(email.trim(), password))
        val request = Request.Builder()
            .url("${BuildConfig.UKIYO_API_BASE_URL.trimEnd('/')}/auth/login")
            .post(body.toRequestBody(JSON))
            .build()

        val response = runCatching { client.newCall(request).execute() }.getOrElse {
            return@withContext it.message ?: "Unable to reach Ukiyo Music"
        }
        response.use {
            val text = it.body.string()
            val parsed = runCatching {
                json.decodeFromString(LoginResponse.serializer(), text)
            }.getOrNull()

            if (!it.isSuccessful || parsed?.ok != true || parsed.token.isNullOrBlank()) {
                return@withContext parsed?.message ?: parsed?.error ?: "Login failed (${it.code})"
            }

            val token = parsed.token
            val refresh = parsed.refreshToken
            val userEmail = parsed.user?.email ?: email.trim()
            val fingerprint = deviceFingerprint ?: buildFingerprint(appContext)
            val registrationError = registerDevice(token, fingerprint)
            if (registrationError != null) {
                return@withContext registrationError
            }

            accessToken = token
            refreshToken = refresh
            this@UkiyoAuthStore.email = userEmail
            deviceFingerprint = fingerprint
            UkiyoSession.token = token
            UkiyoSession.deviceFingerprint = fingerprint
            SignedIn.state(appContext).value = true
            applyUkiyoContentSettings()
            null
        }
    }

    fun logout() {
        prefs.edit()
            .remove(KEY_ACCESS)
            .remove(KEY_REFRESH)
            .remove(KEY_EMAIL)
            .apply()
        UkiyoSession.token = null
        SignedIn.state(appContext).value = false
    }

    fun authHeader(): String? = accessToken?.let { "Bearer $it" }

    private fun registerDevice(token: String, fingerprint: String): String? {
        val body = json.encodeToString(
            DeviceRequest.serializer(),
            DeviceRequest(
                fingerprint = fingerprint,
                deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
                appVersion = BuildConfig.VERSION_NAME,
            ),
        )
        val request = Request.Builder()
            .url("${BuildConfig.UKIYO_API_BASE_URL.trimEnd('/')}/devices/register")
            .header("Authorization", "Bearer $token")
            .post(body.toRequestBody(JSON))
            .build()

        val response = runCatching { client.newCall(request).execute() }.getOrElse {
            return it.message ?: "Unable to verify this device"
        }
        response.use {
            val text = it.body.string()
            val parsed = runCatching {
                json.decodeFromString(DeviceResponse.serializer(), text)
            }.getOrNull()
            if (it.isSuccessful && parsed?.ok == true) return null
            return parsed?.message ?: parsed?.error ?: "Device registration failed (${it.code})"
        }
    }

    private fun applyUkiyoContentSettings() {
        AppSettings.setOnboardingComplete()
        AppSettings.setHideTopPicks(true)
        AppSettings.setHideHomeMoods(true)
    }

    private fun buildFingerprint(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        ) ?: "unknown"
        val raw = "$androidId:${context.packageName}"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val JSON = "application/json".toMediaType()
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_EMAIL = "email"
        private const val KEY_DEVICE = "device_fingerprint"
    }
}
