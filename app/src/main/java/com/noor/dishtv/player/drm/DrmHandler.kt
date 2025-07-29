package com.noor.dishtv.player.drm

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import androidx.media3.exoplayer.drm.OfflineLicenseHelper
import com.noor.dishtv.data.model.DrmConfig
import com.noor.dishtv.data.model.DrmScheme
import java.io.File
import java.util.UUID

@UnstableApi
class DrmHandler(private val context: Context) {
    
    companion object {
        private val WIDEVINE_UUID = C.WIDEVINE_UUID
        private val PLAYREADY_UUID = C.PLAYREADY_UUID
        private val CLEARKEY_UUID = C.CLEARKEY_UUID
    }
    
    fun createDrmSessionManager(drmConfig: DrmConfig?): DrmSessionManager {
        if (drmConfig == null || drmConfig.scheme == DrmScheme.NONE) {
            return DrmSessionManager.DRM_UNSUPPORTED
        }
        
        return when (drmConfig.scheme) {
            DrmScheme.WIDEVINE -> createWidevineSessionManager(drmConfig)
            DrmScheme.PLAYREADY -> createPlayReadySessionManager(drmConfig)
            DrmScheme.CLEARKEY -> createClearKeySessionManager(drmConfig)
            DrmScheme.NONE -> DrmSessionManager.DRM_UNSUPPORTED
        }
    }
    
