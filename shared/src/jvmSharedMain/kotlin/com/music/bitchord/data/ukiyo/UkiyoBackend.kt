package com.music.bitchord.data.ukiyo

import com.music.bitchord.data.DebugLog as Log
import com.music.bitchord.data.Http
import com.music.bitchord.data.YtMusicRepository
import com.music.bitchord.data.model.ArtistPage
import com.music.bitchord.data.model.BrowseItem
import com.music.bitchord.data.model.BrowseType
import com.music.bitchord.data.model.HomeFeed
import com.music.bitchord.data.model.HomeShelf
import com.music.bitchord.data.model.LibraryPage
import com.music.bitchord.data.model.MoodGenre
import com.music.bitchord.data.model.MoodGenreSection
import com.music.bitchord.data.model.SearchResult
import com.music.bitchord.data.model.ShelfItem
import com.music.bitchord.data.model.ShelfLayout
import com.music.bitchord.data.model.Song
import com.music.bitchord.data.model.UserPlaylist
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

@Serializable
private data class UkiyoTrack(
    val id: String? = null,
    @SerialName("track_key") val trackKey: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val artists: List<UkiyoArtistRef> = emptyList(),
    val album: JsonElement? = null,
    @SerialName("duration_ms") val durationMsSnake: Long? = null,
    @SerialName("durationMs") val durationMsCamel: Long? = null,
    @SerialName("durationSeconds") val durationSeconds: Long? = null,
    @SerialName("thumbnail_url") val thumbnailUrlSnake: String? = null,
    @SerialName("thumbnailUrl") val thumbnailUrlCamel: String? = null,
    val thumbnails: List<UkiyoThumb> = emptyList(),
    @SerialName("video_id") val videoIdSnake: String? = null,
    @SerialName("videoId") val videoIdCamel: String? = null,
    val name: String? = null,
    val type: String? = null,
)

@Serializable
private data class UkiyoArtistRef(
    val name: String? = null,
    val id: String? = null,
)

@Serializable
private data class UkiyoThumb(val url: String? = null)

@Serializable
private data class UkiyoReleaseInfo(
    val title: String? = null,
    val description: String? = null,
    val thumbnails: List<UkiyoThumb> = emptyList(),
)

@Serializable
private data class UkiyoReleaseResponse(
    val tracks: List<UkiyoTrack> = emptyList(),
    val playlist: UkiyoReleaseInfo? = null,
)

@Serializable
private data class UkiyoArtist(
    val id: String? = null,
    val name: String? = null,
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null,
)

@Serializable
private data class UkiyoArtistResponse(
    val artist: UkiyoArtist? = null,
    val tracks: List<UkiyoTrack> = emptyList(),
    val name: String? = null,
    val moreEndpoint: UkiyoArtistMore? = null,
)

@Serializable
private data class UkiyoArtistMore(
    val browseId: String? = null,
    val params: String? = null,
)

@Serializable
private data class UkiyoArtistMoreRequest(
    val browseId: String,
    val params: String,
    val name: String? = null,
)

data class UkiyoReleasePage(
    val songs: List<Song>,
    val title: String? = null,
    val subtitle: String? = null,
    val thumbnailUrl: String? = null,
    val description: String? = null,
)

@Serializable
private data class UkiyoTrackList(val tracks: List<UkiyoTrack> = emptyList())

@Serializable
private data class UkiyoFeedPlaylist(
    val id: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    @SerialName("thumbnailUrl") val thumbnailUrl: String? = null,
    val category: String? = null,
)

@Serializable
private data class UkiyoHomeFeed(
    val playlists: List<UkiyoFeedPlaylist> = emptyList(),
    val region: String? = null,
)

@Serializable
private data class UkiyoPlaylist(
    val id: String? = null,
    val name: String? = null,
    @SerialName("trackThumbnails") val trackThumbnails: List<String> = emptyList(),
)

@Serializable
private data class UkiyoPlaylistResponse(val playlists: List<UkiyoPlaylist> = emptyList())

@Serializable
private data class UkiyoResolveRequest(
    val artist: String,
    val title: String,
    val album: String? = null,
    val durationMs: Long? = null,
    val videoId: String? = null,
    val qualityPreference: String = "eq+",
)

@Serializable
private data class UkiyoFormat(
    val codec: String? = null,
    @SerialName("bitrateKbps") val bitrateKbps: Int? = null,
)

