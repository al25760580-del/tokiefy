# `ui/`

Compose UI layer.

## Structure

| Package | Role |
|---------|------|
| `theme/`              | Material 3 colour, shape, typography tokens and the `TokiefyTheme` composable. |
| `components/`         | Reusable stateless components (`Avatar`, `NotificationBadge`, `ChatTextBubble`, `TikTokBottomBar`). |
| `navigation/`         | `NavRoutes` constants and `AppNavHost` (top-level destinations). |
| `feat/`               | Feature folders, one per screen: `home/`, `friends/`, `profile/`, `inbox/`, `chat/`. Each contains a single `*Screen.kt` and (where state is non-trivial) a `*ViewModel.kt`. |
| `util/`               | Compose-specific helpers (locale-aware relative-time formatter, etc.). |

## What goes here

* Composables and Compose-only helpers.
* ViewModels (for a screen they belong next to that screen, not in a
  global `viewmodel/` folder).
* `stringResource()` lookups against locale resources.

## What does NOT go here

* Direct Retrofit calls or OkHttp client setup — use repositories.
* Hardcoded user-visible strings — always add a key to
  `values/strings.xml` (and `values-es/strings.xml` for Spanish preview).
* Generic string formatting (that belongs in `../util/`).

## Feature-first packaging

Features are split into their own folders instead of one giant
`screens/` directory because per-feature files (Screen, ViewModel, any
feature-only components) stay together and can be deleted wholesale
when a feature is removed.
