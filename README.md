# LocalIDE — Android Kotlin

A full-featured local IDE for Android built with Kotlin + Jetpack Compose.

## Features

| Tab | Description |
|-----|-------------|
| **Editor** | Multi-tab code editor with syntax highlighting for 9+ languages, search, font size control, line numbers |
| **Files** | File manager with breadcrumb navigation, create/rename/delete, sort options, hidden file toggle |
| **Terminal** | Shell terminal using `ProcessBuilder`, command history (↑↓), kill running process, `help` command |
| **Server** | Embedded NanoHTTPD HTTP server with directory listing, request log, port config, URL copy |

## Supported Syntax Highlighting

Kotlin · Java · JavaScript/TypeScript · Python · HTML · CSS · Shell · JSON · XML

## Project Structure

```
LocalIDE/
├── app/
│   ├── src/main/kotlin/com/localide/
│   │   ├── MainActivity.kt              # Nav + bottom bar
│   │   ├── model/FileItem.kt            # Data models
│   │   ├── server/LocalHttpServer.kt    # NanoHTTPD wrapper
│   │   ├── ui/
│   │   │   ├── theme/Theme.kt           # IDE dark theme + colors
│   │   │   ├── theme/Typography.kt      # Monospace typography
│   │   │   ├── editor/CodeEditorScreen.kt
│   │   │   ├── filemanager/FileManagerScreen.kt
│   │   │   ├── terminal/TerminalScreen.kt
│   │   │   └── server/ServerScreen.kt
│   │   ├── util/SyntaxHighlighter.kt    # Regex-based highlighter
│   │   └── viewmodel/
│   │       ├── EditorViewModel.kt
│   │       ├── FileManagerViewModel.kt
│   │       ├── TerminalViewModel.kt
│   │       └── ServerViewModel.kt
│   └── src/main/res/
├── build.gradle.kts
└── settings.gradle.kts
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM with StateFlow
- **Navigation**: Jetpack Navigation Compose
- **HTTP Server**: NanoHTTPD 2.3.1
- **Async**: Kotlin Coroutines
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)

## How to Build

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11+
- Android SDK with API 35

### Steps

1. **Open in Android Studio**
   ```
   File → Open → select the `LocalIDE/` folder
   ```

2. **Sync Gradle**
   Android Studio will prompt to sync — click **Sync Now**

3. **Run on device/emulator**
   - Connect an Android device (USB debugging enabled) or start an emulator
   - Click the ▶ Run button or press `Shift+F10`

4. **Build APK**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Command Line Build (requires Android SDK)
```bash
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Permissions Required

| Permission | Reason |
|-----------|--------|
| `READ_EXTERNAL_STORAGE` | Browse files on storage |
| `WRITE_EXTERNAL_STORAGE` | Save files |
| `INTERNET` | Local HTTP server |
| `FOREGROUND_SERVICE` | Keep server alive |

## Key Design Decisions

- **No third-party editor library** — custom `BasicTextField` with `AnnotatedString` for syntax coloring keeps the APK lean
- **ProcessBuilder terminal** — real shell execution on the device; commands run in the app's working directory
- **NanoHTTPD** — tiny (~100KB) embedded Java HTTP server, perfect for mobile
- **StateFlow + ViewModel** — reactive state management, survives configuration changes
- **Dark IDE theme** — VSCode-inspired color palette (`#0D0D0D` background, `#7C6AF7` accent)

## Screenshots

> Build and run in Android Studio to see the full UI.

---

Built with Kotlin + Jetpack Compose
