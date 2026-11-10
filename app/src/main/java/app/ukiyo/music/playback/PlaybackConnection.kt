package app.ukiyo.music.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import app.ukiyo.music.data.api.Track
import app.ukiyo.music.data.download.DownloadEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class QueueItem(
    val mediaId: String?,
    val title: String,
    val artist: String,
    val artworkUri: String?
)

data class PlayerUiState(
    val connected: Boolean = false,
    val isPlaying: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val album: String? = null,
    val artworkUri: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val shuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val mediaId: String? = null,
    val currentIndex: Int = -1,
    val queue: List<QueueItem> = emptyList()
)

@androidx.annotation.OptIn(UnstableApi::class)
class PlaybackConnection(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var positionJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updateState()
        }
    }

    init {
        val token = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        val future = MediaController.Builder(appContext, token).buildAsync()
        future.addListener({
            runCatching { future.get() }.onSuccess { mediaController ->
                controller = mediaController
                mediaController.addListener(listener)
                _state.value = _state.value.copy(connected = true)
                updateState()
                startPositionUpdates()
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    private fun startPositionUpdates() {
        if (positionJob != null) return
        positionJob = scope.launch {
            while (isActive) {
                if (controller?.isPlaying == true) updateState()
                delay(500)
            }
        }
    }

    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        val mediaController = controller ?: return
        if (tracks.isEmpty()) return
        val items = tracks.map(MediaItemFactory::track)
        val safeIndex = startIndex.coerceIn(0, items.lastIndex)
        mediaController.setMediaItems(items, safeIndex, 0L)
        mediaController.prepare()
        mediaController.play()
        updateState()
    }

    fun playDownload(entity: DownloadEntity) {
        val mediaController = controller ?: return
        mediaController.setMediaItem(MediaItemFactory.download(entity))
        mediaController.prepare()
        mediaController.play()
        updateState()
    }

    fun togglePlayPause() {
        val mediaController = controller ?: return
        if (mediaController.isPlaying) mediaController.pause() else mediaController.play()
        updateState()
    }

    fun next() {
        controller?.seekToNextMediaItem()
        updateState()
    }

    fun seekToQueueItem(index: Int) {
        val mediaController = controller ?: return
        if (index !in 0 until mediaController.mediaItemCount) return
        mediaController.seekTo(index, 0L)
        mediaController.play()
        updateState()
    }

    fun previous() {
        controller?.seekToPreviousMediaItem()
        updateState()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        updateState()
    }

    fun toggleShuffle() {
        val mediaController = controller ?: return
        mediaController.shuffleModeEnabled = !mediaController.shuffleModeEnabled
        updateState()
    }

    fun cycleRepeatMode() {
        val mediaController = controller ?: return
        mediaController.repeatMode = when (mediaController.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        updateState()
    }

    fun stop() {
        val mediaController = controller ?: return
        mediaController.stop()
        mediaController.clearMediaItems()
        updateState()
    }

    private fun updateState() {
        val mediaController = controller ?: return
        val metadata = mediaController.currentMediaItem?.mediaMetadata
        val queue = buildList {
            val count = mediaController.mediaItemCount.coerceAtMost(100)
            for (index in 0 until count) {
                val item = mediaController.getMediaItemAt(index)
                val itemMetadata = item.mediaMetadata
                add(
                    QueueItem(
                        mediaId = item.mediaId,
                        title = itemMetadata.title?.toString().orEmpty(),
                        artist = itemMetadata.artist?.toString().orEmpty(),
                        artworkUri = itemMetadata.artworkUri?.toString()
                    )
                )
            }
        }
        _state.value = PlayerUiState(
            connected = true,
            isPlaying = mediaController.isPlaying,
            title = metadata?.title?.toString().orEmpty(),
            artist = metadata?.artist?.toString().orEmpty(),
            album = metadata?.albumTitle?.toString(),
            artworkUri = metadata?.artworkUri?.toString(),
            positionMs = mediaController.currentPosition.coerceAtLeast(0L),
            durationMs = mediaController.duration.takeIf { it > 0 } ?: 0L,
            hasNext = mediaController.hasNextMediaItem(),
            hasPrevious = mediaController.hasPreviousMediaItem(),
            shuffle = mediaController.shuffleModeEnabled,
            repeatMode = mediaController.repeatMode,
            mediaId = mediaController.currentMediaItem?.mediaId,
            currentIndex = mediaController.currentMediaItemIndex,
            queue = queue
        )
    }
}
