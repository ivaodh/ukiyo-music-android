package app.ukiyo.music.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.ukiyo.music.BuildConfig
import app.ukiyo.music.MainViewModel
import app.ukiyo.music.data.api.Track
import app.ukiyo.music.playback.PlayerUiState
import app.ukiyo.music.ui.components.FloatingDock
import app.ukiyo.music.ui.downloads.DownloadsScreen
import app.ukiyo.music.ui.home.HomeScreen
import app.ukiyo.music.ui.library.LibraryScreen
import app.ukiyo.music.ui.login.LoginScreen
import app.ukiyo.music.ui.lyrics.LyricsScreen
import app.ukiyo.music.ui.player.NowPlayingScreen
import app.ukiyo.music.ui.playlist.PlaylistScreen
import app.ukiyo.music.ui.queue.QueueScreen
import app.ukiyo.music.ui.search.SearchScreen
import app.ukiyo.music.ui.settings.SettingsScreen
import app.ukiyo.music.ui.theme.UkiyoBlack
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun UkiyoRoot(viewModel: MainViewModel) {
    val context = LocalContext.current
    val config by viewModel.config.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val loginLoading by viewModel.loginLoading.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }
        LaunchedEffect(Unit) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UkiyoBlack)
    ) {
        when {
            BuildConfig.VERSION_CODE < config.minVersionCode -> {
                UpdateScreen(onUpdate = {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}"))
                        )
                    }
                })
            }
            !config.enabled -> {
                MaintenanceScreen(
                    message = config.maintenanceMessage,
                    onRetry = { viewModel.refreshConfig() }
                )
            }
            authState is app.ukiyo.music.AuthState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            authState is app.ukiyo.music.AuthState.LoggedOut -> {
                LoginScreen(loading = loginLoading, onLogin = viewModel::login)
            }
            authState is app.ukiyo.music.AuthState.LoggedIn -> {
                AppNavigation(
                    viewModel = viewModel,
                    userEmail = (authState as app.ukiyo.music.AuthState.LoggedIn).user.email
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun AppNavigation(viewModel: MainViewModel, userEmail: String) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: "home"
    val player by viewModel.player.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val downloadBytes by viewModel.downloadBytes.collectAsStateWithLifecycle()
    val quality by viewModel.quality.collectAsStateWithLifecycle()
    val downloadWifiOnly by viewModel.downloadWifiOnly.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()
    val playlistState by viewModel.playlist.collectAsStateWithLifecycle()
    val lyricsState by viewModel.lyrics.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()

    val showTabs = route in setOf("home", "search", "library", "downloads")
    val hazeState = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UkiyoBlack)
    ) {
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .hazeSource(hazeState)
        ) {
            composable("home") {
                val state by viewModel.home.collectAsStateWithLifecycle()
                HomeScreen(
                    state = state,
                    userEmail = userEmail,
                    onTrack = { track, queue ->
                        viewModel.playTrack(track, queue)
                        navController.navigate("player")
                    },
                    onFeedPlaylist = { playlist ->
                        viewModel.openFeedPlaylist(playlist.id, playlist.title)
                        navController.navigate("playlist")
                    },
                    onUserPlaylist = { playlist ->
                        viewModel.openUserPlaylist(playlist.id, playlist.name)
                        navController.navigate("playlist")
                    },
                    onSettings = { navController.navigate("settings") },
                    onRefresh = viewModel::loadHome
                )
            }
            composable("search") {
                val state by viewModel.search.collectAsStateWithLifecycle()
                SearchScreen(
                    state = state,
                    onQueryChange = viewModel::search,
                    onTrack = { track ->
                        viewModel.playTrack(track, state.results)
                        navController.navigate("player")
                    }
                )
            }
            composable("library") {
                val state by viewModel.library.collectAsStateWithLifecycle()
                LibraryScreen(
                    state = state,
                    onTrack = { track, queue ->
                        viewModel.playTrack(track, queue)
                        navController.navigate("player")
                    },
                    onOpenTracks = { title, tracks ->
                        viewModel.openTracks(title, tracks)
                        navController.navigate("playlist")
                    },
                    onOpenPlaylist = { playlist ->
                        viewModel.openUserPlaylist(playlist.id, playlist.name)
                        navController.navigate("playlist")
                    },
                    onSettings = { navController.navigate("settings") },
                    onRefresh = viewModel::loadLibrary
                )
            }
            composable("downloads") {
                DownloadsScreen(
                    downloads = downloads,
                    totalBytes = downloadBytes,
                    onPlay = {
                        viewModel.playDownload(it)
                        navController.navigate("player")
                    },
                    onDelete = viewModel::deleteDownload,
                    onClearAll = viewModel::clearDownloads,
                    onSettings = { navController.navigate("settings") }
                )
            }
            composable("playlist") {
                PlaylistScreen(
                    state = playlistState,
                    onTrack = { track ->
                        viewModel.playTrack(track, playlistState.tracks)
                        navController.navigate("player")
                    },
                    onPlayAll = {
                        viewModel.playPlaylist(playlistState.tracks)
                        navController.navigate("player")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("player") {
                val track = currentTrack ?: player.asTrack()
                NowPlayingScreen(
                    state = player,
                    track = track,
                    onBack = { navController.popBackStack() },
                    onPlayPause = viewModel::togglePlayPause,
                    onNext = viewModel::next,
                    onPrevious = viewModel::previous,
                    onSeek = viewModel::seek,
                    onToggleShuffle = viewModel::toggleShuffle,
                    onCycleRepeat = viewModel::cycleRepeat,
                    onToggleLike = { track?.let(viewModel::toggleLike) },
                    onDownload = { track?.let(viewModel::download) },
                    onLyrics = {
                        if (track != null) {
                            viewModel.loadLyrics(track)
                            navController.navigate("lyrics")
                        }
                    },
                    onQueue = { navController.navigate("queue") }
                )
            }
            composable("queue") {
                QueueScreen(
                    state = player,
                    onQueueItem = viewModel::playQueueIndex,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("lyrics") {
                val track = currentTrack ?: player.asTrack()
                LyricsScreen(
                    track = track,
                    state = lyricsState,
                    positionMs = player.positionMs,
                    onLoad = viewModel::loadLyrics,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    quality = quality,
                    onQuality = viewModel::setQuality,
                    downloadWifiOnly = downloadWifiOnly,
                    onDownloadWifiOnly = viewModel::setDownloadWifiOnly,
                    config = config,
                    userEmail = userEmail,
                    onRefreshConfig = viewModel::refreshConfig,
                    onLogout = {
                        viewModel.logout()
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        if (showTabs) {
            FloatingDock(
                hazeState = hazeState,
                player = player,
                currentRoute = route,
                onTab = { tab ->
                    navController.navigate(tab) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onOpenPlayer = { navController.navigate("player") },
                onPlayPause = viewModel::togglePlayPause,
                onNext = viewModel::next,
                onPrevious = viewModel::previous,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

private fun PlayerUiState.asTrack(): Track? {
    if (mediaId == null && title.isBlank()) return null
    return Track(
        key = mediaId ?: "$artist|$title",
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs.takeIf { it > 0 },
        thumbnailUrl = artworkUri,
        videoId = null
    )
}
