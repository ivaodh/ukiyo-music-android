# GitHub repository setup

Two repositories are needed.

## 1. Private backend repository

- Suggested name: `ukiyo-backend`
- Visibility: Private
- Local source: `/Users/shome/Documents/AuraFreq/Ukiyonly/Ukiyo_Backend`
- Do not upload `node_modules`, `.env`, cookies, caches, or generated data.

## 2. Public GPLv3 Android repository

- Suggested name: `ukiyo-music-android`
- Visibility: Public
- Local source: `/Users/shome/Documents/AuraFreq/Ukiyonly/Ukiyo_Android`
- The app is GPLv3. The repository must remain publicly accessible so testers and Play users can obtain the source.
- No backend source, secrets, keystore, or signing passwords belong here.

## After creating the repositories

Share the two clone URLs. The local remotes will be added, the app repo will get the public source URL in `NOTICE` and the Settings screen, and the first commits will be pushed.

## Suggested first commit messages

- Backend: `Initial private Ukiyo backend`
- App: `Initial GPLv3 Ukiyo Android with BitChord UI foundation`
