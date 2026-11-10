# Play Data Safety notes

Use this as a guide for the Play Console Data Safety form. Confirm with the final app build.

## Collected

| Data type | Collected | Shared | Purpose | Optional |
|---|---|---|---|---|
| Email address | Yes | No | Account management, login | No |
| Device or other IDs | Yes | No | Device limit, abuse prevention | No |
| App activity: playback history | Yes | No | Recently played, recommendations | No |
| App activity: liked songs and playlists | Yes | No | User library | No |
| App activity: in-app search history | Yes | No | Search results | No |
| Approximate location | No | No | - | - |
| Precise location | No | No | - | - |
| Contacts | No | No | - | - |
| SMS or call logs | No | No | - | - |
| Photos or videos | No | No | - | - |
| Files and docs | No | No | - | - |
| Health and fitness | No | No | - | - |
| Financial info | No | No | - | - |

## Security

- Data is encrypted in transit.
- Data is not sold.
- Data is not used for advertising.
- Data deletion is available by contacting the admin.
- Users can sign out and remove local downloads at any time.

## Deletion

When a user requests deletion:

1. Delete the Supabase auth user.
2. Delete related `user_devices`, `liked_songs`, `play_history`, `user_playlists`, and `playlist_tracks` rows.
3. The app can be uninstalled to remove local downloads and cached data.
