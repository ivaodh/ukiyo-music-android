package app.ukiyo.music.data.api

data class Track(
    val key: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationMs: Long?,
    val thumbnailUrl: String?,
    val videoId: String?,
    val overrideId: String? = null,
    val source: String? = null
)

data class User(
    val id: String,
    val email: String,
    val role: String
)

data class StreamInfo(
    val url: String,
    val expiresAt: Long?
)

data class HomeData(
    val recent: List<Track>,
    val liked: List<Track>,
    val playlists: List<UserPlaylist>,
    val feed: List<FeedPlaylist>
)

data class UserPlaylist(
    val id: String,
    val name: String,
    val thumbnails: List<String>
)

data class FeedPlaylist(
    val id: String,
    val title: String,
    val subtitle: String?,
    val thumbnailUrl: String?,
    val category: String?
)

data class Lyrics(
    val syncedLrc: String?,
    val plainLyrics: String?,
    val source: String?
)

fun TrackDto.toTrack(): Track {
    val resolvedArtist = artist
        ?: artists?.mapNotNull { it.name }?.joinToString(", ")
        ?: "Unknown Artist"
    val resolvedTitle = title ?: name ?: "Unknown Track"
    val resolvedThumb = thumbnailUrlCamel
        ?: thumbnailUrlSnake
        ?: thumbnails?.lastOrNull()?.url
    val resolvedDuration = durationMsCamel ?: durationMsSnake
    val resolvedVideoId = videoIdCamel
        ?: videoIdSnake
        ?: id?.takeIf { type == "song" && it.length == 11 }
    val resolvedKey = trackKey ?: "$resolvedArtist|$resolvedTitle|${album.orEmpty()}"
    return Track(
        key = resolvedKey,
        title = resolvedTitle,
        artist = resolvedArtist,
        album = album,
        durationMs = resolvedDuration,
        thumbnailUrl = resolvedThumb,
        videoId = resolvedVideoId
    )
}

fun UserPlaylistDto.toUserPlaylist(): UserPlaylist {
    return UserPlaylist(
        id = id.orEmpty(),
        name = name ?: "Playlist",
        thumbnails = trackThumbnails.orEmpty()
    )
}

fun FeedPlaylistDto.toFeedPlaylist(): FeedPlaylist {
    return FeedPlaylist(
        id = id.orEmpty(),
        title = title ?: "Playlist",
        subtitle = subtitle,
        thumbnailUrl = thumbnailUrl,
        category = category
    )
}
