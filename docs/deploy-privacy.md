# Deploy the privacy page

The privacy page is static HTML. Do not use a path on the PWA domain.

## Cloudflare Pages

1. Go to Cloudflare Dashboard > Workers & Pages > Create.
2. Choose Pages > Upload assets.
3. Project name: `ukiyo-legal`.
4. Upload the `docs` folder or just `privacy.html`.
5. After deployment, the URL will be similar to:

```
https://ukiyo-legal.pages.dev/privacy.html
```

6. Add that URL to Play Console > App content > Privacy policy.

## Vercel

1. Create a new Vercel project.
2. Use a static project with `docs` as the output directory.
3. Deploy.
4. Add the resulting URL to Play Console.

## Before publishing

Replace the placeholder contact email `admin@sagaci.top` with the address you want to use for privacy and deletion requests.
