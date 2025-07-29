package com.noor.dishtv.di

import android.content.Context
import com.noor.dishtv.player.drm.DrmHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {
    
    @Provides
    @Singleton
    fun provideDrmHandler(@ApplicationContext context: Context): DrmHandler {
        return DrmHandler(context)
    }
}