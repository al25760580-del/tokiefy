# Tokiefy

A TikTok client for Android, built in Kotlin with Jetpack Compose and Material 3.
The name follows the NewPipe/Xtra tradition: "Tokiefy" blends *TikTok* and *FYP*
(For You Page).

> Status: **UI complete, signing in progress.** The inbox, chat (with sticker
> panel), friends and profile screens all render and navigate end-to-end against
> a [mock interceptor](app/src/main/java/com/milasoraki/tokiefy/extractor/remote/mock/).
> Hitting the real API requires the native `X-Argus`/`X-Bogus` signing step, which
> is stubbed and marked `TODO(SIGNING):`.

## Screenshots (target)

Three reference screens drive the UI:
1. **Inbox** — story circles, dismissible blue "New followers" banner, and the
   conversation list (including MieloVT🔥455).
2. **Chat** — message bubbles with peer badge ("misky"), a shared video card
   with a circular share button, and the expanded sticker panel (Recent /
   Saved, with the `+` add button).
3. **Inbox alt.** — the blue followers-banner variant.

## Architecture

NewPipe-style three-layer split with no heavy DI framework:

| Layer | Location | Responsibility |
|-------|----------|----------------|
| Extractor | `extractor/` | Retrofit interfaces, Moshi DTOs, OkHttp interceptors, mock layer. **No Android dependencies.** |
| Data | `data/` | Repositories that hide network/pagination/caching from the UI. |
| UI | `ui/` | Compose screens, Material 3 theme, ViewModels, navigation. |

A manual [ServiceLocator](app/src/main/java/com/milasoraki/tokiefy/app/di/ServiceLocator.kt)
wires everything together (no Hilt, no Koin — see the trade-off table there).

### API integration

All endpoints described in
[al25760580-del/TIKTOKAPI-DOCS-ES](https://github.com/al25760580-del/TIKTOKAPI-DOCS-ES/blob/main/INVESTIGACION.md)
are wired through typed Retrofit sub-interfaces:

| Sub-interface | Endpoint family |
|---------------|-----------------|
| `TikTokFeedApi` | `/aweme/v1/feed/` (For You) |
| `TikTokDiggApi` | `/aweme/v1/commit/item/digg/` (like/unlike) |
| `TikTokRelationApi` | `/aweme/v1/commit/follow/user/` |
| `TikTokMessagingApi` | DM conversations, messages, sticker store |

Required headers (per docs):
* `User-Agent` (real Android UA — see `UserAgentInterceptor`)
* Common query params: `aid`, `device_id`, `openudid`, `cdid`, `version_code`,
  `os_api`, `channel`, `build_number`
* `X-SS-STUB` (MD5 hex of the POST body) — done by `BodyIntegrityInterceptor`
* `X-Argus`, `X-Bogus`, `X-Ladon`, `X-Gorgon` — **TODO(SIGNING):** these require
  either `libmsaoaidsec.so` or a community reimplementation; until they are
  provided the requests fail signature validation and the mock interceptor
  returns canned responses.

## Building

```bash
./gradlew assembleDebug
```

Requires JDK 17 and Android SDK 34. The debug APK lands at
`app/build/outputs/apk/debug/app-debug.apk`.

## Conventions

* 100% Kotlin; immutable-by-default (`val`, data classes, `StateFlow`).
* Public items carry KDoc that explains **why** they exist, not just what they
  do — JDK/Linux-kernel style, imperative first line, `@param`/`@return` where
  useful.
* No wildcard imports; no cryptic names; no implicit `it` in long lambdas.
* All user-visible strings live in locale resources
  (`values/strings.xml` English default, `values-es/strings.xml` for Spanish
  preview screenshots matching the reference).
* TODOs use the format `TODO(MODULE): description`.
* Commit messages follow Conventional Commits: `module(part): description`.

## License

Code in this repository is for educational/research purposes only. All
trademarks belong to their respective owners.
