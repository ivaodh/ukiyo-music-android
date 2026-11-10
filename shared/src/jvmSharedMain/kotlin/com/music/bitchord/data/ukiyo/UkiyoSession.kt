package com.music.bitchord.data.ukiyo

object UkiyoSession {
    @Volatile
    var token: String? = null

    @Volatile
    var deviceFingerprint: String? = null

    @Volatile
    var baseUrl: String = "https://api.sagaci.top/api/android/v1"

    val isReady: Boolean
        get() = !token.isNullOrBlank()
}
