package app.ukiyo.music.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UkiyoApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): AuthResponse

    @GET("config")
    suspend fun config(): ConfigResponse

    @POST("devices/register")
    suspend fun registerDevice(@Body body: DeviceRequest): DeviceResponse

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("filter") filter: String? = "songs",
        @Query("continuation") continuation: String? = null
    ): SearchResponse

    @GET("library/liked-songs")
    suspend fun likedSongs(@Query("limit") limit: Int = 100): TrackListResponse

    @GET("library/history")
    suspend fun history(@Query("limit") limit: Int = 50): TrackListResponse

    @GET("playlists")
    suspend fun userPlaylists(): UserPlaylistResponse

    @GET("playlists/{id}/tracks")
    suspend fun userPlaylistTracks(@Path("id") id: String): TrackListResponse

    @GET("playlists/{id}")
    suspend fun ytPlaylist(@Path("id") id: String): TrackListResponse

    @POST("playlists")
    suspend fun createPlaylist(@Body body: CreatePlaylistRequest): CreatePlaylistResponse

    @POST("playlists/{id}/tracks")
    suspend fun addTrackToPlaylist(@Path("id") id: String, @Body body: AddTrackRequest): SimpleResponse

    @GET("home-feed")
    suspend fun homeFeed(): HomeFeedResponse

    @GET("charts")
    suspend fun charts(): TrackListResponse

    @GET("tracks/related")
    suspend fun related(@Query("id") id: String): TrackListResponse

    @POST("tracks/resolve")
    suspend fun resolve(@Body body: ResolveRequest): ResolveResponse

    @POST("library/like")
    suspend fun like(@Body body: LikeRequest): LikeResponse

    @POST("library/history/add")
    suspend fun addHistory(@Body body: LikeRequest): SimpleResponse

    @GET("tracks/lyrics")
    suspend fun lyrics(
        @Query("artist") artist: String,
        @Query("title") title: String,
        @Query("album") album: String? = null,
        @Query("durationMs") durationMs: Long? = null,
        @Query("videoId") videoId: String? = null
    ): LyricsResponse
}
