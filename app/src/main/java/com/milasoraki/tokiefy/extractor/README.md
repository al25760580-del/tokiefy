# `extractor/`

Network/data extractor layer. Modelled after NewPipe's extractor: **pure
Kotlin, zero Android framework dependencies.**

## Structure

| Package | Role |
|---------|------|
| `api/`          | Retrofit service interfaces, the `TikTokApi` ISP-style aggregator, and `Session` / `SessionHolder`. |
| `api/interceptor/` | OkHttp interceptors that add required headers, common query params, body integrity (`X-SS-STUB`), and the user-agent. |
| `remote/`       | `OkHttpFactory` plus the `mock/` package containing canned responses for use until native signing is complete. |
| `model/`        | Moshi DTOs split per subdomain: `feed/`, `messaging/`, `sticker/`, `user/`. These stay as pure wire-format classes; UI mapping happens in repositories. |

## What goes here

* New Retrofit sub-interfaces.
* New DTOs matching server JSON.
* New OkHttp interceptors (e.g. future signing interceptor).
* Endpoint/path constants in `api/TikTokEndpoints.kt`.

## What does NOT go here

* Any Android imports (no `android.content.*`, no `Context`, no Compose).
* Formatting strings for the UI (for example "2 min ago" lives in
  `../util/RelativeTime.kt` *but* the rendering using `stringResource`
  lives in `../ui/util/`).
* Business rules (those belong in `../data/` repositories).

## Signing status

* `X-SS-STUB` — **done**, `BodyIntegrityInterceptor`.
* `X-Argus` / `X-Bogus` / `X-Ladon` / `X-Gorgon` — **TODO(SIGNING):** blocked
  on either `libmsaoaidsec.so` or a community reimplementation.