@Serializable
private data class UkiyoResolveResponse(
    val t: Int? = null,
    val u: String? = null,
    val f: UkiyoFormat? = null,
    val source: String? = null,
    val expiresAt: Long? = null,
    val error: String? = null,
    val message: String? = null,
)

data class UkiyoStream(
    val url: String,
    val codec: String?,
    val kbps: Int?,
    val headers: Map<String, String>,
)

@Serializable
private data class UkiyoSearch(
    @SerialName("topResult") val topResult: UkiyoTrack? = null,
    val songs: List<UkiyoTrack> = emptyList(),
    val artists: List<UkiyoTrack> = emptyList(),
    val albums: List<UkiyoTrack> = emptyList(),
    val playlists: List<UkiyoTrack> = emptyList(),
    val continuation: String? = null,
)

private val json = Json { ignoreUnknownKeys = true }
private val USER_PLAYLIST_ID = Regex(
    "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
)

object UkiyoBackend {

    suspend fun home(): Result<HomeFeed> = runCatching {
        requireReady()
        val history = get<UkiyoTrackList>("/library/history?limit=20").tracks
        val liked = get<UkiyoTrackList>("/library/liked-songs?limit=50").tracks
        val playlists = get<UkiyoPlaylistResponse>("/playlists").playlists
        val feed = get<UkiyoHomeFeed>("/home-feed").playlists
        val charts = get<UkiyoTrackList>("/charts").tracks

        val shelves = buildList {
            if (history.isNotEmpty()) {
                add(
                    HomeShelf(
                        title = YtMusicRepository.RECENT_TITLE,
                        items = history.map { it.toShelfItem() },
                        layout = ShelfLayout.LIST,
                    )
                )
            }
            if (liked.isNotEmpty()) {
                add(
                    HomeShelf(
                        title = "Liked songs",
                        items = liked.map { it.toShelfItem() },
                        layout = ShelfLayout.LIST,
                    )
                )
            }
            if (playlists.isNotEmpty()) {
                add(
                    HomeShelf(
                        title = "Your playlists",
                        items = playlists.map { it.toShelfItem() },
                        layout = ShelfLayout.CARDS,
                    )
                )
            }
            val madeForYou = feed.map { it.toShelfItem() }
            if (madeForYou.isNotEmpty()) {
                add(HomeShelf(title = "Made for you", items = madeForYou, layout = ShelfLayout.CARDS))
            }
            if (charts.isNotEmpty()) {
                add(
                    HomeShelf(
                        title = "Trending now",
                        items = charts.map { it.toShelfItem() },
                        layout = ShelfLayout.LIST,
                    )
                )
            }
        }
        HomeFeed(shelves = shelves, continuation = null)
    }

    suspend fun homeRecentlyPlayed(): Result<HomeShelf?> = runCatching {
        requireReady()
        val history = get<UkiyoTrackList>("/library/history?limit=20").tracks
        if (history.isEmpty()) null
        else HomeShelf(
            title = YtMusicRepository.RECENT_TITLE,
            items = history.map { it.toShelfItem() },
            layout = ShelfLayout.LIST,
        )
    }

    suspend fun homeSupplement(): Result<List<HomeShelf>> = Result.success(emptyList())

    suspend fun moreHome(): Result<HomeFeed> = Result.success(HomeFeed(emptyList(), null))

    suspend fun searchPage(query: String, filter: String): Result<YtMusicRepository.SearchPage> = runCatching {
        requireReady()
        val result = get<UkiyoSearch>(
            "/search?q=${encode(query)}&filter=${encode(filter)}"
        )
        val rows = buildList {
            result.topResult?.let { add(SearchResult.TopTrack(it.toSong())) }
            result.songs.forEach { add(SearchResult.Track(it.toSong())) }
            result.artists.forEach {
                add(SearchResult.Browse(it.toBrowseItem(BrowseType.ARTIST, "Artist")))
            }
            result.albums.forEach {
                add(SearchResult.Browse(it.toBrowseItem(BrowseType.ALBUM, "Album")))
            }
            result.playlists.forEach {
                add(SearchResult.Browse(it.toBrowseItem(BrowseType.PLAYLIST, "Playlist")))
            }
        }.distinctBy { row ->
            when (row) {
                is SearchResult.TopTrack -> "v:${row.song.videoId}"
                is SearchResult.Track -> "v:${row.song.videoId}"
                is SearchResult.Browse -> "b:${row.item.browseId}"
            }
        }
        YtMusicRepository.SearchPage(rows = rows, continuation = null)
    }

