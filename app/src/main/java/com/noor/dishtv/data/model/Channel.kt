package com.noor.dishtv.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("url")
    val url: String,
    
    @SerializedName("logo")
    val logoUrl: String? = null,
    
    @SerializedName("group")
    val group: String? = null,
    
    @SerializedName("epg_id")
    val epgId: String? = null,
    
    // Stream type (HLS, DASH, etc.)
    val streamType: StreamType = StreamType.AUTO,
    
    // DRM Configuration
    val drmConfig: DrmConfig? = null,
    
    // Authentication headers
    val headers: Map<String, String> = emptyMap(),
    
    // Cookies for authentication
    val cookies: Map<String, String> = emptyMap(),
    
    // User agent
    val userAgent: String? = null,
    
    // Referrer
    val referrer: String? = null,
    
    // Additional metadata
    val language: String? = null,
    val country: String? = null,
    val resolution: String? = null,
    val codec: String? = null,
    
    // Playback settings
    val isEnabled: Boolean = true,
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,
    
    // Offline support
    val isOfflineAvailable: Boolean = false,
    val offlinePath: String? = null,
    
    // Last played information
    val lastPlayedPosition: Long = 0,
    val lastPlayedTimestamp: Long = 0
)

enum class StreamType {
    AUTO,
    HLS,
    DASH,
    SMOOTH_STREAMING,
    PROGRESSIVE_HTTP,
    RTMP,
    RTSP
}

data class DrmConfig(
    val scheme: DrmScheme,
    val licenseUrl: String? = null,
    val keyId: String? = null,
    val key: String? = null,
    val clearKeyId: String? = null,
    val clearKey: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val isOfflineKey: Boolean = false,
    val offlineKeyPath: String? = null
)

enum class DrmScheme {
    NONE,
    WIDEVINE,
    PLAYREADY,
    CLEARKEY
}