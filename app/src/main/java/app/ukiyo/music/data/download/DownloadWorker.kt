package app.ukiyo.music.data.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import app.ukiyo.music.R
import app.ukiyo.music.UkiyoApp
import app.ukiyo.music.data.api.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class DownloadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val container = (applicationContext as UkiyoApp).container
        val trackKey = inputData.getString(DownloadRepository.KEY_TRACK_KEY) ?: return@withContext Result.failure()
        val title = inputData.getString(DownloadRepository.KEY_TITLE) ?: return@withContext Result.failure()
        val artist = inputData.getString(DownloadRepository.KEY_ARTIST) ?: "Unknown Artist"
        val album = inputData.getString(DownloadRepository.KEY_ALBUM)
        val durationMs = inputData.getLong(DownloadRepository.KEY_DURATION, 0L).takeIf { it > 0 }
        val thumbnailUrl = inputData.getString(DownloadRepository.KEY_THUMB)
        val videoId = inputData.getString(DownloadRepository.KEY_VIDEO_ID)
        val quality = inputData.getString(DownloadRepository.KEY_QUALITY) ?: "eq+"
        val overrideId = inputData.getString(DownloadRepository.KEY_OVERRIDE_ID)

        val track = Track(
            key = trackKey,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            thumbnailUrl = thumbnailUrl,
            videoId = videoId,
            overrideId = overrideId
        )

        setForeground(createForegroundInfo(title))

        val dir = File(applicationContext.filesDir, "downloads").apply { mkdirs() }
        val target = File(dir, safeName(trackKey) + DownloadRepository.extensionFor(quality))
        val temp = File(dir, safeName(trackKey) + ".part")

        try {
            val stream = container.libraryRepository.resolve(track, quality)
            val deviceId = container.sessionStore.deviceFingerprint
            val request = Request.Builder()
                .url(stream.url)
                .apply { if (deviceId != null) header("X-Ukiyo-Device", deviceId) }
                .build()
            container.downloadClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.failure()
                val body = response.body ?: return@withContext Result.failure()
                temp.outputStream().use { output ->
                    body.byteStream().use { input -> input.copyTo(output) }
                }
            }
            if (temp.length() <= 0) return@withContext Result.failure()
            if (target.exists()) target.delete()
            if (!temp.renameTo(target)) {
                temp.copyTo(target, overwrite = true)
                temp.delete()
            }
            container.database.downloadDao().upsert(
                DownloadEntity(
                    trackKey = trackKey,
                    title = title,
                    artist = artist,
                    album = album,
                    durationMs = durationMs,
                    thumbnailUrl = thumbnailUrl,
                    videoId = videoId,
                    filePath = target.absolutePath,
                    quality = quality,
                    sizeBytes = target.length(),
                    createdAt = System.currentTimeMillis(),
                    lastPlayedAt = System.currentTimeMillis()
                )
            )
            container.downloadRepository.enforceCap()
            Result.success()
        } catch (e: Exception) {
            temp.delete()
            Result.failure()
        }
    }

    private fun safeName(value: String): String {
        return value.replace(Regex("[^A-Za-z0-9._-]"), "_").take(80).ifBlank { "track" }
    }

    private fun createForegroundInfo(title: String): ForegroundInfo {
        val context = applicationContext
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.download_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Downloading $title")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(0, 0, true)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "ukiyo_downloads"
        private const val NOTIFICATION_ID = 4202
    }
}
