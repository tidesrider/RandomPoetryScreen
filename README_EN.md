# Random Poetry Screen / 拾歌
**[English](README_EN.md), [中文](README.md)**

A fully local, server-free "screensaver-like" native Android app. When launched, it displays randomly generated poetry on the screen with a Decryption Effect animation, occupying the phone screen to provide a buffer window for users who unconsciously reach for their phone for entertainment.

## Core Features

- **Random Poetry Display**: Reveals randomly generated poems character-by-character with a decryption animation, keeping the screen always on
- **Poetry Generation**: Three generation modes — random paragraph, random line combination, random paragraph + line combination
- **Anthology Management**: Create anthologies, write/import poetry materials, store by line or by paragraph
- **File Import**: Supports TXT (split by line) and Markdown (split by heading) format imports
- **Color Schemes**: Four geek-style color schemes — Terminal Green, Black & White, Cyan-Blue, Amber-Orange
- **Time Display**: Refresh countdown, app runtime, pomodoro countdown, real-time clock
- **Default Anthologies**: Built-in "Random Poetry" (1189 entries) and "Harmless Prophecies" (1500 entries) anthologies

## Tech Stack

| Category | Choice |
|-----------|--------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM |
| Navigation | Navigation Compose |
| Persistence | JSON files (Gson) + DataStore Preferences |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

## Project Structure

```
app/src/main/java/online/dicemeow/dev_randompoetryscreen/
├── MainActivity.kt              # App entry point
├── AppContainer.kt               # Dependency container
├── data/
│   ├── model/Models.kt           # Data models
│   ├── store/
│   │   ├── JsonDataStore.kt      # Anthology data persistence
│   │   └── PreferencesStore.kt   # User config persistence
│   ├── repository/PoetryRepository.kt
│   ├── generator/PoetryGenerator.kt
│   └── importer/                 # File import & parsing
├── ui/
│   ├── theme/                    # Theme, color, typography
│   ├── components/DecryptionText.kt  # Decryption animation component
│   ├── navigation/               # Routes & navigation
│   ├── main/                     # Main screen
│   ├── anthology/                # Anthology management
│   ├── editor/                   # Text editor
│   └── config/                   # Config screen
└── res/
    ├── font/ark_pixel_12px.ttf   # Pixel font
    ├── raw/
    │   ├── default_poetry.json   # Default anthology: Random Poetry
    │   └── prophet.json          # Default anthology: Harmless Prophecies
    └── values/strings.xml        # App name: 拾歌
```

## Build

### Prerequisites

- Android Studio
- JDK 11+
- Android SDK 36 (path configured in `local.properties` via `sdk.dir`)

### Build APK

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

The generated APK is located at `app/build/outputs/apk/`, named `RandomPoetryScreen-{debug|release}.apk`.

## Documentation

- [PRD.md](Documents/PRD.md) - Product Requirements Document
- [LLD.md](Documents/LLD.md) - Low-Level Design Document
- [BUG_LOG.md](Documents/BUG_LOG.md) - Bug Log

## Permissions

This app requests no system permissions, collects no user data, and does not connect to the internet.

## License

MIT
