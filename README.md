# Offline-First Notes App

A production-ready Android application demonstrating a robust offline-first architecture with real-time bi-directional synchronization and field-level conflict resolution.

## Key Features

*   **Offline-First**: Full functionality without internet using Room for local persistence.
*   **Real-time Sync**: Instant updates using Supabase Realtime (WebSockets) to detect server-side changes.
*   **Intelligent Conflict Resolution**: Granular field-level merging based on specific timestamps (`titleUpdatedAt`, `contentUpdatedAt`).
*   **Immediate Background Sync**: Uses WorkManager to schedule an immediate push as soon as the connection returns, even if the app is closed.
*   **Silent Background Sync**: Automatic synchronization triggered by network restoration or server events without interrupting the user.
*   **Reliable Deletion**: Support for both soft deletes (cross-device sync) and server-side hard delete reconciliation.
*   **Modern UI**: Built with Jetpack Compose, Material 3, and interactive Pull-to-Refresh.
*   **Connectivity Aware**: Real-time UI updates based on network status changes.

## Tech Stack

*   **UI**: Jetpack Compose, Material 3
*   **Architecture**: MVVM, Clean Architecture
*   **Dependency Injection**: Koin
*   **Local DB**: Room
*   **Networking**: Retrofit, OkHttp, WebSockets
*   **Real-time**: Supabase Realtime
*   **Background Work**: WorkManager
*   **Concurrency**: Kotlin Coroutines & Flow

## Architecture

The project follows Clean Architecture principles:
*   **Domain**: Business logic and use cases.
*   **Data**: Repository implementations, Room entities/DAOs, Retrofit DTOs, and WebSocket management.
*   **UI**: Reactive Compose components and ViewModels.

## Sync Strategy

1.  **Pull & Merge FIRST**: Remote changes are fetched and merged into the local state. Conflicts are resolved by comparing field-specific timestamps.
2.  **Push SECOND**: Local-only changes (including merged results) are pushed to the server to ensure consistency.
3.  **Silent Updates**: Realtime events trigger syncs silently. The loading indicator is reserved for manual user actions (Pull-to-Refresh).
4.  **Full Reconciliation**: Reconciles "orphaned" local notes by comparing local synced data against the complete remote state.
5.  **WorkManager Guarantees**: Whenever a local change is made offline, a one-time `WorkRequest` is enqueued to guarantee the data is pushed when the device comes back online.
