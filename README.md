# UP Police - Android WebView Application

A modern, production-ready Android WebView application for the [UP Police](https://uppolice.gov.in/) official website.

## Features

- **Native Android Splash Screen** - Material 3 splash screen API
- **Smart WebView** - Optimized with hardware acceleration, caching, and proper cookie handling
- **Network Awareness** - Real-time connectivity monitoring with offline UI
- **Pull-to-Refresh** - Swipe down to reload the current page
- **File Downloads** - Native download manager integration
- **File Upload** - File chooser support for forms
- **Deep Linking** - Handles `uppolice.gov.in` URLs from other apps
- **Back Navigation** - Proper WebView history navigation
- **Dark Mode** - Supports system dark/light theme
- **ProGuard Optimized** - Minified release builds
- **Security** - Network security config, cleartext traffic only for gov.in domains

## Tech Stack

- **Language:** Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Architecture:** Single Activity with clean separation
- **UI:** Material 3, ViewBinding
- **Async:** Kotlin Coroutines + Flow
- **Build:** Gradle Kotlin DSL

## Project Structure

```
app/src/main/
├── kotlin/com/uppolice/app/
│   ├── UPPoliceApplication.kt          # Application class
│   ├── ui/
│   │   └── MainActivity.kt             # Main WebView host
│   ├── util/
│   │   ├── Constants.kt                # App-wide constants
│   │   └── NetworkUtil.kt              # Connectivity observer
│   └── webview/
│       ├── UPPoliceChromeClient.kt     # Chrome client with file chooser
│       ├── UPPoliceWebViewClient.kt    # URL routing & error handling
│       └── WebViewManager.kt           # WebView configuration
├── res/
│   ├── layout/activity_main.xml
│   ├── drawable/                        # Vector icons
│   ├── values/                          # Colors, strings, themes
│   └── xml/                             # Network config, file paths
└── AndroidManifest.xml
```

## Build & Run

1. Open in Android Studio (Hedgehog or later)
2. Sync Gradle
3. Run on device/emulator (API 24+)

### Release Build
```bash
./gradlew assembleRelease
```

## License

See [LICENSE](LICENSE) file.
