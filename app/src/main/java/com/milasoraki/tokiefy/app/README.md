# `app/`

Application-level glue.

## What goes here

* `TokiefyApp.kt` — the `Application` subclass. Owns one-time Coil setup.
* `di/ServiceLocator.kt` — manual dependency container; constructs the single
  `TikTokApi` and every repository.

## What does NOT go here

* UI composables (those live in `../ui/`).
* Network or domain logic (those live in `../extractor/` and `../data/`).
* Any per-screen state (use ViewModels under `../ui/feat/...`).

## Design choice: Manual Service Locator

| Option    | Pros                                    | Cons                                  |
|-----------|-----------------------------------------|---------------------------------------|
| Hilt      | Standard, scoped, compile-time-checked  | Heavy kapt processing; harder to read |
| Koin      | No code generation                      | Runtime errors for missing bindings   |
| **Manual (chosen)** | Zero magic; graph visible in one file; no extra deps | Slightly more boilerplate per new dep |

The project is intentionally small; explicitness wins.
