package app.ukiyo.music.data.library

import app.ukiyo.music.data.api.AddTrackRequest
import app.ukiyo.music.data.api.CreatePlaylistRequest
import app.ukiyo.music.data.api.FeedPlaylist
import app.ukiyo.music.data.api.HomeData
import app.ukiyo.music.data.api.LikeRequest
import app.ukiyo.music.data.api.ResolveRequest
import app.ukiyo.music.data.api.StreamInfo
import app.ukiyo.music.data.api.Track
import app.ukiyo.music.data.api.TrackDto
import app.ukiyo.music.data.api.UkiyoApi
import app.ukiyo.music.data.api.UserPlaylist
import app.ukiyo.music.data.api.toFeedPlaylist
import app.ukiyo.music.data.api.toTrack
import app.ukiyo.music.data.api.toUserPlaylist
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class LibraryRepository(private val api: UkiyoApi) {

    suspend fun home(): HomeData = coroutineScope {
        val liked = async { runCatching { api.likedSongs(40).tracks.orEmpty().map(TrackDto::toTrack) }.getOrDefault(emptyList()) }
        val history = async { runCatching { api.history(30).tracks.orEmpty().map(TrackDto::toTrack) }.getOrDefault(emptyList()) }
        val playlists = async { runCatching { api.userPlaylists().playlists.orEmpty().map { it.toUserPlaylist() } }.getOrDefault(emptyList()) }
        val feed = async { runCatching { api.homeFeed().playlists.orEmpty().map { it.toFeedPlaylist() } }.getOrDefault(emptyList()) }
        HomeData(
            recent = history.await(),
            liked = liked.await(),
            playlists = playlists.await(),
            feed = feed.await()
        )
    }

    suspend fun liked(): List<Track> {
        return api.likedSongs(100).tracks.orEmpty().map(TrackDto::toTrack)
    }

    suspend fun history(): List<Track> {
        return api.history(100).tracks.orEmpty().map(TrackDto::toTrack)
    }

    suspend fun search(query: String): List<Track> {
        return api.search(query, "songs").songs.orEmpty().map(TrackDto::toTrack)
    }

    suspend fun related(videoId: String): List<Track> {
        return api.related(videoId).tracks.orEmpty().map(TrackDto::toTrack)
    }

    suspend fun userPlaylists(): List<UserPlaylist> {
        return api.userPlaylists().playlists.orEmpty().map { it.toUserPlaylist() }
    }

    suspend fun playlistTracks(id: String): List<Track> {
        return api.userPlaylistTracks(id).tracks.orEmpty().map(TrackDto::toTrack)
    }

    suspend fun feedPlaylistTracks(id: String): List<Track> {
        return api.ytPlaylist(id).tracks.orEmpty().map(TrackDto::toTrack)
    }

    suspend fun feed(): List<FeedPlaylist> {
        return api.homeFeed().playlists.orEmpty().map { it.toFeedPlaylist() }
    }

    suspend fun charts(): List<Track> {
        return api.charts().tracks.orEmpty().map(TrackDto::toTrack)
    }

    suspend fun createPlaylist(name: String): UserPlaylist? {
        return api.createPlaylist(CreatePlaylistRequest(name)).playlist?.toUserPlaylist()
    }

    suspend fun addTrackToPlaylist(playlistId: String, track: Track) {
        api.addTrackToPlaylist(
            playlistId,
            AddTrackRequest(
                artist = track.artist,
                title = track.title,
                album = track.album,
                durationMs = track.durationMs,
                thumbnailUrl = track.thumbnailUrl,
                videoId = track.videoId
            )
        )
    }

    suspend fun toggleLike(track: Track): Boolean? {
        return api.like(
            LikeRequest(
                artist = track.artist,
                title = track.title,
                album = track.album,
                durationMs = track.durationMs,
                thumbnailUrl = track.thumbnailUrl,
                videoId = track.videoId
            )
        ).liked
    }

    suspend fun addHistory(track: Track) {
        runCatching {
            api.addHistory(
                LikeRequest(
                    artist = track.artist,
                    title = track.title,
                    album = track.album,
                    durationMs = track.durationMs,
                    thumbnailUrl = track.thumbnailUrl,
                    videoId = track.videoId
                )
            )
        }
    }

    suspend fun resolve(track: Track, quality: String): StreamInfo {
        val response = api.resolve(
            ResolveRequest(
                artist = track.artist,
                title = track.title,
                album = track.album,
                durationMs = track.durationMs,
                videoId = track.videoId,
                qualityPreference = quality,
                overrideId = track.overrideId,
                fallbackMode = null
            )
        )
        val url = response.u
        if (response.t == 1 && !url.isNullOrBlank()) {
            return StreamInfo(url = url, expiresAt = response.expiresAt)
        }
        throw IllegalStateException(response.message ?: response.error ?: "Track source not found")
    }
}
