package app.ukiyo.music.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DataSourceBitmapLoader
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import app.ukiyo.music.MainActivity
import app.ukiyo.music.UkiyoApp
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Callable
import java.util.concurrent.Executors

@androidx.annotation.OptIn(UnstableApi::class)
class PlaybackService : MediaLibraryService() {

    private lateinit var player: ExoPlayer
    private lateinit var librarySession: MediaLibrarySession
    private val executor = Executors.newFixedThreadPool(2)
    private val bitmapExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor())

    private val container by lazy { (application as UkiyoApp).container }

    override fun onCreate() {
        super.onCreate()

        val deviceId = container.authRepository.ensureDeviceFingerprint()
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(mapOf("X-Ukiyo-Device" to deviceId))

        val resolvingFactory = ResolvingDataSourceFactory(
            context = this,
            libraryRepository = container.libraryRepository,
            settingsRepository = container.settingsRepository,
            httpDataSourceFactory = httpDataSourceFactory
        )

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(resolvingFactory))
            .build()
            .apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true
                )
                setHandleAudioBecomingNoisy(true)
                setWakeMode(C.WAKE_MODE_NETWORK)
            }

        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        librarySession = MediaLibrarySession.Builder(this, player, libraryCallback)
            .setSessionActivity(sessionActivity)
            .setBitmapLoader(DataSourceBitmapLoader(bitmapExecutor, DefaultDataSource.Factory(this)))
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return librarySession
    }

    override fun onDestroy() {
        librarySession.release()
        player.release()
        executor.shutdownNow()
        bitmapExecutor.shutdownNow()
        super.onDestroy()
    }

    private val libraryCallback = object : MediaLibrarySession.Callback {
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(
                LibraryResult.ofItem(MediaItemFactory.folder(ROOT_ID, "Ukiyo Music"), params)
            )
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return Futures.submit(
                Callable { LibraryResult.ofItemList(loadChildren(parentId), params) },
                executor
            )
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            return Futures.immediateFuture(LibraryResult.ofVoid(params))
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return Futures.submit(
                Callable { LibraryResult.ofItemList(search(query), params) },
                executor
            )
        }
    }

    private fun loadChildren(parentId: String): ImmutableList<MediaItem> {
        if (!container.authRepository.isLoggedIn) {
            return ImmutableList.of(
                MediaItemFactory.message("signin", "Open Ukiyo Music on your phone to sign in")
            )
        }
        return try {
            val items = when {
                parentId == ROOT_ID -> listOf(
                    MediaItemFactory.folder("home", "Home"),
                    MediaItemFactory.folder("liked", "Liked Songs"),
                    MediaItemFactory.folder("history", "Recently Played"),
                    MediaItemFactory.folder("playlists", "Playlists"),
                    MediaItemFactory.folder("downloads", "Downloads")
                )
                parentId == "home" -> runBlocking { container.libraryRepository.home().recent }
                    .map(MediaItemFactory::track)
                parentId == "liked" -> runBlocking { container.libraryRepository.liked() }
                    .map(MediaItemFactory::track)
                parentId == "history" -> runBlocking { container.libraryRepository.history() }
                    .map(MediaItemFactory::track)
                parentId == "playlists" -> runBlocking { container.libraryRepository.userPlaylists() }
                    .map { MediaItemFactory.folder("playlist:${it.id}", it.name) }
                parentId == "downloads" -> runBlocking { container.downloadRepository.all() }
                    .map(MediaItemFactory::download)
                parentId.startsWith("playlist:") -> runBlocking {
                    container.libraryRepository.playlistTracks(parentId.removePrefix("playlist:"))
                }.map(MediaItemFactory::track)
                else -> emptyList()
            }
            ImmutableList.copyOf(items)
        } catch (e: Exception) {
            ImmutableList.of(
                MediaItemFactory.message(
                    "error",
                    "Could not load Ukiyo Music. Open the app and check your connection."
                )
            )
        }
    }

    private fun search(query: String): ImmutableList<MediaItem> {
        if (!container.authRepository.isLoggedIn) {
            return ImmutableList.of(
                MediaItemFactory.message("signin", "Open Ukiyo Music on your phone to sign in")
            )
        }
        return try {
            val tracks = runBlocking { container.libraryRepository.search(query) }
            ImmutableList.copyOf(tracks.take(30).map(MediaItemFactory::track))
        } catch (e: Exception) {
            ImmutableList.of(
                MediaItemFactory.message("error", "Search is unavailable right now.")
            )
        }
    }

    companion object {
        private const val ROOT_ID = "ukiyo_root"
    }
}