    suspend fun searchSuggestions(): Result<List<String>> = Result.success(emptyList())

    suspend fun library(): Result<LibraryPage> = runCatching {
        requireReady()
        val liked = get<UkiyoTrackList>("/library/liked-songs?limit=100").tracks.map { it.toSong() }
        val playlists = get<UkiyoPlaylistResponse>("/playlists").playlists
        val shelves = if (playlists.isEmpty()) emptyList() else listOf(
            HomeShelf(
                title = "Your playlists",
                items = playlists.map { it.toShelfItem() },
                layout = ShelfLayout.CARDS,
            )
        )
        LibraryPage(likedSongs = liked, librarySongs = liked, shelves = shelves)
    }

    suspend fun history(): Result<List<Song>> = runCatching {
        requireReady()
        get<UkiyoTrackList>("/library/history?limit=100").tracks.map { it.toSong() }
    }

    suspend fun releasePage(browseId: String): Result<UkiyoReleasePage> = runCatching {
        requireReady()
        val key = browseKey(browseId)
        val id = key?.second?.takeIf { it.isNotBlank() } ?: browseId
        val userPlaylist = key?.first == "playlist" && USER_PLAYLIST_ID.matches(id)
        val response = if (userPlaylist) {
            val tracks = get<UkiyoTrackList>("/playlists/${encode(id)}/tracks").tracks
            val title = get<UkiyoPlaylistResponse>("/playlists").playlists
                .firstOrNull { it.id == id }?.name
            UkiyoReleaseResponse(tracks = tracks, playlist = UkiyoReleaseInfo(title = title))
        } else {
            get<UkiyoReleaseResponse>("/playlists/${encode(id)}")
        }
        UkiyoReleasePage(
            songs = response.tracks.map { it.toSong() },
            title = response.playlist?.title,
            thumbnailUrl = response.playlist?.thumbnails?.lastOrNull()?.url,
            description = response.playlist?.description,
        )
    }

    suspend fun artistPage(browseId: String): Result<ArtistPage> = runCatching {
        requireReady()
        val id = browseKey(browseId)?.second?.takeIf { it.isNotBlank() } ?: browseId
        val response = get<UkiyoArtistResponse>("/artists/${encode(id)}")
        val name = response.artist?.name ?: response.name
        val direct = response.tracks.map { it.toSong() }
        val more = response.moreEndpoint?.let { endpoint ->
            val browseId = endpoint.browseId
            val params = endpoint.params
            if (browseId.isNullOrBlank() || params.isNullOrBlank()) {
                emptyList()
            } else {
                runCatching {
                    post<UkiyoArtistMoreRequest, UkiyoTrackList>(
                        "/artists/more",
                        UkiyoArtistMoreRequest(browseId = browseId, params = params, name = name),
                    ).tracks.map { it.toSong() }
                }.getOrDefault(emptyList())
            }
        }.orEmpty()
        val songs = (direct + more).distinctBy { it.videoId }.take(50)
        ArtistPage(
            songs = songs,
            moreSongsBrowseId = null,
            sections = emptyList(),
            thumbnailUrl = response.artist?.thumbnailUrl,
            name = name,
        )
    }

    suspend fun userPlaylists(): Result<List<UserPlaylist>> = runCatching {
        requireReady()
        get<UkiyoPlaylistResponse>("/playlists").playlists.map {
            UserPlaylist(
                playlistId = it.id.orEmpty(),
                title = it.name ?: "Playlist",
                subtitle = "Playlist",
                thumbnailUrl = it.trackThumbnails.firstOrNull(),
            )
        }
    }

    suspend fun quickPicks(): Result<List<Song>> = runCatching {
        requireReady()
        val history = get<UkiyoTrackList>("/library/history?limit=8").tracks
        val liked = get<UkiyoTrackList>("/library/liked-songs?limit=8").tracks
        (history + liked).distinctBy { it.trackKey ?: it.id }.take(10).map { it.toSong() }
    }

