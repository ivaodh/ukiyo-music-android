package app.ukiyo.music.data.api

import app.ukiyo.music.data.auth.SessionStore
import com.google.gson.Gson
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val sessionStore: SessionStore,
    private val baseUrl: String
) : Authenticator {
    private val gson = Gson()
    private val client = OkHttpClient()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.endsWith("/auth/refresh")) return null

        synchronized(this) {
            val current = sessionStore.accessToken
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (current != null && current != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $current")
                    .build()
            }

            val refresh = sessionStore.refreshToken ?: return null
            val newToken = refreshBlocking(refresh) ?: return null
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }
    }

    private fun refreshBlocking(refreshToken: String): String? {
        return runCatching {
            val body = gson.toJson(RefreshRequest(refreshToken))
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(baseUrl.trimEnd('/') + "/auth/refresh")
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val payload = response.body?.string() ?: return null
                val parsed = gson.fromJson(payload, AuthResponse::class.java)
                val token = parsed.token ?: return null
                sessionStore.accessToken = token
                parsed.refreshToken?.let { sessionStore.refreshToken = it }
                token
            }
        }.getOrNull()
    }
}
