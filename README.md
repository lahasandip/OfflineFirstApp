# Offline-First Notes App

A production-ready Android application demonstrating a robust offline-first architecture with real-time bi-directional synchronization and granular field-level conflict resolution.

## 🚀 Key Features

*   **Offline-First**: Full functionality without internet. The local database is the single source of truth for the UI.
*   **Real-time Sync**: Instant updates using **Supabase Realtime (WebSockets)** to detect server-side changes and trigger silent background reconciliations.
*   **Intelligent Conflict Resolution**: Granular field-level merging based on specific timestamps (`titleUpdatedAt`, `contentUpdatedAt`). Merges changes instead of overwriting the entire record.
*   **Immediate Background Sync**: Uses **WorkManager** to schedule immediate one-time syncs as soon as the connection returns, ensuring data is pushed even if the app is closed.
*   **Silent Background Sync**: Automatic synchronization triggered by network restoration or server events without interrupting the user's flow.
*   **Reliable Deletion**: Support for both soft deletes (cross-device sync) and server-side hard delete reconciliation (Full Reconciliation).
*   **Modern UI**: Built with Jetpack Compose, Material 3, and includes interactive features like Pull-to-Refresh and automatic scroll-to-top on new entries.
*   **Performance Optimized**: Indexed database queries, bulk operations, and shared network resources for efficiency.

## 🛠 Tech Stack

*   **UI**: Jetpack Compose, Material 3
*   **Architecture**: MVVM, Clean Architecture
*   **Dependency Injection**: Koin
*   **Local Persistence**: Room (with Indices and @Upsert)
*   **Networking**: Retrofit, OkHttp
*   **Real-time**: Supabase Realtime (WebSockets via Phoenix Protocol)
*   **Background Tasks**: WorkManager
*   **Concurrency**: Kotlin Coroutines & Flow

## 🏗 Architecture

The project follows strict **Clean Architecture** principles to ensure separation of concerns:

*   **Domain Layer**: Contains the core Business Logic, Use Cases, and Domain Models (`Note`).
*   **Data Layer**: Handles data retrieval and storage. Includes Room Entity/DAO, Retrofit API definitions, and Repository implementations.
*   **UI Layer**: Reactive UI built with Compose, managed by ViewModels that observe data flows from the Domain layer.

## 🔄 Sync Strategy (Pull-FIRST, Push-SECOND)

The sync engine follows a strict order to ensure data integrity:

1.  **Pull & Merge (Remote ➔ Local)**:
    *   Fetches the complete remote state for reconciliation.
    *   Compares `titleUpdatedAt` and `contentUpdatedAt` field-by-field.
    *   Reconciles deletions: Local notes missing from the server or marked as deleted are hard-deleted locally.
2.  **Push (Local ➔ Remote)**:
    *   Identifies locally modified notes (`isSynced = false`).
    *   Performs bulk upsert to the remote server.
    *   Updates local status to `isSynced = true` upon success.
3.  **Background Reliability**:
    *   **One-Time Sync**: Triggered instantly on every local change to guarantee delivery.
    *   **Periodic Sync**: Runs every 15 minutes as a safety fallback and background update mechanism.

## ⚡ Performance Optimizations

*   **Database Indexing**: Optimized columns (`updatedAt`, `isSynced`, `isDeleted`) to ensure fast lookups and efficient sorting.
*   **Bulk Operations**: Implemented `hardDeleteNotes` and `markAsSyncedBulk` to minimize disk I/O and database transaction overhead.
*   **Resource Sharing**: A single `OkHttpClient` instance is shared between Retrofit (REST) and Supabase Realtime (WebSockets) to optimize memory and battery.
*   **Silent UI**: Background syncs do not trigger UI loading indicators; pull-to-refresh is reserved for manual user intent.

## 📁 Project Structure

```text
app/src/main/java/com/example/offlinefirstnotesapp/
├── core/
│   ├── database/       # Room database setup & converters
│   ├── di/             # Koin dependency injection modules
│   ├── network/        # Retrofit interceptors & WebSocket manager
│   ├── theme/          # Material 3 UI theme definitions
│   └── utils/          # Shared utilities (Network observer, Sync scheduler)
└── features/notes/
    ├── data/           # Repositories, DAOs, DTOs, Mappers, Workers
    ├── domain/         # Use Cases, Repository interfaces, Domain models
    └── ui/             # Compose screens, components, and ViewModels
```
