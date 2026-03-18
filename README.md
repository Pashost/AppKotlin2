# Notes Manager (Kotlin, MVVM, Room, Hilt)

A production-ready Notes Manager Android app built with modern Android architecture:

- MVVM + Repository pattern
- Room database for local persistence
- Hilt dependency injection
- Kotlin Coroutines + Flow
- Navigation Component + Safe Args
- RecyclerView + ListAdapter + DiffUtil
- ViewBinding + Material Design UI

## Features

- List notes (title, preview, timestamp)
- Add new note
- Edit existing note
- Swipe to delete with Undo (Snackbar)
- Search notes by title or content
- Loading indicators and user-friendly error messages

## Tech Stack

- Kotlin
- Minimum SDK: 21
- AndroidX Architecture Components (ViewModel, LiveData/Flow)
- Room
- Hilt
- Navigation Component
- Material Components

## Package Structure

- `data`
  - `local`: Room entity, DAO, database
  - `repository`: repository contract and implementation
- `di`: Hilt modules
- `ui`
  - `notes`: list screen, adapter, list ViewModel
  - `addedit`: add/edit screen and ViewModel
- `utils`: constants and utility extensions

## Build and Run

1. Open the project in Android Studio (latest stable recommended).
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or device.

## Important Notes

- Search is implemented as debounced query updates observed via Flow.
- Repository is the single source of truth for UI layers.
- ViewModels are lifecycle-aware and survive configuration changes.
