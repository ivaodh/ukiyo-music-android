package app.ukiyo.music

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.ukiyo.music.data.api.HomeData
import app.ukiyo.music.data.api.Lyrics
import app.ukiyo.music.data.api.Track
import app.ukiyo.music.data.api.User
import app.ukiyo.music.data.auth.AuthResult
import app.ukiyo.music.data.download.DownloadEntity
import app.ukiyo.music.data.download.DownloadRepository
import app.ukiyo.music.playback.PlayerUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface AuthState {
    data object Loading : AuthState
    data object LoggedOut : AuthState
    data class LoggedIn(val user: User) : AuthState
}

data class HomeState(
    val loading: Boolean = false,
    val data: HomeData? = null,
    val error: String? = null
)

data class SearchState(
    val query: String = "",
    val loading: Boolean = false,
    val results: List<Track> = emptyList(),
    val error: String? = null
)

data class LibraryState(
    val loading: Boolean = false,
    val liked: List<Track> = emptyList(),
    val history: List<Track> = emptyList(),
    val playlists: List<app.ukiyo.music.data.api.UserPlaylist> = emptyList(),
    val error: String? = null
)

data class PlaylistState(
    val title: String = "",
    val tracks: List<Track> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

data class LyricsState(
    val trackKey: String? = null,
    val lyrics: Lyrics? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as UkiyoApp).container

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _home = MutableStateFlow(HomeState())
    val home: StateFlow<HomeState> = _home.asStateFlow()

    private val _search = MutableStateFlow(SearchState())
    val search: StateFlow<SearchState> = _search.asStateFlow()

    private val _library = MutableStateFlow(LibraryState())
    val library: StateFlow<LibraryState> = _library.asStateFlow()

    private val _playlist = MutableStateFlow(PlaylistState())
    val playlist: StateFlow<PlaylistState> = _playlist.asStateFlow()

    private val _lyrics = MutableStateFlow(LyricsState())
    val lyrics: StateFlow<LyricsState> = _lyrics.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    private val _loginLoading = MutableStateFlow(false)
    val loginLoading: StateFlow<Boolean> = _loginLoading.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    val config = container.configRepository.config
    val player: StateFlow<PlayerUiState> = container.playbackConnection.state
    val downloads: StateFlow<List<DownloadEntity>> = container.downloadRepository.downloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val downloadBytes: StateFlow<Long> = downloads
        .map { list -> list.sumOf { it.sizeBytes } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val quality: StateFlow<String> = container.settingsRepository.quality
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "eq+")
    val downloadWifiOnly: StateFlow<Boolean> = container.settingsRepository.downloadWifiOnly
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private var searchJob: Job? = null

    init {
        bootstrap()
    }

    private fun bootstrap() {
        viewModelScope.launch {
            container.configRepository.fetch()
            val user = container.authRepository.currentUser
            if (container.authRepository.isLoggedIn && user != null) {
                _authState.value = AuthState.LoggedIn(user)
                loadHome()
            } else {
                _authState.value = AuthState.LoggedOut
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            viewModelScope.launch { _messages.emit("Enter your email and password") }
            return
        }
        viewModelScope.launch {
            _loginLoading.value = true
            try {
                when (val result = container.authRepository.login(email, password)) {
                    is AuthResult.Success -> {
                        _authState.value = AuthState.LoggedIn(result.user)
                        loadHome()
                        _messages.emit("Welcome back")
                    }
                    is AuthResult.Failure -> _messages.emit(result.message)
                }
            } finally {
                _loginLoading.value = false
            }
        }
    }

    fun logout() {
        container.playbackConnection.stop()
        container.authRepository.logout()
        _authState.value = AuthState.LoggedOut
        _currentTrack.value = null
        _home.value = HomeState()
        _library.value = LibraryState()
        _search.value = SearchState()
        _playlist.value = PlaylistState()
        _lyrics.value = LyricsState()
    }

    fun loadHome() {
        if (_home.value.loading) return
        _home.value = _home.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { container.libraryRepository.home() }
                .onSuccess { data ->
                    _home.value = HomeState(loading = false, data = data)
                    _library.value = LibraryState(
                        loading = false,
                        liked = data.liked,
                        history = data.recent,
                        playlists = data.playlists
                    )
                }
                .onFailure { error ->
                    if (error.isUnauthorized()) logout()
                    else _home.value = HomeState(loading = false, data = _home.value.data, error = friendly(error))
                }
        }
    }

    fun loadLibrary() {
        _library.value = _library.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching {
                val liked = container.libraryRepository.liked()
                val history = container.libraryRepository.history()
                val playlists = container.libraryRepository.userPlaylists()
                Triple(liked, history, playlists)
            }.onSuccess { (liked, history, playlists) ->
                _library.value = LibraryState(false, liked, history, playlists)
            }.onFailure { error ->
                if (error.isUnauthorized()) logout()
                else _library.value = _library.value.copy(loading = false, error = friendly(error))
            }
        }
    }

    fun search(query: String) {
        _search.value = _search.value.copy(query = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _search.value = SearchState()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _search.value = _search.value.copy(loading = true, error = null)
            runCatching { container.libraryRepository.search(query) }
                .onSuccess { results -> _search.value = SearchState(query, false, results) }
                .onFailure { error ->
                    if (error.isUnauthorized()) logout()
                    else _search.value = _search.value.copy(loading = false, error = friendly(error))
                }
        }
    }

    fun openUserPlaylist(id: String, name: String) {
        _playlist.value = PlaylistState(title = name, loading = true)
        viewModelScope.launch {
            runCatching { container.libraryRepository.playlistTracks(id) }
                .onSuccess { tracks -> _playlist.value = PlaylistState(name, tracks) }
                .onFailure { error ->
                    if (error.isUnauthorized()) logout()
                    else _playlist.value = PlaylistState(name, emptyList(), false, friendly(error))
                }
        }
    }

    fun openFeedPlaylist(id: String, name: String) {
        _playlist.value = PlaylistState(title = name, loading = true)
        viewModelScope.launch {
            runCatching { container.libraryRepository.feedPlaylistTracks(id) }
                .onSuccess { tracks -> _playlist.value = PlaylistState(name, tracks) }
                .onFailure { error ->
                    if (error.isUnauthorized()) logout()
                    else _playlist.value = PlaylistState(name, emptyList(), false, friendly(error))
                }
        }
    }

    fun openTracks(title: String, tracks: List<Track>) {
        _playlist.value = PlaylistState(title = title, tracks = tracks)
    }

    fun playTrack(track: Track, queue: List<Track> = listOf(track)) {
        val index = queue.indexOfFirst { it.key == track.key }.coerceAtLeast(0)
        _currentTrack.value = track
        container.playbackConnection.playQueue(queue, index)
        viewModelScope.launch { container.libraryRepository.addHistory(track) }
    }

    fun playPlaylist(tracks: List<Track>) {
        if (tracks.isEmpty()) return
        _currentTrack.value = tracks.first()
        container.playbackConnection.playQueue(tracks, 0)
        viewModelScope.launch { container.libraryRepository.addHistory(tracks.first()) }
    }

    fun playDownload(entity: DownloadEntity) {
        _currentTrack.value = Track(
            key = entity.trackKey,
            title = entity.title,
            artist = entity.artist,
            album = entity.album,
            durationMs = entity.durationMs,
            thumbnailUrl = entity.thumbnailUrl,
            videoId = entity.videoId
        )
        container.playbackConnection.playDownload(entity)
        viewModelScope.launch { container.downloadRepository.touch(entity.trackKey) }
    }

    fun playQueueIndex(index: Int) {
        container.playbackConnection.seekToQueueItem(index)
    }

    fun togglePlayPause() {
        container.playbackConnection.togglePlayPause()
    }

    fun next() {
        container.playbackConnection.next()
    }

    fun previous() {
        container.playbackConnection.previous()
    }

    fun seek(positionMs: Long) {
        container.playbackConnection.seekTo(positionMs)
    }

    fun toggleShuffle() {
        container.playbackConnection.toggleShuffle()
    }

    fun cycleRepeat() {
        container.playbackConnection.cycleRepeatMode()
    }

    fun toggleLike(track: Track) {
        viewModelScope.launch {
            runCatching { container.libraryRepository.toggleLike(track) }
                .onSuccess { liked -> _messages.emit(if (liked == true) "Added to Liked Songs" else "Removed from Liked Songs") }
                .onFailure { _messages.emit(friendly(it)) }
        }
    }

    fun download(track: Track) {
        viewModelScope.launch {
            val selectedQuality = quality.value
            container.downloadRepository.download(track, selectedQuality)
            _messages.emit("Download started")
        }
    }

    fun deleteDownload(trackKey: String) {
        viewModelScope.launch { container.downloadRepository.delete(trackKey) }
    }

    fun clearDownloads() {
        viewModelScope.launch { container.downloadRepository.clearAll() }
    }

    fun loadLyrics(track: Track) {
        if (_lyrics.value.trackKey == track.key && (_lyrics.value.loading || _lyrics.value.lyrics != null)) return
        _lyrics.value = LyricsState(trackKey = track.key, loading = true)
        viewModelScope.launch {
            val lyrics = container.lyricsRepository.fetch(track)
            _lyrics.value = LyricsState(
                trackKey = track.key,
                lyrics = lyrics,
                loading = false,
                error = if (lyrics == null) "No lyrics found" else null
            )
        }
    }

    fun setQuality(value: String) {
        viewModelScope.launch { container.settingsRepository.setQuality(value) }
    }

    fun setDownloadWifiOnly(value: Boolean) {
        viewModelScope.launch { container.settingsRepository.setDownloadWifiOnly(value) }
    }

    fun refreshConfig() {
        viewModelScope.launch { container.configRepository.fetch() }
    }

    private fun Throwable.isUnauthorized(): Boolean {
        return this is HttpException && code() == 401
    }

    private fun friendly(error: Throwable): String {
        return when (error) {
            is HttpException -> when (error.code()) {
                401 -> "Session expired. Please sign in again."
                403 -> "This device is not allowed. Open Settings and check your account."
                429 -> "Too many requests. Please slow down."
                else -> "Server error (${error.code()})"
            }
            else -> error.message ?: "Something went wrong"
        }
    }
}
