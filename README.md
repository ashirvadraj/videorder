# Videorder

> **“Your media. Your downloads. Your control.”**

Videorder is a production-ready, privacy-focused universal media downloader and file manager built for Android. It enables users to download videos, audio, and documents from websites and supported services where downloading is technically permitted and the user is authorized.

---

## 🌟 Key Features

### 1. Universal URL Downloader
- **Auto-Detection**: Paste any direct media link or supported webpage URL. Videorder validates the URL, extracts stream endpoints, and presents selectable quality profiles (e.g., 1080p Full HD, 720p HD, 480p, 360p, or Audio-only MP3).
- **Format Inspection**: Probes `Content-Type`, `Content-Length`, `Content-Disposition`, and HTTP Range headers via non-intrusive HEAD requests.
- **Strict DRM & Security Compliance**: Strictly respects Widevine, FairPlay, and platform access restrictions. Never attempts DRM circumvention or unauthorized scraping.

### 2. High-Performance Download Manager & Queue
- **Foreground Service**: Downloads continue uninterrupted when the application is minimized, screen is locked, or configuration changes occur.
- **Interactive Notifications**: Real-time progress bar, speed (e.g. `2.4 MB/s`), remaining time (ETA), and actionable **Pause** / **Resume** / **Cancel** buttons.
- **Resumable HTTP Range Requests**: Uses `Range: bytes={offset}-` with `RandomAccessFile` streaming. If Wi-Fi disconnects or network drops, downloads resume from the exact interrupted byte.
- **Configurable Queue**: Control concurrent downloads (1 to 5 simultaneous tasks) with automated queue draining.
- **Queue Actions**: Pause All, Resume All, Cancel All.

### 3. Official Telegram Integration
- **Official Client Authentication**: Connects using Telegram's official authentication flow (phone number verification code).
- **Hardware-Backed Keystore Security**: Auth tokens and session secrets are encrypted with **AES-256 GCM** backed by the Android KeyStore (`AndroidKeyStore`). No plaintext secrets are ever written to disk or logs.
- **Explore Media**: Browse Saved Messages, private chats, channels, and groups you belong to.
- **Multi-Selection Queue**: Select multiple media files (videos, photos, documents, audio) and download them together in an automated queue.
- **Instant Demo Sandbox**: Includes a built-in sandbox preview mode to test chats and queue downloads without needing active phone verification.
- **Instant Session Wipe**: Clean "Logout from Telegram" button completely purges KeyStore keys and local metadata.

### 4. File Management & Media Player
- **Categorized Library**: Videos, Images, Audio, Documents, and All Files.
- **File Inspector**: Filename, formatted size, MIME type, last modified date, resolution, duration, and storage path.
- **Operations**: In-app media playback preview, Rename, System Share sheet (`ACTION_SEND`), and Delete.

### 5. Download History
- **Date Grouping**: Grouped into Today, Yesterday, Earlier this week, and Older.
- **Search & Filter**: Search completed downloads by filename or source URL.
- **Safe History Deletion**: Clearing history records never deletes actual media files from storage unless explicitly selected by the user.

### 6. Android Share Target Integration
- Registered as an Android Share target for `text/plain` and direct media intents. Share any link directly from Chrome, YouTube, Telegram, or any browser to Videorder to immediately open the stream detection dialog.

---

## 🏗️ Architecture & Technology Stack

Videorder is engineered according to Google's recommended **Clean Architecture** and **MVVM** principles:

```
app/
├── data/
│   ├── database/
│   │   ├── entities/        # Room entities (DownloadTask, History, Files, Telegram, Settings)
│   │   ├── dao/             # Room DAOs with reactive Kotlin Coroutines Flows
│   │   └── AppDatabase.kt   # Database configuration & singleton builder
│   └── repositories/        # Repository implementations (Download, History, File, Settings, Telegram)
├── domain/
│   ├── models/              # Pure domain models (DownloadTask, MediaItem, MediaQuality, FileItem)
│   └── usecases/            # Use cases (DetectMedia, StartDownload, ManageDownload, TelegramAuth)
├── presentation/
│   ├── common/              # Material 3 Theme, Dark palette, reusable UI components
│   ├── navigation/          # NavGraph and screen destination routing
│   ├── splash/              # Animated splash screen
│   ├── onboarding/          # First-launch onboarding walkthrough
│   ├── home/                # URL input, detection, quality selection dialog
│   ├── downloads/           # Active downloads queue & completed downloads
│   ├── telegram/            # Official Telegram media explorer & login flow
│   ├── files/               # File manager & media playback dialog
│   └── settings/            # Storage, Wi-Fi constraints, Theme, Privacy, About
├── downloader/
│   ├── core/                # MediaExtractor, Downloader, MediaResolver interfaces
│   ├── network/             # Resumable HttpDownloader with Range requests, NetworkMonitor
│   ├── providers/           # DirectMediaProvider, GenericProvider, TelegramProvider
│   ├── queue/               # DownloadQueueManager with concurrent execution & constraint handling
│   ├── services/            # DownloadForegroundService & DownloadWorker
│   └── notifications/       # DownloadNotificationManager with interactive actions
├── security/                # SecureKeystoreManager (AES-256 GCM) & TelegramSessionStore
└── utils/                   # FileUtils, Formatters, UrlValidator
```

---

## 🔒 Privacy & Security Guarantee

- **100% On-Device**: No remote relay servers, no telemetry, and no third-party analytics.
- **Hardware Encryption**: Sensitive credentials encrypted with AES-256 GCM inside Android KeyStore.
- **Zero Data Collection**: No user tracking, no data monetization.
- **No DRM Circumvention**: Adheres strictly to platform terms and authorized access boundaries.

---

## 🛠️ Build and Installation

### Prerequisites
- JDK 17
- Android SDK (API 34+)

### Build APK
To run the automated test suite:
```bash
./gradlew test
```

To assemble the debug APK:
```bash
./gradlew assembleDebug
```

The compiled APK will be generated at:
`release/Videorder-v1.0.0-debug.apk` and `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 License
Videorder is open-source software licensed under the MIT License.
