package com.noor.dishtv.di

import com.noor.dishtv.data.database.ChannelDao
import com.noor.dishtv.data.database.PlaylistDao
import com.noor.dishtv.data.repository.SampleDataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideSampleDataRepository(
        channelDao: ChannelDao,
        playlistDao: PlaylistDao
    ): SampleDataRepository {
        return SampleDataRepository(channelDao, playlistDao)
    }
}