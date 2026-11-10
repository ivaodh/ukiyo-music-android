# Build status

## Completed

- New Android git repo initialized.
- Kotlin + Compose Android app with package `app.ukiyo.music`.
- Media3 ExoPlayer playback.
- MediaSession and background playback.
- MediaLibraryService for Android Auto.
- Android Auto browse tree: Home, Liked, Playlists, History, Downloads.
- Android Auto search entry point.
- Login with existing Ukiyo accounts through the backend facade.
- Device registration and 3-device limit.
- Remote config and maintenance screen.
- Force-update screen.
- Home, Search, Library, Playlist, Now Playing, Queue, Lyrics, Downloads, Settings.
- Lyrics from the backend (NetEase, LRCLIB, YT Music fallback).
- Offline downloads with WorkManager and Room.
- 5 GB download cap with oldest-first eviction.
- Signed stream URLs with user/device binding.
- Private backend repository prepared locally at `../Ukiyo_Backend`.
- SQL for `android_config`.
- Deploy script for the home server, now in the private backend repo.
- Upload keystore generated and used for the release AAB.
- Privacy page and Play checklist.
- Lint passes.
- Unit tests pass.
- Release AAB builds.

## UI spike (BitChord direction)

- App relicensed GPLv3 with `LICENSE` and `NOTICE`.
- BitChord attribution added to `NOTICE`, `UPSTREAM.md`, and the Android Settings screen.
- Added Haze frosted glass.
- Added artwork-driven mesh gradient background.
- Added floating glass dock with integrated mini player and progress ring.
- Added full-bleed artwork Now Playing screen with mesh backdrop and readable scrim.
- Adapted BitChord mesh/colour utilities under GPLv3.
- Release APK and AAB rebuilt with the new UI spike.

This is still a visual foundation, not the full BitChord screen set. Home, Library, Search, Player, Queue, Lyrics, and Settings still need the deeper BitChord layout pass.

## Verified on emulator

- Login against a local backend.
- Home feed loads.
- Search loads.
- Track resolution works.
- Audio playback works.
- Notification and media session appear.
- Lyrics screen opens.
- Download completes and local playback works.
- Downloads screen shows storage usage.
- Mini player works.
- Haze floating dock renders over content.
- Artwork mesh background loads from the current track.
- Full-bleed Now Playing screen renders and plays.
- Search text colours corrected for the new shell.

## Production backend

- Facade deployed to the home server on 2026-11-09.
- `https://api.sagaci.top/api/android/v1/config` returns `enabled: true`.
- Production smoke test passed: login, device registration, search, signed resolve, signed playback redirect, and lyrics.
- Smoke-test user and device rows were removed.
- `api-server`, `gdstudio-proxy`, and `yt-microservice` are running.
- `yt-microservice` is healthy.
- GDStudio upstream returned HTTP 521 during deployment. This is an external provider outage, not a deployment failure. EQ+ resolution and LRCLIB lyrics still work. Retry FLAC later.

## Not verified yet

- Android Auto on a real phone or DHU. The emulator does not have Android Auto installed.
- Play Console upload and review.
- Real-device testing across Samsung/Xiaomi/Pixel battery savers.

## Repositories

1. Private backend: https://github.com/ivaodh/ukiyo-backend
   - Local source: `../Ukiyo_Backend`
   - Contains `api_server`, `gdstudio_proxy`, `yt_microservice`, SQL and deploy scripts.
2. Public GPLv3 app: https://github.com/ivaodh/ukiyo-music-android
   - Local source: this directory.
   - Contains the Android app only. No backend source, no secrets, no keystore.

## Next steps

1. Create the two GitHub repositories and share the URLs.
2. Continue the BitChord UI integration: Home shelves, Library tabs/grid, Search overlay, Player queue/lyrics sheets, Settings.
3. Keep our Media3 playback and Android Auto service.
4. Add crossfade and canvas in a later playback/UI phase.
5. Install the release APK on a real phone.
6. Test Android Auto with a DHU or head unit.
7. Upload the AAB to Play internal testing, then closed testing.
8. Deploy `docs/privacy.html` to Cloudflare Pages or Vercel.
9. Back up `keystore/ukiyo-upload.jks` and `keystore.properties` securely.
10. Retry FLAC after the GDStudio upstream recovers.

## Cleanup already done

- Local test user removed from Supabase.
- Test device rows removed.
- Local API server stopped.
- Emulator stopped.
- App data cleared on the emulator.
