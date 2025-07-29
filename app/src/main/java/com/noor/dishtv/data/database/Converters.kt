package com.noor.dishtv.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noor.dishtv.data.model.DrmConfig
import com.noor.dishtv.data.model.DrmScheme
import com.noor.dishtv.data.model.PlaylistType
import com.noor.dishtv.data.model.StreamType

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromDrmConfig(value: DrmConfig?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toDrmConfig(value: String?): DrmConfig? {
        return value?.let { gson.fromJson(it, DrmConfig::class.java) }
    }

    @TypeConverter
    fun fromStreamType(value: StreamType): String {
        return value.name
    }

    @TypeConverter
    fun toStreamType(value: String): StreamType {
        return StreamType.valueOf(value)
    }

    @TypeConverter
    fun fromDrmScheme(value: DrmScheme): String {
        return value.name
    }

    @TypeConverter
    fun toDrmScheme(value: String): DrmScheme {
        return DrmScheme.valueOf(value)
    }

    @TypeConverter
    fun fromPlaylistType(value: PlaylistType): String {
        return value.name
    }

    @TypeConverter
    fun toPlaylistType(value: String): PlaylistType {
        return PlaylistType.valueOf(value)
    }
}