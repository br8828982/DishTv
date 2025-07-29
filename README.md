# DishTV - Advanced Android IPTV Player

A modern, feature-rich Android IPTV player built with the latest Android technologies, specifically designed for elderly users with large UI elements and simple navigation.

## 🚀 Features

### Core Video Playback
- **Universal Codec Support**: Built with Media3/ExoPlayer supporting all major video and audio codecs
- **Streaming Protocols**: Full support for HLS, DASH, M3U8, M3U, Smooth Streaming, RTMP, RTSP
- **Adaptive Streaming**: Automatic quality adjustment based on network conditions
- **Hardware Acceleration**: Optimized for performance across all Android devices

### DRM & Security
- **Widevine DRM**: Full support for Widevine DRM protected content
- **PlayReady DRM**: Microsoft PlayReady DRM implementation
- **ClearKey DRM**: Support for ClearKey DRM with both online and offline keys
- **Offline Key Support**: Store DRM keys locally for offline playback
- **Custom Key Management**: Manual key injection for testing and development

### Authentication & Headers
- **Cookie Injection**: Automatic cookie handling for authenticated streams
- **Custom Headers**: Support for custom HTTP headers (User-Agent, Referer, Authorization, etc.)
- **OkHttp Integration**: Advanced networking with connection pooling and retry mechanisms
- **Authentication Passthrough**: Seamless authentication for premium content

### Playlist Management
- **M3U/M3U8 Parser**: Advanced parser supporting all IPTV playlist formats
- **Extended Attributes**: Support for TVG tags, group titles, logos, EPG mapping
- **Bulk Import**: Import thousands of channels from playlist URLs or local files
- **Auto-Update**: Automatic playlist refresh with configurable intervals
- **Group Organization**: Automatic channel grouping and filtering

### User Interface (Elderly-Friendly)
- **Large UI Elements**: All buttons, text, and logos are oversized for easy visibility
- **High Contrast**: Clear visual separation and high contrast design
- **Simple Navigation**: Intuitive channel switching with remote control support
- **Large Channel Logos**: Prominent channel branding for easy recognition
- **Voice Control Ready**: Prepared for future voice control integration

### Android TV Support
- **Leanback Launcher**: Native Android TV launcher integration
- **D-Pad Navigation**: Full remote control support for TV interfaces
- **Focus Management**: Proper focus handling for TV navigation
- **Picture-in-Picture**: Minimize to PiP mode while browsing channels

## 🏗️ Technical Architecture

### Technology Stack
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Hilt dependency injection
- **Media**: Media3/ExoPlayer 1.4.1 with FFmpeg decoder support
- **Database**: Room database for local data persistence
- **Networking**: Retrofit + OkHttp for playlist management
- **Image Loading**: Coil for channel logo loading

### Project Structure
```
app/
├── src/main/java/com/noor/dishtv/
│   ├── data/
│   │   ├── database/          # Room database, DAOs, entities
│   │   ├── model/             # Data models (Channel, Playlist, DRM)
│   │   ├── parser/            # M3U/M3U8 playlist parsers
│   │   └── repository/        # Data repositories
│   ├── di/                    # Hilt dependency injection modules
│   ├── player/
│   │   ├── drm/              # DRM handlers (Widevine, PlayReady, ClearKey)
│   │   └── MediaPlaybackService.kt
│   └── ui/
│       ├── components/        # Reusable UI components
│       ├── screens/           # Main app screens
│       ├── theme/             # Material 3 theming
│       └── viewmodel/         # ViewModels for UI state management
```

### Key Components

#### 1. Advanced DRM Handler (`DrmHandler.kt`)
```kotlin
// Supports all major DRM schemes
- Widevine DRM with license URL and custom headers
- PlayReady DRM for Microsoft ecosystem
- ClearKey DRM with inline or offline key files
- Automatic DRM scheme detection and fallback
```

#### 2. Comprehensive M3U Parser (`M3UParser.kt`)
```kotlin
// Parses extended M3U attributes:
- TVG-ID, TVG-NAME, TVG-LOGO for EPG integration
- Group-title for channel organization
- Custom HTTP headers and cookies
- DRM configuration within playlist entries
- Language and country metadata
```

#### 3. Media Playback Service (`MediaPlaybackService.kt`)
```kotlin
// Media3-based service providing:
- Background playback with notifications
- MediaSession integration for lock screen controls
- Automatic reconnection on network errors
- Custom track selection for quality optimization
```

#### 4. Channel Management System
```kotlin
// Room database with:
- Offline channel storage and caching
- Favorite channels management
- Playback position tracking
- Channel grouping and filtering
- Full-text search capabilities
```

## 📱 User Interface Design

### Elderly-Friendly Features
- **Font Sizes**: 18sp+ for all text, 24sp+ for headers
- **Button Sizes**: Minimum 48dp touch targets, 56dp+ for primary actions
- **Logo Sizes**: 80dp channel logos for easy recognition
- **Color Contrast**: High contrast ratios meeting WCAG 2.1 AA standards
- **Simplified Navigation**: Maximum 3 levels deep, clear back button behavior

### Screen Layouts

#### Channel List Screen
- Grid view with large channel cards (140dp height)
- Search bar with large text input (18sp)
- Filter chips for easy group selection
- Favorite star prominently displayed
- Channel count and loading indicators

