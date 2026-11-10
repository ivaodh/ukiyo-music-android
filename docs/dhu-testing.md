# Android Auto Desktop Head Unit (DHU)

The Android Auto app and DHU are separate from the normal Android SDK. Install them before testing.

## Install Android Auto

1. Install the Android Auto app from Google Play on the test phone.
2. Open Android Auto, go to Settings, and tap Version repeatedly until developer mode is enabled.
3. In Developer settings, enable "Unknown sources" for sideloaded builds.
4. Connect the phone to the development machine with USB.
5. Enable USB debugging.

## Install DHU

1. Open Android Studio.
2. Go to Tools > SDK Manager > SDK Tools.
3. Enable "Android Auto API Simulators" and "Android Auto Desktop Head Unit Emulator".
4. Apply and install.
5. The DHU is installed under the Android SDK extras directory.

## Run DHU

```bash
adb forward tcp:5277 tcp:5277
desktop-head-unit
```

If `desktop-head-unit` is not on PATH, find it under:

```bash
find ~/Library/Android/sdk -iname 'desktop-head-unit'
```

Start the Ukiyo app first, sign in, and register the device. Then start DHU and select Ukiyo Music from the media apps.

## Test matrix

- Service starts before the main Activity.
- Service starts after force-stop.
- Service starts with no network.
- Service starts when the user is signed out.
- Browse Home, Liked Songs, Recently Played, Playlists, Downloads.
- Search for a song and play it.
- Play, pause, next, previous from the car screen.
- Steering wheel and Bluetooth controls.
- Notification and lockscreen controls.
- Play downloaded tracks with the backend stopped.
- No autoplay on startup.
- Content loads within 10 seconds.
- Contrast and icon visibility.
- Dark and light car themes.
