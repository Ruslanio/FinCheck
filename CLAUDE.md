# FinCheck — Android

## What this project is
Jetpack Compose personal finance tracker app. Offline-first, MVI architecture, biometric auth.

## Modules
- `:app`                — NavGraph, MainActivity, Hilt entry point
- `:core:data`          — Repositories, mappers, domain models; depends on :core:database and :core:network
- `:core:database`      — Room DB, DAOs, entities only — no repositories here
- `:core:network`       — Retrofit interfaces, DTOs only — no repositories here
- `:core:sync`          — WorkManager workers only — no repositories here
- `:core:ui`            — Shared composables, design tokens, theme
- `:feature:auth`            — Login/Register screens + AuthNavigation
- `:feature:home`            — Home screen + HomeNavigation
- `:feature:profile`         — Profile screen + ProfileNavigation
- `:feature:transactions`    — Transaction list + TransactionNavigation
- `:feature:add-transaction` — AddTransactionFab, AddTransactionSheet, AddTransactionViewModel

## Adding add-transaction to any screen
Use `AddTransactionFab()` in the screen's `floatingActionButton` slot. The FAB owns sheet
visibility state and its own `AddTransactionViewModel` instance. The host screen must provide
`LocalSnackbarHostState` via `CompositionLocalProvider` so the FAB can surface snackbars:

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = { AddTransactionFab() },
    ) { ... }
}
```

## Module boundaries — critical rules
- Repositories live exclusively in `:core:data`. Never create a repository in `:core:database`, `:core:network`, or `:core:sync`.
- `:core:database`, `:core:network`, and `:core:sync` must not depend on each other.
- Feature modules must not depend on other feature modules.
- `:app` is the only module allowed to depend on all feature modules.

## Architecture
- Pattern: MVI. Every screen has: UiState (sealed), UiEvent, ViewModel.
- State: StateFlow for UI state, SharedFlow for one-shot events (navigation, toasts).
- DI: Hilt. All ViewModels are @HiltViewModel. No manual factory boilerplate.
- Async: Coroutines + Flow only. No RxJava, no GlobalScope, no runBlocking in prod code.

## Navigation
- There are two NavHosts: one in `AppNavGraph` (auth vs main split) and one inside `MainScreen` (bottom-nav tabs).
- NavController is created at NavHost level only (`rememberNavController()`). Never pass a NavController below the NavHost — pass lambdas instead.
- Each feature module exposes a NavGraphBuilder extension (e.g. `authGraph(...)`, `transactionsGraph(...)`) plus `navigateTo*` extension functions on NavController.
- Destinations are typesafe `@Serializable` objects or data classes.

## UI conventions
- Compose only. No XML layouts, no View system.
- All strings in strings.xml. No hardcoded text in composables.
- Shared components live in `:core:ui`. Feature-specific components live inside their own feature module.
- Theme tokens (colors, typography, spacing) come from FinanceTrackerTheme — no raw Color() calls.

## Data layer
- Single source of truth: Room (`:core:database`). Network is a sync source, never read directly by UI.
- Repositories in `:core:data` coordinate between DAOs and API services.
- WorkManager handles background sync (`:core:sync`). No foreground service for sync.
- Network: one Retrofit interface per backend service domain, in `:core:network/service/`.

## Key files
- `app/.../navigation/AppNavGraph.kt`          — top-level NavHost (auth vs main)
- `app/.../ui/main/MainScreen.kt`              — bottom-nav NavHost
- `core/data/repository/`                      — one file per domain (Auth, Transaction)
- `core/network/service/`                      — Retrofit interfaces
- `core/network/dto/`                          — DTOs
- `core/database/dao/`                         — Room DAOs
- `core/database/entity/`                      — Room entities
- `app/di/AppModule.kt`                        — top-level Hilt bindings

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
- Call the network layer directly from a ViewModel — always go through a Repository.
- Pass NavController below the NavHost level — use lambdas.
