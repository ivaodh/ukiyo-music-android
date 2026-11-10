package app.ukiyo.music.playback

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import app.ukiyo.music.data.api.Track
import app.ukiyo.music.data.download.DownloadEntity
import java.io.File

object MediaItemFactory {
    fun track(track: Track): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .setArtworkUri(track.thumbnailUrl?.let(Uri::parse))
            .setIsBrowsable(false)
            .setIsPlayable(true)
        track.durationMs?.takeIf { it > 0 }?.let { metadataBuilder.setDurationMs(it) }
        return MediaItem.Builder()
            .setMediaId(track.key)
            .setUri(TrackUri.build(track))
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    fun download(entity: DownloadEntity): MediaItem {
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(entity.title)
            .setArtist(entity.artist)
            .setAlbumTitle(entity.album)
            .setArtworkUri(entity.thumbnailUrl?.let(Uri::parse))
            .setIsBrowsable(false)
            .setIsPlayable(true)
        entity.durationMs?.takeIf { it > 0 }?.let { metadataBuilder.setDurationMs(it) }
        return MediaItem.Builder()
            .setMediaId(entity.trackKey)
            .setUri(Uri.fromFile(File(entity.filePath)))
            .setMediaMetadata(metadataBuilder.build())
            .build()
    }

    fun folder(id: String, title: String): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
    }

    fun message(id: String, text: String): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(text)
                    .setIsBrowsable(false)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
    }
}
