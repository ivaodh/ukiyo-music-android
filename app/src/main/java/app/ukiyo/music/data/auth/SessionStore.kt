package app.ukiyo.music.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.ukiyo.music.data.api.User
import com.google.gson.Gson

class SessionStore(context: Context) {
    private val prefs: SharedPreferences
    private val gson = Gson()

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            "ukiyo_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        set(value) = prefs.edit().putString(KEY_ACCESS, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

    var user: User?
        get() = prefs.getString(KEY_USER, null)?.let {
            runCatching { gson.fromJson(it, User::class.java) }.getOrNull()
        }
        set(value) = prefs.edit().putString(KEY_USER, value?.let { gson.toJson(it) }).apply()

    var deviceFingerprint: String?
        get() = prefs.getString(KEY_DEVICE, null)
        set(value) = prefs.edit().putString(KEY_DEVICE, value).apply()

    val isLoggedIn: Boolean
        get() = !accessToken.isNullOrBlank() && user != null

    fun saveSession(token: String, refresh: String?, user: User) {
        accessToken = token
        refreshToken = refresh
        this.user = user
    }

    fun clear() {
        accessToken = null
        refreshToken = null
        user = null
        deviceFingerprint = null
    }

    companion object {
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_USER = "user"
        private const val KEY_DEVICE = "device_fingerprint"
    }
}