#### Player Screen
- Fullscreen video with custom controls
- Large play/pause buttons (80dp)
- Channel switching with skip buttons (60dp)
- Volume controls and settings access
- Channel info overlay with large text (20sp)

#### Settings Screen (Planned)
- DRM key management interface
- HTTP headers and cookies configuration
- Playlist import/export functionality
- Playback quality preferences

## 🔧 Installation & Setup

### Prerequisites
- Android Studio Flamingo or later
- Android SDK 34 or higher
- Kotlin 2.0+ support
- Java 11+ for development

### Building the Project
```bash
git clone <repository-url>
cd dishtv-android
./gradlew build
```

### Installing on Device
```bash
./gradlew installDebug
```

## 📊 Supported Formats

### Video Codecs
- H.264 (AVC)
- H.265 (HEVC)
- VP8, VP9
- AV1 (on supported devices)
- MPEG-2, MPEG-4

### Audio Codecs
- AAC, AAC+
- MP3, MP2
- AC3, EAC3
- DTS (on supported devices)
- Opus, Vorbis

### Streaming Protocols
- HLS (HTTP Live Streaming)
- DASH (Dynamic Adaptive Streaming)
- Smooth Streaming
- Progressive HTTP
- RTMP, RTMPS
- RTSP
- UDP Multicast

### Playlist Formats
- M3U, M3U8 (with extended attributes)
- XSPF (XML Shareable Playlist Format)
- JSON playlists (custom format)

## 🔐 DRM Configuration Examples

### Widevine DRM
```kotlin
val drmConfig = DrmConfig(
    scheme = DrmScheme.WIDEVINE,
    licenseUrl = "https://drm.provider.com/widevine",
    headers = mapOf(
        "Authorization" -> "Bearer your_token",
        "X-Custom-Header" -> "custom_value"
    )
)
```

### ClearKey DRM (Inline)
```kotlin
val drmConfig = DrmConfig(
    scheme = DrmScheme.CLEARKEY,
    clearKeyId = "key_id_base64_encoded",
    clearKey = "key_value_base64_encoded"
)
```

### ClearKey DRM (Offline File)
```kotlin
val drmConfig = DrmConfig(
    scheme = DrmScheme.CLEARKEY,
    isOfflineKey = true,
    offlineKeyPath = "/storage/emulated/0/keys/channel_key.json"
)
```

## 🎯 Channel Authentication Examples

### Custom Headers
```kotlin
val channel = Channel(
    name = "Premium Sports",
    url = "https://stream.provider.com/sports.m3u8",
    headers = mapOf(
        "User-Agent" -> "DishTV Player/1.0",
        "Referer" -> "https://provider.com",
        "Authorization" -> "Bearer premium_token"
    )
)
```

### Cookie Authentication
```kotlin
val channel = Channel(
    name = "Authenticated Channel",
    url = "https://stream.provider.com/channel.m3u8",
    cookies = mapOf(
        "session_id" -> "abc123456789",
        "auth_token" -> "xyz987654321"
    )
)
```

## 🔄 Playlist Import Examples

### URL-based Import
```kotlin
val playlist = Playlist(
    name = "IPTV Provider",
    url = "https://provider.com/playlist.m3u8",
    headers = mapOf("Authorization" -> "Bearer token"),
    autoUpdate = true,
    updateIntervalMinutes = 60
)
```

### Local File Import
```kotlin
val playlist = Playlist(
    name = "Local Channels",
    localPath = "/storage/emulated/0/playlists/local.m3u",
    type = PlaylistType.M3U
)
```

## 🚀 Performance Optimizations

### Memory Management
- Aggressive bitmap recycling for channel logos
- Lazy loading of channel data
- Efficient database queries with pagination
- Background thread processing for heavy operations

### Network Optimization
- Connection pooling with OkHttp
- Automatic retry with exponential backoff
- Bandwidth-aware quality selection
- Prefetching of playlist metadata

### Battery Optimization
- Foreground service for playback
- Wake lock management
- Background processing limits
- Efficient encoder/decoder usage

## 🔒 Security Features

### Privacy Protection
- No analytics or tracking by default
- Local data storage only
- User-controlled data sharing
- Secure credential storage

### Content Protection
- DRM license caching
- Secure key storage
- Certificate pinning support
- HTTPS enforcement where possible

## 🛠️ Development Guidelines

### Code Style
- Kotlin coding conventions
- MVVM architecture pattern
- Dependency injection with Hilt
- Reactive programming with Coroutines/Flow

### Testing Strategy
- Unit tests for business logic
- Integration tests for database operations
- UI tests for critical user flows
- Mock servers for network testing

### Contribution Guidelines
- Feature branches for new development
- Pull request reviews required
- Automated testing on CI/CD
- Documentation updates with features

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Support

For support and questions:
- Create an issue on GitHub
- Contact the development team
- Check the documentation wiki

## 🔮 Future Enhancements

### Planned Features
- Voice control integration
- Chromecast support
- EPG (Electronic Program Guide) integration
- Recording and timeshift functionality
- Multi-language subtitle support
- Parental controls
- Cloud sync for favorites and settings

### Accessibility Improvements
- Screen reader compatibility
- High contrast themes
- Larger touch targets option
- Voice announcements
- Simplified mode for basic functionality

---

**Note**: This IPTV player is designed for legitimate streaming content only. Users are responsible for ensuring they have proper rights to access any content through this application.