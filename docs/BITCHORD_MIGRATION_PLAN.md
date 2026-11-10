# BitChord-based Ukiyo Android Migration Plan

## Status

Approved: 2026-11-10

The current `main` contains the first native Ukiyo app (v1.0.1). It remains installed/testable as a fallback.

The BitChord UI migration will be developed on the `bit-chord-base` branch.

## Goal

Use the complete BitChord Android UI and player, remove BitChord's network/auth/source layers, and make the private Ukiyo backend the only data source.

This is a full fork, not an element-by-element redesign.

## Decisions

- Keep BitChord's UI, player, queue, Android Auto, crossfade, canvas, downloads, and settings presentation.
- Keep our private backend for auth, library, search, playlists, lyrics, and signed streams.
- The Android app is GPLv3 and its source stays public.
- The backend is a separate private repository.
- No BitChord backend code is included in the public app repository.
- No provider URLs or provider API code should remain in the final APK.
- Main branch remains the current v1.0.1 build until the fork is ready.

## Repositories

- Public GPLv3 app: https://github.com/ivaodh/ukiyo-music-android
- Private backend: https://github.com/ivaodh/ukiyo-backend

## Upstream

- BitChord: https://github.com/kushagrasinghx/BitChord
- Pinned commit: `acf219a7e4a7f52f026d4336d5c7ac5cf7bfafcc`
- License: GPLv3

## Module strategy

Keep:

- `app`
- `shared`
- `sharedUi`
- `native`
- `wearos/protocol` (required by the app source set, not the full Wear app)
- Gradle wrapper and BitChord build versions

Exclude:

- `backend` (BitChord server)
- `tv`
- `wearos` app module (keep only `wearos/protocol`)
- `web`
- BitChord branding images and README
- BitChord's own docs folder

## What we keep, replace, and remove

| Area | Action |
|---|---|
| BitChord UI (`app`, `sharedUi`) | Keep |
| BitChord player, queue, crossfade, canvas, Auto | Keep |
| BitChord downloads, lyrics UI, settings UI | Keep and adapt |
| Haze glass, mesh gradients, animations | Keep |
| Google/YouTube auth | Replace with Ukiyo login/device registration |
| `YtMusicRepository`, Innertube, StreamResolver | Replace with Ukiyo backend adapter |
| Home/Explore/Library/Search data | Replace with Ukiyo API |
| Artist/album/playlist detail data | Replace with Ukiyo API or new backend endpoints |
| Lyrics | Route through Ukiyo backend |
| BitChord server | Do not include |
| TV, Wear OS, web, desktop | Do not include |
| Spotify, Discord, Last.fm, ListenBrainz, Listen Together | Hide/remove |
| Addons, module sources, YouTube/JioSaavn sources | Remove |
| Local library, SMB, WebDAV | Hide for v1 |
| Bit-perfect/DAC, video, podcasts | Hide for v1 |
| App ID | `app.ukiyo.music` |
| Namespace | Keep `com.music.bitchord` initially to avoid a package-wide rename |

## Integration architecture

### Auth

- Remove BitChord Google WebView login and account switcher.
- Add Ukiyo email/password login through `/api/android/v1/auth/login`.
- Register the device through `/api/android/v1/devices/register`.
- Store tokens encrypted.
- Map the Ukiyo user to BitChord's `Account` model.
- Use a single Ukiyo account in v1.

### Data repository

Keep the `YtMusicRepository` object name and method surface so `MainViewModel` and the UI do not need a large rewrite. Replace its internals with Ukiyo API calls and mapping.

Map:

- `HomeFeed`, `HomeShelf`, `ShelfItem` from Ukiyo home feed, history, liked songs, playlists, charts.
- `LibraryPage` from liked, history, and playlists.
- `SearchPage`, `SearchResult` from Ukiyo search.
- `ArtistPage` from Ukiyo artist endpoint.
- Album/playlist pages from Ukiyo playlist/library endpoints.
- `SongMenu`, likes, history, playlist CRUD from existing Ukiyo routes.
- Moods/genres from charts/categories, with new backend support where needed.

BitChord features that are separate products will be hidden, not mapped.

### Stream resolution

Add an Ukiyo stream resolver that:

