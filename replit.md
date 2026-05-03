# LocalIDE — Android Kotlin

A full-featured native Android IDE built with Kotlin + Jetpack Compose.

## What This Is

A native Android app that turns your phone into a local development environment. Open it in **Android Studio** to build and deploy to a device or emulator.

## Features

| Tab | What It Does |
|-----|-------------|
| **Editor** | Multi-tab code editor with syntax highlighting (9 languages), in-file search, line numbers, font size |
| **Files** | File manager with breadcrumb nav, create/rename/delete files & folders, sort, hidden file toggle |
| **Terminal** | Real shell via `ProcessBuilder` — runs actual commands on the device, command history, kill support |
| **Server** | Embedded NanoHTTPD HTTP server — serve any directory over LAN, request log, port config |

## Project Structure

```
app/src/main/kotlin/com/localide/
├── MainActivity.kt                  # Navigation + bottom bar
├── model/FileItem.kt                # Data models
├── server/LocalHttpServer.kt        # NanoHTTPD HTTP server
├── ui/
│   ├── theme/Theme.kt               # IDE dark color palette
│   ├── theme/Typography.kt          # Monospace typography
│   ├── editor/CodeEditorScreen.kt   # Code editor UI
│   ├── filemanager/FileManagerScreen.kt
│   ├── terminal/TerminalScreen.kt
│   └── server/ServerScreen.kt
├── util/SyntaxHighlighter.kt        # Regex-based syntax highlighting
├── util/FileUtils.kt                # File utilities
└── viewmodel/
    ├── EditorViewModel.kt
    ├── FileManagerViewModel.kt
    ├── TerminalViewModel.kt
    └── ServerViewModel.kt
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + StateFlow
- **Navigation**: Jetpack Navigation Compose
- **HTTP Server**: NanoHTTPD 2.3.1
- **Async**: Kotlin Coroutines
- **Min SDK**: 26 (Android 8.0+)
- **Target SDK**: 35 (Android 15)

## How to Build

1. Open this project in **Android Studio** (Hedgehog 2023.1.1+)
2. Click **Sync Now** when Gradle sync is prompted
3. Connect an Android device (USB debugging on) or start an emulator
4. Press **Run (▶)** or `Shift+F10`

### Build APK from command line
```bash
export ANDROID_HOME=/path/to/android-sdk
chmod +x gradlew
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

## Syntax Highlighting Support

Kotlin · Java · JavaScript/TypeScript · Python · HTML · CSS · Shell · JSON · XML
