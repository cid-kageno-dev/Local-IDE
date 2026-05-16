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
| **Auth** | Google Sign-In + GitHub OAuth login screen, persistent session, profile sheet with sign-out |

## Project Structure

```
app/src/main/kotlin/com/localide/
├── MainActivity.kt                  # Navigation + auth gating + GitHub deep link handler
├── auth/
│   ├── SessionManager.kt            # DataStore-backed session persistence
│   ├── GitHubOAuthConfig.kt         # GitHub OAuth credentials & URLs  ← edit this
│   └── GitHubAuthHelper.kt          # Browser launch + token exchange
├── model/FileItem.kt                # Data models
├── server/LocalHttpServer.kt        # NanoHTTPD HTTP server
├── ui/
│   ├── auth/
│   │   ├── AuthScreen.kt            # Login / sign-up UI
│   │   └── ProfileSheet.kt          # Bottom sheet with user info + sign-out
│   ├── theme/Theme.kt               # IDE dark color palette
│   ├── theme/Typography.kt          # Monospace typography
│   ├── editor/CodeEditorScreen.kt
│   ├── filemanager/FileManagerScreen.kt
│   ├── terminal/TerminalScreen.kt
│   └── server/ServerScreen.kt
├── util/SyntaxHighlighter.kt
├── util/FileUtils.kt
└── viewmodel/
    ├── AuthViewModel.kt             # Auth state + Google/GitHub sign-in logic  ← edit this
    ├── EditorViewModel.kt
    ├── FileManagerViewModel.kt
    ├── TerminalViewModel.kt
    └── ServerViewModel.kt
```

## Auth Setup (Required before building)

### Google Sign-In

1. Go to https://console.cloud.google.com/
2. Create or select a project → Enable **Identity Toolkit API**
3. Under **APIs & Services → Credentials**, create two OAuth 2.0 Client IDs:
   - **Web application** → copy the Client ID
   - **Android** → package name: `com.localide`, SHA-1: run `./gradlew signingReport` and copy the debug SHA-1
4. Open `app/src/main/kotlin/com/localide/viewmodel/AuthViewModel.kt`
5. Replace `YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com` with your Web Client ID

### GitHub OAuth

1. Go to https://github.com/settings/developers → **New OAuth App**
2. Set **Authorization callback URL** to: `localide://auth`
3. Copy the **Client ID** and generate a **Client Secret**
4. Open `app/src/main/kotlin/com/localide/auth/GitHubOAuthConfig.kt`
5. Replace `YOUR_GITHUB_CLIENT_ID` and `YOUR_GITHUB_CLIENT_SECRET`

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + StateFlow
- **Navigation**: Jetpack Navigation Compose
- **Auth**: Credential Manager API (Google) + Chrome Custom Tabs + OkHttp (GitHub)
- **Session**: Jetpack DataStore (encrypted on-device)
- **HTTP Server**: NanoHTTPD 2.3.1
- **Async**: Kotlin Coroutines
- **Min SDK**: 26 (Android 8.0+)
- **Target SDK**: 35 (Android 15)

## How to Build

1. Complete the **Auth Setup** steps above
2. Open this project in **Android Studio** (Hedgehog 2023.1.1+)
3. Click **Sync Now** when Gradle sync is prompted
4. Connect an Android device (USB debugging on) or start an emulator
5. Press **Run (▶)** or `Shift+F10`

### Build APK from command line
```bash
export ANDROID_HOME=/path/to/android-sdk
chmod +x gradlew
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

## Syntax Highlighting Support

Kotlin · Java · JavaScript/TypeScript · Python · HTML · CSS · Shell · JSON · XML