    private fun createWidevineSessionManager(drmConfig: DrmConfig): DrmSessionManager {
        if (!isSchemeSupported(WIDEVINE_UUID)) {
            return DrmSessionManager.DRM_UNSUPPORTED
        }
        
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("DishTV IPTV Player")
            .setAllowCrossProtocolRedirects(true)
        
        // Add custom headers if provided
        if (drmConfig.headers.isNotEmpty()) {
            httpDataSourceFactory.setDefaultRequestProperties(drmConfig.headers)
        }
        
        val drmCallback = HttpMediaDrmCallback(
            drmConfig.licenseUrl ?: "",
            httpDataSourceFactory
        )
        
        val drmSessionManager = DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(WIDEVINE_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
            .build(drmCallback)
        
        return drmSessionManager
    }
    
    private fun createPlayReadySessionManager(drmConfig: DrmConfig): DrmSessionManager {
        if (!isSchemeSupported(PLAYREADY_UUID)) {
            return DrmSessionManager.DRM_UNSUPPORTED
        }
        
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("DishTV IPTV Player")
            .setAllowCrossProtocolRedirects(true)
        
        if (drmConfig.headers.isNotEmpty()) {
            httpDataSourceFactory.setDefaultRequestProperties(drmConfig.headers)
        }
        
        val drmCallback = HttpMediaDrmCallback(
            drmConfig.licenseUrl ?: "",
            httpDataSourceFactory
        )
        
        val drmSessionManager = DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(PLAYREADY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
            .build(drmCallback)
        
        return drmSessionManager
    }
    
    private fun createClearKeySessionManager(drmConfig: DrmConfig): DrmSessionManager {
        if (!isSchemeSupported(CLEARKEY_UUID)) {
            return DrmSessionManager.DRM_UNSUPPORTED
        }
        
        // Handle offline ClearKey
        if (drmConfig.isOfflineKey && !drmConfig.offlineKeyPath.isNullOrEmpty()) {
            return createOfflineClearKeySessionManager(drmConfig)
        }
        
        // Handle online ClearKey
        return if (!drmConfig.clearKeyId.isNullOrEmpty() && !drmConfig.clearKey.isNullOrEmpty()) {
            createInlineClearKeySessionManager(drmConfig)
        } else {
            createHttpClearKeySessionManager(drmConfig)
        }
    }
    
    private fun createOfflineClearKeySessionManager(drmConfig: DrmConfig): DrmSessionManager {
        try {
            val keyFile = File(drmConfig.offlineKeyPath!!)
            if (!keyFile.exists()) {
                return DrmSessionManager.DRM_UNSUPPORTED
            }
            
            val keyData = keyFile.readText()
            val clearKeyCallback = OfflineClearKeyCallback(keyData)
            
            return DefaultDrmSessionManager.Builder()
                .setUuidAndExoMediaDrmProvider(CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
                .build(clearKeyCallback)
        } catch (e: Exception) {
            return DrmSessionManager.DRM_UNSUPPORTED
        }
    }
    
    private fun createInlineClearKeySessionManager(drmConfig: DrmConfig): DrmSessionManager {
        val clearKeyCallback = InlineClearKeyCallback(
            drmConfig.clearKeyId!!,
            drmConfig.clearKey!!
        )
        
        return DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
            .build(clearKeyCallback)
    }
    
    private fun createHttpClearKeySessionManager(drmConfig: DrmConfig): DrmSessionManager {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("DishTV IPTV Player")
            .setAllowCrossProtocolRedirects(true)
        
        if (drmConfig.headers.isNotEmpty()) {
            httpDataSourceFactory.setDefaultRequestProperties(drmConfig.headers)
        }
        
        val drmCallback = HttpMediaDrmCallback(
            drmConfig.licenseUrl ?: "",
            httpDataSourceFactory
        )
        
        return DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(CLEARKEY_UUID, FrameworkMediaDrm.DEFAULT_PROVIDER)
            .build(drmCallback)
    }
    
    fun createProtectedMediaItem(
        url: String,
        drmConfig: DrmConfig?
    ): MediaItem {
        val builder = MediaItem.Builder().setUri(url)
        
        if (drmConfig != null && drmConfig.scheme != DrmScheme.NONE) {
            val drmConfiguration = MediaItem.DrmConfiguration.Builder(getDrmUuid(drmConfig.scheme))
            
            // Add license URL if available
            drmConfig.licenseUrl?.let { licenseUrl ->
                drmConfiguration.setLicenseUri(licenseUrl)
            }
            
            // Add request headers
            if (drmConfig.headers.isNotEmpty()) {
                drmConfiguration.setLicenseRequestHeaders(drmConfig.headers)
            }
            
            // For ClearKey with inline keys
            if (drmConfig.scheme == DrmScheme.CLEARKEY && 
                !drmConfig.clearKeyId.isNullOrEmpty() && 
                !drmConfig.clearKey.isNullOrEmpty()) {
                
                val keySetId = createClearKeySetId(drmConfig.clearKeyId!!, drmConfig.clearKey!!)
                drmConfiguration.setKeySetId(keySetId)
            }
            
            builder.setDrmConfiguration(drmConfiguration.build())
        }
        
        return builder.build()
    }
    
    private fun getDrmUuid(scheme: DrmScheme): UUID {
        return when (scheme) {
            DrmScheme.WIDEVINE -> WIDEVINE_UUID
            DrmScheme.PLAYREADY -> PLAYREADY_UUID
            DrmScheme.CLEARKEY -> CLEARKEY_UUID
            DrmScheme.NONE -> throw IllegalArgumentException("No DRM scheme specified")
        }
    }
    
    private fun createClearKeySetId(keyId: String, key: String): ByteArray {
        // Create a simple key set ID for ClearKey
        return "$keyId:$key".toByteArray()
    }
    
    private fun isSchemeSupported(uuid: UUID): Boolean {
        return try {
            FrameworkMediaDrm.newInstance(uuid) != null
        } catch (e: Exception) {
            false
        }
    }
    
    fun downloadOfflineLicense(
        drmConfig: DrmConfig,
        mediaItem: MediaItem,
        callback: (ByteArray?) -> Unit
    ) {
        if (drmConfig.scheme == DrmScheme.NONE || drmConfig.licenseUrl.isNullOrEmpty()) {
            callback(null)
            return
        }
        
        try {
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("DishTV IPTV Player")
            
            if (drmConfig.headers.isNotEmpty()) {
                httpDataSourceFactory.setDefaultRequestProperties(drmConfig.headers)
            }
            
            val drmCallback = HttpMediaDrmCallback(
                drmConfig.licenseUrl,
                httpDataSourceFactory
            )
            
            val offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(
                drmCallback,
                getDrmSessionManager = { createDrmSessionManager(drmConfig) }
            )
            
            // Download license asynchronously
            Thread {
                try {
                    val keySetId = offlineLicenseHelper.downloadLicense(mediaItem.localConfiguration!!)
                    callback(keySetId)
                } catch (e: Exception) {
                    callback(null)
                }
            }.start()
            
        } catch (e: Exception) {
            callback(null)
        }
    }
    
    fun releaseOfflineLicense(keySetId: ByteArray, drmConfig: DrmConfig) {
        try {
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("DishTV IPTV Player")
            
            if (drmConfig.headers.isNotEmpty()) {
                httpDataSourceFactory.setDefaultRequestProperties(drmConfig.headers)
            }
            
            val drmCallback = HttpMediaDrmCallback(
                drmConfig.licenseUrl ?: "",
                httpDataSourceFactory
            )
            
            val offlineLicenseHelper = OfflineLicenseHelper.newWidevineInstance(
                drmCallback,
                getDrmSessionManager = { createDrmSessionManager(drmConfig) }
            )
            
            offlineLicenseHelper.releaseLicense(keySetId)
        } catch (e: Exception) {
            // Log error but don't throw
        }
    }
}