1. Reads track metadata from BitChord's playback URI.
2. Calls `/api/android/v1/tracks/resolve`.
3. Receives the signed `/play` URL.
4. Adds the `X-Ukiyo-Device` header.
5. Hands the URL to BitChord's Media3 player.

Remove YouTube/Innertube/NewPipe/JioSaavn resolver paths from playback. Keep crossfade and canvas.

### Downloads

Keep BitChord's download manager, pointed at the Ukiyo resolver. Use a longer signed URL TTL for downloads or re-resolve on expiry.

### Lyrics

Keep the BitChord lyrics presentation. Replace provider lookups with the Ukiyo lyrics endpoint and map LRC/plain lyrics into BitChord's lyric model.

### Android Auto

Keep BitChord's `MediaLibraryService` and feed its library from Ukiyo. Verify browse, search, play, downloads, and background playback with DHU or a head unit.

### Settings cleanup

Keep theme, playback quality, crossfade, canvas, downloads, account, and about.

Hide Spotify, Discord, Last.fm, ListenBrainz, sources/addons, local/SMB/WebDAV, bit-perfect, video, podcasts, TV, and Wear.

## Milestones

### Milestone 1 - Full BitChord UI running

- Create `bit-chord-base`.
- Import BitChord Android modules.
- Exclude BitChord server, TV, Wear app, web, and desktop.
- Set application ID to `app.ukiyo.music`.
- Add GPL notices and attribution.
- Build and install an APK.

Deliverable: complete BitChord UI on the phone.

Estimate: 2-3 days.

### Milestone 2 - Ukiyo login

- Replace Google login with Ukiyo login.
- Add device registration.
- Map user to BitChord account state.
- Open full UI signed in.

Estimate: 3-5 days.

### Milestone 3 - Ukiyo backend data

- Implement repository adapter.
- Add missing backend endpoints.
- Map Home, Explore, Library, Search, artist, album, playlist.
- Hide empty/unsupported shelves.

Estimate: 2-3 weeks.

### Milestone 4 - Playback and Auto

- Add Ukiyo stream resolver.
- Add device header to player requests.
- Adapt downloads and Auto.
- Remove provider resolvers and dependencies.
- Verify crossfade, canvas, queue, background playback.

Estimate: 1-2 weeks.

### Milestone 5 - Cleanup and rebrand

- Hide unsupported integrations.
- Replace BitChord logo/name with Ukiyo.
- Verify no provider URLs remain in the APK.
- Bump version, build AAB, upload for internal testing.

Estimate: 1-2 weeks.

Total: roughly 5-8 weeks. Full UI appears in the first few days.

## Risks

- BitChord is large: about 1,816 files, 6,277-line `MainActivity`, 3,639-line `MainViewModel`, 9,883-line `PlaybackService`.
- Build uses AGP 8.10.1, Gradle 8.11.1, Kotlin 2.3.20, Compose Multiplatform 1.10.3.
- Main integration risk: removing provider code without breaking the player.
- BitChord UI is Apple Music-inspired, not Ukiyo. Rebranding is a later pass.
- Some BitChord features depend on its own backend; hide or reimplement.
- GPLv3 app source remains public; backend stays private.

## Progress log

- [x] Decide on a full BitChord fork instead of incremental redesign.
- [x] Save this plan in the public app repo.
- [ ] Create `bit-chord-base` branch.
- [ ] Import BitChord Android modules.
- [ ] Strip BitChord server/TV/Wear/web/desktop modules.
- [ ] Set application ID to `app.ukiyo.music`.
- [ ] Build the full BitChord UI.
- [ ] Install and screenshot on emulator.
- [ ] Replace auth with Ukiyo login.
- [ ] Implement Ukiyo repository adapter.
- [ ] Add missing backend endpoints.
- [ ] Implement Ukiyo stream resolver.
- [ ] Adapt downloads and Android Auto.
- [ ] Remove provider code and dependencies.
- [ ] Hide unsupported integrations.
- [ ] Rebrand UI.
- [ ] Verify no provider URLs in APK.
- [ ] Build release AAB and upload for internal testing.

## Build commands

Current fallback app on `main`:

```bash
./gradlew :app:assembleRelease
```

BitChord fork on `bit-chord-base`:

```bash
./gradlew :app:assembleProdDebug
```

Release AAB:

```bash
./gradlew :app:bundleProdRelease
```

Signing uses `keystore.properties` locally. It is gitignored.
