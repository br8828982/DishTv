package com.noor.dishtv.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.noor.dishtv.data.model.Channel
import com.noor.dishtv.data.model.Playlist

@Database(
    entities = [Channel::class, Playlist::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class IPTVDatabase : RoomDatabase() {
    
    abstract fun channelDao(): ChannelDao
    abstract fun playlistDao(): PlaylistDao
    
    companion object {
        @Volatile
        private var INSTANCE: IPTVDatabase? = null
        
        fun getDatabase(context: Context): IPTVDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IPTVDatabase::class.java,
                    "iptv_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}