    suspend fun moodAndGenres(): Result<List<MoodGenreSection>> = Result.success(
        listOf(
            MoodGenreSection(
                title = "Moods & genres",
                items = listOf(
                    "Bollywood",
                    "Punjabi",
                    "Hindi",
                    "English",
                    "Romance",
                    "Chill",
                    "Party",
                ).map { name ->
                    MoodGenre(
                        title = name,
                        browseId = "ukiyo:search:$name",
                        params = null,
                        thumbnailUrl = null,
                    )
                },
            )
        )
    )

    suspend fun resolve(
        artist: String,
        title: String,
        album: String?,
        durationMs: Long?,
        videoId: String?,
        quality: String = UkiyoSession.quality.id,
    ): Result<UkiyoStream> = runCatching {
        requireReady()
        val response = post<UkiyoResolveRequest, UkiyoResolveResponse>(
            "/tracks/resolve",
            UkiyoResolveRequest(
                artist = artist,
                title = title,
                album = album,
                durationMs = durationMs,
                videoId = videoId,
                qualityPreference = quality,
            ),
        )
        val url = response.u
        if (response.t != 1 || url.isNullOrBlank()) {
            throw IllegalStateException(response.message ?: response.error ?: "Ukiyo stream not found")
        }
        UkiyoStream(
            url = url,
            codec = response.f?.codec,
            kbps = response.f?.bitrateKbps,
            headers = buildMap {
                UkiyoSession.deviceFingerprint?.let { put("X-Ukiyo-Device", it) }
            },
        )
    }

    suspend fun moodGenreShelves(browseId: String): Result<List<HomeShelf>> = runCatching {
        requireReady()
        val query = browseId.substringAfterLast(':')
        val page = searchPage(query, "all").getOrThrow()
        val songs = page.rows.mapNotNull { row ->
            when (row) {
                is SearchResult.Track -> row.song
                is SearchResult.TopTrack -> row.song
                else -> null
            }
        }.distinctBy { it.videoId }
        val browse = page.rows.filterIsInstance<SearchResult.Browse>().map { it.item }
        val albums = browse.filter { it.type == BrowseType.ALBUM }
        val artists = browse.filter { it.type == BrowseType.ARTIST }
        val playlists = browse.filter { it.type == BrowseType.PLAYLIST }

        buildList {
            if (songs.isNotEmpty()) {
                add(
                    HomeShelf(
                        title = "$query songs",
                        items = songs.map { it.toShelfItem() },
                        layout = ShelfLayout.LIST,
                    )
                )
            }
            if (albums.isNotEmpty()) {
                add(HomeShelf("$query albums", albums.map { it.toShelfItem() }, layout = ShelfLayout.CARDS))
            }
            if (artists.isNotEmpty()) {
                add(HomeShelf("$query artists", artists.map { it.toShelfItem() }, layout = ShelfLayout.CARDS))
            }
            if (playlists.isNotEmpty()) {
                add(HomeShelf("$query playlists", playlists.map { it.toShelfItem() }, layout = ShelfLayout.CARDS))
            }
        }
    }

