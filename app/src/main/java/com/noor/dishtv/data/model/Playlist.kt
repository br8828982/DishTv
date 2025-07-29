package com.noor.dishtv.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("url")
    val url: String? = null,
    
    @SerializedName("local_path")
    val localPath: String? = null,
    
    // Playlist type
    val type: PlaylistType = PlaylistType.M3U,
    
    // Authentication
    val requiresAuth: Boolean = false,
    val headers: Map<String, String> = emptyMap(),
    val cookies: Map<String, String> = emptyMap(),
    val userAgent: String? = null,
    val referrer: String? = null,
    
    // Update settings
    val autoUpdate: Boolean = false,
    val updateIntervalMinutes: Int = 60,
    val lastUpdated: Long = 0,
    
    // Statistics
    val channelCount: Int = 0,
    val totalSize: Long = 0,
    
    // Settings
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
    
    // EPG configuration
    val epgUrl: String? = null,
    val epgHeaders: Map<String, String> = emptyMap(),
    
    // Created and modified timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)

enum class PlaylistType {
    M3U,
    M3U8,
    XSPF,
    JSON
}

data class PlaylistMetadata(
    val title: String? = null,
    val description: String? = null,
    val creator: String? = null,
    val version: String? = null,
    val image: String? = null,
    val url: String? = null,
    val allowCache: Boolean = true,
    val allowCookies: Boolean = true,
    val trackCount: Int = 0
)

data class M3UEntry(
    val duration: Double = -1.0,
    val title: String,
    val url: String,
    val attributes: Map<String, String> = emptyMap(),
    val logoUrl: String? = null,
    val groupTitle: String? = null,
    val language: String? = null,
    val country: String? = null,
    val id: String? = null,
    val epgId: String? = null,
    val resolution: String? = null,
    val codec: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val drmConfig: DrmConfig? = null
)