package app.ukiyo.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import app.ukiyo.music.di.AppContainer

class UkiyoApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        val playback = NotificationChannel(
            "ukiyo_playback",
            getString(R.string.playback_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = getString(R.string.playback_channel_description) }
        val downloads = NotificationChannel(
            "ukiyo_downloads",
            getString(R.string.download_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = getString(R.string.download_channel_description) }
        manager.createNotificationChannels(listOf(playback, downloads))
    }
}
