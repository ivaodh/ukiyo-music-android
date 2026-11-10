package app.ukiyo.music.data.api

import app.ukiyo.music.data.auth.SessionStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionStore: SessionStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        sessionStore.accessToken?.let { builder.header("Authorization", "Bearer $it") }
        sessionStore.deviceFingerprint?.let { builder.header("X-Ukiyo-Device", it) }
        return chain.proceed(builder.build())
    }
}
