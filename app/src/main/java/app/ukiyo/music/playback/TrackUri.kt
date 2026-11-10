package app.ukiyo.music.playback

import android.net.Uri
import app.ukiyo.music.data.api.Track

object TrackUri {
    const val SCHEME = "ukiyo"
    private const val AUTHORITY = "track"

    fun build(track: Track): Uri {
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(AUTHORITY)
            .appendQueryParameter("key", track.key)
            .appendQueryParameter("title", track.title)
            .appendQueryParameter("artist", track.artist)
            .appendQueryParameter("album", track.album.orEmpty())
            .appendQueryParameter("durationMs", (track.durationMs ?: 0L).toString())
            .appendQueryParameter("videoId", track.videoId.orEmpty())
            .appendQueryParameter("overrideId", track.overrideId.orEmpty())
            .build()
    }

    fun parse(uri: Uri): Track? {
        if (uri.scheme != SCHEME || uri.authority != AUTHORITY) return null
        val title = uri.getQueryParameter("title") ?: return null
        val artist = uri.getQueryParameter("artist") ?: return null
        val key = uri.getQueryParameter("key") ?: "$artist|$title"
        return Track(
            key = key,
            title = title,
            artist = artist,
            album = uri.getQueryParameter("album")?.takeIf { it.isNotBlank() },
            durationMs = uri.getQueryParameter("durationMs")?.toLongOrNull()?.takeIf { it > 0 },
            thumbnailUrl = null,
            videoId = uri.getQueryParameter("videoId")?.takeIf { it.isNotBlank() },
            overrideId = uri.getQueryParameter("overrideId")?.takeIf { it.isNotBlank() }
        )
    }
}
