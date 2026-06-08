# FinCheck — Android

## What this project is
Jetpack Compose personal finance tracker app. Offline-first, MVI architecture, biometric auth.

## Modules
- :app          — Compose UI, NavHost, Hilt entry point, MainActivity
- :core:data    — Repositories, Room DB, WorkManager, Retrofit/Ktor client
- :core:ui      — Shared composables, design tokens, theme

## Architecture
- Pattern: MVI. Every screen has: UiState (sealed), UiEvent, ViewModel.
- State: StateFlow for UI state, SharedFlow for one-shot events (navigation, toasts).
- DI: Hilt. All ViewModels are @HiltViewModel. No manual factory boilerplate.
- Async: Coroutines + Flow only. No RxJava, no GlobalScope, no runBlocking in prod code.
- Navigation: single NavHost in AppNavGraph.kt. Destinations are typesafe sealed objects.

## UI conventions
- Compose only. No XML layouts, no View system.
- All strings in strings.xml. No hardcoded text in composables.
- Shared components live in :core:ui. Never create a one-off composable in :app directly.
- Theme tokens (colors, typography, spacing) come from FinanceTrackerTheme — no raw Color() calls.

## Data layer
- Single source of truth: Room. Network is a sync source, never read directly by UI.
- All repositories live in :core:data/repository/.
- WorkManager handles background sync. No foreground service for sync.
- Retrofit interface lives in :core:data/network/. One interface per backend service domain.

## Key files
- AppNavGraph.kt            — all navigation destinations
- core/data/repository/     — one file per domain (Auth, Transaction, User)
- core/data/network/        — Retrofit interfaces + DTOs
- core/data/local/          — Room DAOs + entities
- app/di/AppModule.kt       — top-level Hilt bindings

## Backend connection
- Base URL configured via BuildConfig.BASE_URL (set in local.properties, not committed).
- Backend repo: ../FinanceTracker-Backend (separate project, separate Git repo).
- API contract lives in backend repo at docs/openapi.yaml — read it before adding endpoints.

## Testing
- Unit tests: JUnit4 + MockK + Turbine (for Flow). No Robolectric unless strictly necessary.
- UI tests: Compose test rules. Cover login flow, transaction list, add transaction.
- All new ViewModels must have at least a happy-path unit test before merging.

## Do not
- Use GlobalScope anywhere.
- Add Gradle dependencies without updating libs.versions.toml first.
- Commit local.properties or any file containing BASE_URL or API keys.
- Leave TODO comments in committed code — open a GitHub issue instead.
- Call the network layer directly from a ViewModel — always go through a Repository.