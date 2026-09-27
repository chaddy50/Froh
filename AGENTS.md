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

## Personal Coding Style

Nathan's cross-cutting preferences, applied on top of whatever the surrounding code does
(prefer these even where local style differs, except for a genuine framework technical
requirement — that's correctness, not style):

- **Comments** — extremely sparse, the exception not the rule. Default to none; don't
  restate what code plainly does. Only write one for a genuinely complex edge case,
  workaround, or non-obvious constraint, and keep it to a single short line — never
  multi-line blocks or docstrings.
- **Naming** — descriptive and fully spelled out, even if longer (`selectedPerformance`,
  not `selPerf`). Abbreviate only widely-understood terms (`id`, `url`, `http`). Booleans
  start with a verb that reads as a yes/no question: `is`, `was`, `should`, `can`.
- **Functions** — small and single-purpose. Extract helpers readily. A function doing two
  things is two functions.
- **Error handling** — fail fast. Validate inputs/preconditions early and surface problems
  loudly rather than swallowing them. Don't add defensive guards that mask a real upstream
  bug — fix the caller instead.
- **Control flow** — guard clauses and early returns for edge cases; keep the happy path
  flat and un-nested.
- **Abstraction** — rule of three. Tolerate a little duplication; abstract once a pattern
  genuinely repeats. Don't build a framework for one caller.
- **Mutability** — immutable by default (`val`, read-only collection types); introduce
  `var` or a mutable collection only for a clear, deliberate reason.
- **File/feature organization** — follow the established local pattern above; don't
  introduce a new architectural or directory pattern without precedent or an explicit need.

## Git & Commits

- **Never include Claude (or any AI assistant) as a commit co-author or contributor.** No `Co-Authored-By: Claude` trailer, no "Generated with Claude Code" line, no assistant mention in commit messages, PR titles, or PR descriptions. Write commits as the author, describing the change and why.
- Create commits only when explicitly asked.
