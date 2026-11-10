package com.music.bitchord.data.ukiyo

enum class UkiyoQuality(
    val id: String,
    val label: String,
    val detail: String,
) {
    EQ_PLUS("eq+", "EQ+", "320 kbps. Best balance for daily listening."),
    FLAC("eq", "FLAC", "Lossless. Uses more data and storage."),
    AAC("aac", "AAC", "128 kbps. Data saver."),
}
