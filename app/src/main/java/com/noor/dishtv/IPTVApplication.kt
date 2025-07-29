package com.noor.dishtv

import android.app.Application
import androidx.lifecycle.lifecycleScope
import com.noor.dishtv.data.repository.SampleDataRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class IPTVApplication : Application() {
    
    @Inject
    lateinit var sampleDataRepository: SampleDataRepository
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize sample data for demonstration
        // In a real app, you would load user's playlists
        androidx.lifecycle.ProcessLifecycleOwner.get().lifecycleScope.launch {
            try {
                sampleDataRepository.insertSampleData()
            } catch (e: Exception) {
                // Log error but don't crash the app
                e.printStackTrace()
            }
        }
    }
}