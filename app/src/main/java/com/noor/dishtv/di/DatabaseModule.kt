package com.noor.dishtv.di

import android.content.Context
import androidx.room.Room
import com.noor.dishtv.data.database.IPTVDatabase
import com.noor.dishtv.data.database.ChannelDao
import com.noor.dishtv.data.database.PlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideIPTVDatabase(@ApplicationContext context: Context): IPTVDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            IPTVDatabase::class.java,
            "iptv_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideChannelDao(database: IPTVDatabase): ChannelDao {
        return database.channelDao()
    }
    
    @Provides
    fun providePlaylistDao(database: IPTVDatabase): PlaylistDao {
        return database.playlistDao()
    }
}