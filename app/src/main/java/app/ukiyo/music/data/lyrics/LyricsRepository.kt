package app.ukiyo.music.data.lyrics

import app.ukiyo.music.data.api.Lyrics
import app.ukiyo.music.data.api.Track
import app.ukiyo.music.data.api.UkiyoApi

class LyricsRepository(private val api: UkiyoApi) {
    suspend fun fetch(track: Track): Lyrics? {
        return runCatching {
            val response = api.lyrics(
                artist = track.artist,
                title = track.title,
                album = track.album,
                durationMs = track.durationMs,
                videoId = track.videoId
            )
            val synced = response.syncedLrc
            val plain = response.plainLyrics
            if (synced.isNullOrBlank() && plain.isNullOrBlank()) null
            else Lyrics(syncedLrc = synced, plainLyrics = plain, source = response.source)
        }.getOrNull()
    }
}
