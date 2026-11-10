# Ukiyo Music Android

Native Kotlin client for Ukiyo Music.

- Application ID: `app.ukiyo.music`
- App name: Ukiyo Music
- Min SDK: 28
- Compile SDK: 37
- Target SDK: 36
- UI: Jetpack Compose with Haze glass and a BitChord-derived visual foundation
- Playback: Media3 ExoPlayer + MediaSession + MediaLibraryService
- Android Auto: media library and playback service
- Offline: Room + WorkManager, 5 GB cap
- No admin screens
- No crash reporting SDK

The existing PWA in `../Ukiyo_Music_App` is not modified by this project.

## License

The Android app is licensed under the GNU General Public License, version 3. See `LICENSE` and `NOTICE`.

Source code: https://github.com/ivaodh/ukiyo-music-android

Repository setup notes are in `docs/REPO_SETUP.md`.

The backend is private and lives in a separate repository. It is not part of this app repository.

## Backend API

The Android app talks only to:

```
https://api.sagaci.top/api/android/v1
```

The app source exposes this endpoint, but not the backend resolver logic. The backend is a separate program reached over HTTP.

## Local build

Use Android Studio's JBR or any JDK 17+.

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:assembleDebug
```

The debug APK is at:

```
app/build/outputs/apk/debug/app-debug.apk
```

### Emulator against local backend

Start the local API server first:

```bash
cd ../api_server
node server.js
```

Build with the emulator loopback address:

```bash
./gradlew :app:assembleDebug -Pukiyo.apiBaseUrl=http://10.0.2.2:3008/api/android/v1
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The debug manifest allows cleartext traffic. Release builds do not.

### Production debug build

```bash
./gradlew :app:assembleDebug
```

The default API base URL is `https://api.sagaci.top/api/android/v1`.

## Signing

An upload keystore has been generated at:

```
keystore/ukiyo-upload.jks
```

Credentials are in:

```
keystore.properties
```

Both are gitignored. Move them to a safe place before publishing and keep a secure backup. If the upload key is lost, Play App Signing can recover only if it was enrolled before the first upload.

Release build:

```bash
./gradlew :app:bundleRelease
```

Output:

```
app/build/outputs/bundle/release/app-release.aab
```

### Direct phone testing without Play

```bash
./gradlew :app:assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

The release APK is signed with the upload key. Uninstall it before installing the Play-distributed build, because Play App Signing uses a different signing key.

## Android Auto

The app declares:

- `MediaLibraryService` and `MediaBrowserService` intent filters.
- `android:foregroundServiceType="mediaPlayback"`.
- `res/xml/automotive_app_desc.xml` with `<uses name="media"/>`.
- `com.google.android.gms.car.application` metadata.

Auto library:

- Home
- Liked Songs
- Recently Played
- Playlists
- Downloads
- Search

Auto requires:

- Phone running Android 9 or newer.
- Android Auto app installed on the phone.
- For a real head unit, connect the phone.
- For local testing, use the Android Auto Desktop Head Unit (DHU). See `docs/dhu-testing.md`.

Auto never autoplays. The user must choose content.

## Play closed testing

See `docs/play-store-checklist.md`.

## Privacy policy

A static privacy page is in `docs/privacy.html`. Deploy the `docs/` folder to Cloudflare Pages or Vercel. Do not use the empty `sagaci.top/privacy` path.

## Architecture notes

```
Android app
  |
  | HTTPS + Supabase JWT + X-Ukiyo-Device
  v
api.sagaci.top/api/android/v1
  |
  +-- auth / config / device guard / rate limit
  +-- proxy to existing api-server routes
  +-- signed /play/{token}
  +-- lyrics (NetEase, LRCLIB, ytmusic fallback)
  |
  +-- audio CDN (EQ+/FLAC direct to phone)
  +-- yt-microservice (AAC fallback)
```

Security properties:

- The Android APK contains no provider URLs, cookies, service-role keys, or resolver logic.
- Stream URLs returned to the app are signed, short-lived, and bound to the user and device.
- The app is dormant without a valid account and a registered device.
- The backend can be disabled remotely with `android_config.enabled = false`.
- The PWA remains untouched. Its existing open Vercel endpoints are a separate, deferred issue.

## Known limitations

- The home server is the primary backend for the Android app. Offline downloads keep working if it is unreachable.
- Android Auto cannot be fully verified without the Android Auto app and a DHU or real head unit.
- Lyrics are shown in their original script in v1. Transliteration is not included.
- Admin tools stay in the PWA and Supabase dashboard.
- Key rotation and PWA/Vercel hardening are deferred until the Android app is complete.
