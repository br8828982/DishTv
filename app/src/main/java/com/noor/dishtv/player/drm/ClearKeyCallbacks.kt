package com.noor.dishtv.player.drm

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.drm.MediaDrmCallback
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.util.UUID

@UnstableApi
class OfflineClearKeyCallback(private val keyData: String) : MediaDrmCallback {
    
    override fun executeProvisionRequest(uuid: UUID, request: MediaDrmCallback.ProvisionRequest): ByteArray {
        // ClearKey doesn't require provisioning
        return ByteArray(0)
    }
    
    override fun executeKeyRequest(uuid: UUID, request: MediaDrmCallback.KeyRequest): ByteArray {
        try {
            // Parse the offline key data (assuming JSON format)
            val gson = Gson()
            val keyObject = gson.fromJson(keyData, JsonObject::class.java)
            
            // Return the key data as response
            return keyObject.toString().toByteArray()
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse offline ClearKey data", e)
        }
    }
}

@UnstableApi
class InlineClearKeyCallback(
    private val keyId: String,
    private val key: String
) : MediaDrmCallback {
    
    override fun executeProvisionRequest(uuid: UUID, request: MediaDrmCallback.ProvisionRequest): ByteArray {
        // ClearKey doesn't require provisioning
        return ByteArray(0)
    }
    
    override fun executeKeyRequest(uuid: UUID, request: MediaDrmCallback.KeyRequest): ByteArray {
        try {
            // Create ClearKey response format
            val gson = Gson()
            val keyObject = JsonObject().apply {
                addProperty("kty", "oct")
                addProperty("kid", keyId)
                addProperty("k", key)
            }
            
            val responseObject = JsonObject().apply {
                add("keys", gson.toJsonTree(arrayOf(keyObject)))
                addProperty("type", "temporary")
            }
            
            return responseObject.toString().toByteArray()
        } catch (e: Exception) {
            throw RuntimeException("Failed to create ClearKey response", e)
        }
    }
}