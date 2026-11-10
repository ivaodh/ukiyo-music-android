package app.ukiyo.music.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import app.ukiyo.music.data.library.LibraryRepository
import app.ukiyo.music.data.settings.SettingsRepository
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

@androidx.annotation.OptIn(UnstableApi::class)
class ResolvingDataSourceFactory(
    private val context: Context,
    private val libraryRepository: LibraryRepository,
    private val settingsRepository: SettingsRepository,
    private val httpDataSourceFactory: DataSource.Factory
) : DataSource.Factory {

    private data class CachedUrl(val url: String, val validUntil: Long)

    private val cache = ConcurrentHashMap<String, CachedUrl>()

    override fun createDataSource(): DataSource {
        val upstream = DefaultDataSource.Factory(context, httpDataSourceFactory).createDataSource()
        return ResolvingDataSource(upstream) { dataSpec ->
            val uri = dataSpec.uri
            if (uri.scheme != TrackUri.SCHEME) {
                dataSpec
            } else {
                val track = TrackUri.parse(uri)
                if (track == null) {
                    dataSpec
                } else {
                    val key = uri.toString()
                    val now = System.currentTimeMillis()
                    val cached = cache[key]
                    val url = if (cached != null && cached.validUntil > now) {
                        cached.url
                    } else {
                        val quality = runBlocking { settingsRepository.currentQuality() }
                        val stream = runBlocking { libraryRepository.resolve(track, quality) }
                        val expires = stream.expiresAt ?: (now + 5 * 60 * 1000)
                        cache[key] = CachedUrl(stream.url, expires - 30_000)
                        stream.url
                    }
                    dataSpec.withUri(Uri.parse(url))
                }
            }
        }
    }
}