    private suspend inline fun <reified T> get(path: String): T {
        val request = Request.Builder()
            .url("${UkiyoSession.baseUrl.trimEnd('/')}$path")
            .header("Accept", "application/json")
            .apply {
                UkiyoSession.token?.let { header("Authorization", "Bearer $it") }
                UkiyoSession.deviceFingerprint?.let { header("X-Ukiyo-Device", it) }
            }
            .build()

        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val call = Http.client.newCall(request)
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    if (cont.isActive) cont.resumeWith(Result.failure(e))
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.use {
                        val text = it.body.string()
                        if (!it.isSuccessful) {
                            if (cont.isActive) cont.resumeWith(
                                Result.failure(IllegalStateException("Ukiyo API ${it.code}: $text"))
                            )
                            return
                        }
                        val parsed = runCatching { json.decodeFromString<T>(text) }
                        if (cont.isActive) cont.resumeWith(parsed)
                    }
                }
            })
        }
    }

    private suspend inline fun <reified Req, reified Res> post(path: String, body: Req): Res {
        val payload = json.encodeToString(body)
        val request = Request.Builder()
            .url("${UkiyoSession.baseUrl.trimEnd('/')}$path")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .apply {
                UkiyoSession.token?.let { header("Authorization", "Bearer $it") }
                UkiyoSession.deviceFingerprint?.let { header("X-Ukiyo-Device", it) }
            }
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val call = Http.client.newCall(request)
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    if (cont.isActive) cont.resumeWith(Result.failure(e))
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    response.use {
                        val text = it.body.string()
                        if (!it.isSuccessful) {
                            if (cont.isActive) cont.resumeWith(
                                Result.failure(IllegalStateException("Ukiyo API ${it.code}: $text"))
                            )
                            return
                        }
                        val parsed = runCatching { json.decodeFromString<Res>(text) }
                        if (cont.isActive) cont.resumeWith(parsed)
                    }
                }
            })
        }
    }

    private fun requireReady() {
        check(UkiyoSession.isReady) { "Ukiyo session is not ready" }
    }

    private fun encode(value: String): String = java.net.URLEncoder.encode(value, "UTF-8")

    private fun browseKey(browseId: String): Pair<String, String>? {
        if (!browseId.startsWith("ukiyo:")) return null
        val rest = browseId.removePrefix("ukiyo:")
        val kind = rest.substringBefore(':')
        val id = rest.substringAfter(':', "")
        return kind to id
    }

    private fun UkiyoTrack.toSong(): Song {
        val resolvedArtist = cleanArtist(artist)
            ?: cleanArtist(artists.mapNotNull { it.name }.joinToString(", "))
            ?: "Unknown Artist"
        val resolvedTitle = title ?: name ?: "Unknown Track"
        val durationMs = durationMsCamel ?: durationMsSnake ?: durationSeconds?.times(1000)
        val id = videoIdCamel ?: videoIdSnake ?: trackKey ?: id ?: "$resolvedArtist|$resolvedTitle"
        return Song(
            videoId = id,
            title = resolvedTitle,
            artist = resolvedArtist,
            thumbnailUrl = thumbnailUrlCamel ?: thumbnailUrlSnake ?: thumbnails.lastOrNull()?.url,
            durationText = durationMs?.let(::formatDuration),
            albumName = albumName(),
        )
    }

    private fun cleanArtist(value: String?): String? = value
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.joinToString(", ")
        ?.takeIf { it.isNotEmpty() }

    private fun UkiyoTrack.albumName(): String? = when (val value = album) {
        is JsonPrimitive -> value.contentOrNull
        is JsonObject -> value["name"]?.jsonPrimitive?.contentOrNull
        else -> null
    }

    private fun UkiyoTrack.toShelfItem(): ShelfItem = toSong().toShelfItem()

    private fun Song.toShelfItem(): ShelfItem = ShelfItem(
        title = title,
        subtitle = artist,
        thumbnailUrl = thumbnailUrl,
        videoId = videoId,
        browseId = null,
    )

    private fun UkiyoPlaylist.toShelfItem(): ShelfItem = ShelfItem(
        title = name ?: "Playlist",
        subtitle = "Playlist",
        thumbnailUrl = trackThumbnails.firstOrNull(),
        videoId = null,
        browseId = "ukiyo:playlist:${id.orEmpty()}",
    )

    private fun UkiyoFeedPlaylist.toShelfItem(): ShelfItem = ShelfItem(
        title = title ?: "Playlist",
        subtitle = (subtitle ?: category.orEmpty())
            .replace("YouTube Charts", "Charts")
            .replace("YouTube Music", "Ukiyo Music"),
        thumbnailUrl = thumbnailUrl,
        videoId = null,
        browseId = "ukiyo:feed:${id.orEmpty()}",
    )

    private fun BrowseItem.toShelfItem(): ShelfItem = ShelfItem(
        title = title,
        subtitle = subtitle,
        thumbnailUrl = thumbnailUrl,
        videoId = null,
        browseId = browseId,
    )

    private fun UkiyoTrack.toBrowseItem(type: BrowseType, subtitle: String): BrowseItem = BrowseItem(
        browseId = "ukiyo:${type.name.lowercase()}:${id ?: trackKey.orEmpty()}",
        title = title ?: name ?: "Unknown",
        subtitle = subtitle,
        thumbnailUrl = thumbnailUrlCamel ?: thumbnailUrlSnake,
        type = type,
    )

    private fun formatDuration(ms: Long): String {
        if (ms <= 0) return ""
        val total = ms / 1000
        return String.format(Locale.US, "%d:%02d", total / 60, total % 60)
    }
}
