# `data/`

Repository layer.

## What goes here

One repository per aggregate root:

| File | Exposes |
|------|---------|
| `FeedRepository.kt`           | Vertical feed videos (For You). |
| `ConversationRepository.kt`   | DM conversation list. |
| `MessageRepository.kt`        | Per-conversation messages + send. |
| `StickerRepository.kt`        | Sticker store (recent/saved packs). |
| `InteractionRepository.kt`    | One-off actions: like, unlike, follow. |
| `StoryRepository.kt`          | Story circles shown at the top of the inbox. |
| `NotificationRepository.kt`   | Banners/cards at the top of the inbox (new followers, activity). |

Repositories are the single place where:
* network is called,
* errors are caught and fallback data is produced,
* pagination cursors live,
* and future local DB caching will live.

## What does NOT go here

* Compose/UI code; composables never import repository classes directly
  except through ViewModels.
* DTO-to-string formatting (that stays in the extractor/util or UI layer).
* Business rules that belong to a specific screen (those live in the
  ViewModel in `../ui/feat/...`).

## Retrofit vs alternatives

| HTTP client | Why we did not pick it |
|-------------|------------------------|
| Raw `OkHttp` | Manual JSON parsing everywhere; error-prone. |
| Ktor client | No strong advantage; Moshi+Retrofit is already the standard. |
| Gson        | Less strict, slower; Moshi has better Kotlin support via `moshi-kotlin` / `@JsonClass(generateAdapter = true)`. |
| **Retrofit + Moshi (chosen)** | Typed interfaces, generated adapters, OkHttp interceptors compose cleanly. |
