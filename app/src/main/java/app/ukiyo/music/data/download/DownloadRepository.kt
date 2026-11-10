package app.ukiyo.music.data.download

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.ukiyo.music.data.api.Track
import kotlinx.coroutines.flow.Flow
import java.io.File

class DownloadRepository(
    private val context: Context,
    private val dao: DownloadDao
) {
    val downloads: Flow<List<DownloadEntity>> = dao.observeAll()

    fun download(track: Track, quality: String) {
        val data = workDataOf(
            KEY_TRACK_KEY to track.key,
            KEY_TITLE to track.title,
            KEY_ARTIST to track.artist,
            KEY_ALBUM to track.album,
            KEY_DURATION to (track.durationMs ?: 0L),
            KEY_THUMB to track.thumbnailUrl,
            KEY_VIDEO_ID to track.videoId,
            KEY_QUALITY to quality,
            KEY_OVERRIDE_ID to track.overrideId
        )
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(data)
            .addTag("ukiyo-download")
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "download:${track.key}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    suspend fun delete(trackKey: String) {
        dao.find(trackKey)?.let { entity ->
            runCatching { File(entity.filePath).delete() }
        }
        dao.delete(trackKey)
    }

    suspend fun clearAll() {
        dao.all().forEach { entity -> runCatching { File(entity.filePath).delete() } }
        dao.clear()
    }

    suspend fun touch(trackKey: String) {
        dao.touch(trackKey, System.currentTimeMillis())
    }

    suspend fun totalBytes(): Long = dao.totalBytes()

    suspend fun enforceCap(limitBytes: Long = MAX_BYTES) {
        var total = dao.totalBytes()
        while (total > limitBytes) {
            val oldest = dao.oldest() ?: break
            runCatching { File(oldest.filePath).delete() }
            dao.delete(oldest.trackKey)
            total = dao.totalBytes()
        }
    }

    suspend fun find(trackKey: String): DownloadEntity? = dao.find(trackKey)

    suspend fun all(): List<DownloadEntity> = dao.all()

    companion object {
        const val MAX_BYTES = 5L * 1024 * 1024 * 1024

        const val KEY_TRACK_KEY = "trackKey"
        const val KEY_TITLE = "title"
        const val KEY_ARTIST = "artist"
        const val KEY_ALBUM = "album"
        const val KEY_DURATION = "durationMs"
        const val KEY_THUMB = "thumbnailUrl"
        const val KEY_VIDEO_ID = "videoId"
        const val KEY_QUALITY = "quality"
        const val KEY_OVERRIDE_ID = "overrideId"

        fun extensionFor(quality: String): String {
            return if (quality == "eq" || quality == "flac-mini") ".flac" else ".m4a"
        }
    }
}
