package app.ukiyo.music.data.api

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: String? = null,
    val email: String? = null,
    val role: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class AuthResponse(
    val ok: Boolean? = null,
    val token: String? = null,
    val refreshToken: String? = null,
    val user: UserDto? = null,
    val error: String? = null,
    val message: String? = null
)

data class ConfigResponse(
    val enabled: Boolean? = null,
    @SerializedName("minVersionCode") val minVersionCode: Int? = null,
    @SerializedName("maintenanceMessage") val maintenanceMessage: String? = null,
    @SerializedName("defaultQuality") val defaultQuality: String? = null,
    @SerializedName("maxDevices") val maxDevices: Int? = null,
    @SerializedName("serverTime") val serverTime: String? = null
)

data class DeviceRequest(
    val fingerprint: String,
    val deviceName: String,
    val appVersion: String
)

data class DeviceResponse(
    val ok: Boolean? = null,
    @SerializedName("deviceCount") val deviceCount: Int? = null,
    @SerializedName("maxDevices") val maxDevices: Int? = null,
    val error: String? = null,
    val message: String? = null
)

data class TrackDto(
    val id: String? = null,
    @SerializedName("track_key") val trackKey: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    @SerializedName("duration_ms") val durationMsSnake: Long? = null,
    @SerializedName("durationMs") val durationMsCamel: Long? = null,
    @SerializedName("thumbnail_url") val thumbnailUrlSnake: String? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrlCamel: String? = null,
    @SerializedName("video_id") val videoIdSnake: String? = null,
    @SerializedName("videoId") val videoIdCamel: String? = null,
    val artists: List<ArtistDto>? = null,
    val thumbnails: List<ThumbnailDto>? = null,
    val type: String? = null,
    val name: String? = null,
    val subtitle: String? = null
)

data class ArtistDto(
    val id: String? = null,
    val name: String? = null
)

data class ThumbnailDto(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

data class TrackListResponse(
    val tracks: List<TrackDto>? = null
)

data class SearchResponse(
    val topResult: TrackDto? = null,
    val songs: List<TrackDto>? = null,
    val artists: List<TrackDto>? = null,
    val albums: List<TrackDto>? = null,
    val playlists: List<TrackDto>? = null,
    val continuation: String? = null
)

data class FeedPlaylistDto(
    val id: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val thumbnailUrl: String? = null,
    val category: String? = null
)

data class HomeFeedResponse(
    val playlists: List<FeedPlaylistDto>? = null,
    val region: String? = null
)

data class UserPlaylistDto(
    val id: String? = null,
    val name: String? = null,
    @SerializedName("trackThumbnails") val trackThumbnails: List<String>? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class UserPlaylistResponse(
    val playlists: List<UserPlaylistDto>? = null
)

data class CreatePlaylistRequest(
    val name: String
)

data class CreatePlaylistResponse(
    val playlist: UserPlaylistDto? = null
)

data class AddTrackRequest(
    val artist: String,
    val title: String,
    val album: String?,
    val durationMs: Long?,
    val thumbnailUrl: String?,
    val videoId: String?
)

data class LikeRequest(
    val artist: String,
    val title: String,
    val album: String?,
    val durationMs: Long?,
    val thumbnailUrl: String?,
    val videoId: String?
)

data class LikeResponse(
    val liked: Boolean? = null
)

data class SimpleResponse(
    val success: Boolean? = null
)

data class ResolveRequest(
    val artist: String,
    val title: String,
    val album: String?,
    val durationMs: Long?,
    val videoId: String?,
    val qualityPreference: String,
    val overrideId: String? = null,
    val fallbackMode: String? = null
)

data class FormatDto(
    val codec: String? = null,
    @SerializedName("bitrateKbps") val bitrateKbps: Int? = null,
    @SerializedName("sampleRateHz") val sampleRateHz: Int? = null,
    @SerializedName("bitsPerSample") val bitsPerSample: Int? = null
)

data class ResolveResponse(
    val t: Int? = null,
    val u: String? = null,
    val c: String? = null,
    val f: FormatDto? = null,
    @SerializedName("trackId") val trackId: String? = null,
    val source: String? = null,
    val expiresAt: Long? = null,
    val error: String? = null,
    val message: String? = null
)

data class LyricsResponse(
    val source: String? = null,
    @SerializedName("syncedLrc") val syncedLrc: String? = null,
    @SerializedName("plainLyrics") val plainLyrics: String? = null,
    @SerializedName("neteaseId") val neteaseId: String? = null,
    val error: String? = null
)
