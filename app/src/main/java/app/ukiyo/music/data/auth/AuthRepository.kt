package app.ukiyo.music.data.auth

import android.content.Context
import app.ukiyo.music.BuildConfig
import app.ukiyo.music.data.api.AuthResponse
import app.ukiyo.music.data.api.DeviceRequest
import app.ukiyo.music.data.api.LoginRequest
import app.ukiyo.music.data.api.UkiyoApi
import app.ukiyo.music.data.api.User
import com.google.gson.Gson
import retrofit2.HttpException

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Failure(val message: String, val deviceLimit: Boolean = false) : AuthResult()
}

class AuthRepository(
    private val context: Context,
    private val api: UkiyoApi,
    private val sessionStore: SessionStore
) {
    private val gson = Gson()

    val isLoggedIn: Boolean get() = sessionStore.isLoggedIn
    val currentUser: User? get() = sessionStore.user

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = api.login(LoginRequest(email.trim(), password))
            val token = response.token
            val userDto = response.user
            if (token.isNullOrBlank() || userDto?.id == null) {
                AuthResult.Failure(response.message ?: response.error ?: "Login failed")
            } else {
                val user = User(
                    id = userDto.id,
                    email = userDto.email ?: email.trim(),
                    role = userDto.role ?: "friend"
                )
                sessionStore.saveSession(token, response.refreshToken, user)
                ensureDeviceFingerprint()
                val device = registerDevice()
                if (device != null) {
                    sessionStore.clear()
                    AuthResult.Failure(device, deviceLimit = true)
                } else {
                    AuthResult.Success(user)
                }
            }
        } catch (e: HttpException) {
            AuthResult.Failure(parseError(e) ?: "Login failed")
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Unable to reach Ukiyo Music")
        }
    }

    suspend fun registerDevice(): String? {
        return try {
            val fingerprint = ensureDeviceFingerprint()
            val response = api.registerDevice(
                DeviceRequest(
                    fingerprint = fingerprint,
                    deviceName = DeviceInfo.name(),
                    appVersion = BuildConfig.VERSION_NAME
                )
            )
            if (response.ok == true) null
            else response.message ?: response.error ?: "Device registration failed"
        } catch (e: HttpException) {
            parseError(e) ?: "Device registration failed (${e.code()})"
        } catch (e: Exception) {
            "Unable to verify this device. Check your connection."
        }
    }

    fun ensureDeviceFingerprint(): String {
        val existing = sessionStore.deviceFingerprint
        if (!existing.isNullOrBlank()) return existing
        val fingerprint = DeviceInfo.fingerprint(context)
        sessionStore.deviceFingerprint = fingerprint
        return fingerprint
    }

    fun logout() {
        sessionStore.clear()
    }

    private fun parseError(e: HttpException): String? {
        return runCatching {
            val body = e.response()?.errorBody()?.string()
            val parsed = gson.fromJson(body, AuthResponse::class.java)
            parsed.message ?: parsed.error
        }.getOrNull()
    }
}
