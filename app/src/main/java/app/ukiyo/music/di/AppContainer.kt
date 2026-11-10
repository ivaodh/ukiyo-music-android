package app.ukiyo.music.di

import android.content.Context
import androidx.room.Room
import app.ukiyo.music.BuildConfig
import app.ukiyo.music.data.api.AuthInterceptor
import app.ukiyo.music.data.api.TokenAuthenticator
import app.ukiyo.music.data.api.UkiyoApi
import app.ukiyo.music.data.auth.AuthRepository
import app.ukiyo.music.data.auth.SessionStore
import app.ukiyo.music.data.config.ConfigRepository
import app.ukiyo.music.data.download.DownloadRepository
import app.ukiyo.music.data.download.UkiyoDatabase
import app.ukiyo.music.data.library.LibraryRepository
import app.ukiyo.music.data.lyrics.LyricsRepository
import app.ukiyo.music.data.settings.SettingsRepository
import app.ukiyo.music.playback.PlaybackConnection
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val gson = Gson()

    val sessionStore = SessionStore(appContext)
    val settingsRepository = SettingsRepository(appContext)

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(sessionStore))
        .authenticator(TokenAuthenticator(sessionStore, BuildConfig.API_BASE_URL))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val downloadClient: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL.trimEnd('/') + "/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val api: UkiyoApi = retrofit.create(UkiyoApi::class.java)

    val database: UkiyoDatabase = Room.databaseBuilder(
        appContext,
        UkiyoDatabase::class.java,
        "ukiyo.db"
    ).build()

    val downloadRepository = DownloadRepository(appContext, database.downloadDao())
    val authRepository = AuthRepository(appContext, api, sessionStore)
    val configRepository = ConfigRepository(api)
    val libraryRepository = LibraryRepository(api)
    val lyricsRepository = LyricsRepository(api)

    val playbackConnection: PlaybackConnection by lazy { PlaybackConnection(appContext) }
}
