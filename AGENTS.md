# AGENTS.md

Froh: a local Android music player (Kotlin/Compose) focused on large, varied libraries with a specific emphasis on making classical music easier to organize (catalog numbers, composer metadata via OpenOpus, performances instead of flat albums, hierarchical genres). Also supports ListenBrainz scrobbling and Android Auto.

## Layout

Package root: `app/src/main/java/com/chaddy50/froh/`

- `data/` — persistence and external integrations
  - `entity/`, `dao/` — Room entities and DAOs
  - `repository/` — repositories mediating between DAOs, API clients, and UI
  - `api/` — external API clients (e.g. OpenOpus)
  - `scanner/` — local media library scanning
  - `scrobbling/` — ListenBrainz integration
  - `util/` — data-layer helpers (e.g. catalog number parsing)
- `ui/` — Compose UI
  - `screens/` — top-level screens
  - `composables/` — reusable composables
  - `modalSheets/` — bottom sheets
  - `layout/`, `theme/` — shared layout scaffolding and theming
- `services/` — Android services (media playback session, etc.)
- `navigation/` — Compose navigation graph
- `di/` — dependency injection setup
- `utilities/` — general-purpose helpers not tied to the data layer

## Commands

- `./gradlew app:testDebugUnitTest` — unit tests (mirrors CI's `unit-tests` job).
- `./gradlew connectedDebugAndroidTest` — instrumentation tests; needs an emulator/device (CI runs this with an x86_64 API 33 emulator).
- Both jobs run in `.github/workflows/test.yml` on push/PR to `master`.

## Conventions

- Domain terms are load-bearing: "performance" means a specific recording of a work (not a generic session), distinct from "album." Don't collapse these when adding features.
- Catalog number parsing (opus, K., BWV, etc.) drives sort order — check `data/util/` before adding new sorting logic elsewhere.
- New data-layer code follows entity → dao → repository layering; UI reads through repositories, not DAOs directly.

## Git & Commits

- **Never include Claude (or any AI assistant) as a commit co-author or contributor.** No `Co-Authored-By: Claude` trailer, no "Generated with Claude Code" line, no assistant mention in commit messages, PR titles, or PR descriptions. Write commits as the author, describing the change and why.
- Create commits only when explicitly asked.
