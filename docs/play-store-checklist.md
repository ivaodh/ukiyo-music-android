# Google Play closed testing checklist

## Before upload

- [ ] Application ID is `app.ukiyo.music`.
- [ ] App name is Ukiyo Music.
- [ ] Version code and version name updated.
- [ ] Release AAB built: `./gradlew :app:bundleRelease`.
- [ ] Upload keystore backed up securely.
- [ ] Privacy policy deployed to Cloudflare Pages or Vercel.
- [ ] Privacy policy URL added to Play Console.
- [ ] Data safety form completed:
  - Email address: account management.
  - Device or other IDs: device limit and abuse prevention.
  - App activity: playback history, liked songs, playlists.
  - No location, contacts, SMS, or call logs.
  - Data encrypted in transit.
  - Data deletion available by email.
- [ ] Content rating questionnaire completed.
- [ ] Target audience completed. The app is not directed at children.
- [ ] Ads declaration: no ads.
- [ ] News declaration: no.
- [ ] Government app declaration: no.
- [ ] Health declaration: no.
- [ ] Financial features declaration: no.
- [ ] App access instructions: provide a test account to reviewers if required.
- [ ] Android Auto car app quality checked with DHU.
- [ ] No provider names in the store listing.
- [ ] Store listing does not claim licensed content.

## Play Console

1. Create the app.
2. Enable Play App Signing.
3. Upload the AAB to Internal testing first.
4. Install from the internal testing link on 5-10 devices.
5. Fix crashes and playback issues.
6. Create a closed testing track.
7. Add a Google Group or an email list. Closed testing supports up to 2,000 testers per list and 50 lists per track. Google Groups have no published member cap.
8. Share the opt-in link with 150-200 friends.
9. Submit the release for review.
10. Wait for review approval. Closed releases are reviewed by Google.

## Review timing

- Internal testing: usually fast, but review is not guaranteed to be skipped.
- Closed testing: hours to 7 days, longer in exceptional cases.
- If the app is rejected, fix the issue and resubmit. Repeated rejections can affect the account.

## Policy risk

The app accesses content from third-party sources. Google Play may treat that as a policy issue even in closed testing. Keep a private distribution fallback ready:

- Firebase App Distribution: 500 testers per project, 200 per group.
- Private GitHub release plus Obtainium.

Do not attempt to disguise the app. Keep the store listing accurate and be prepared for review feedback.
