package com.noor.dishtv.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import com.noor.dishtv.data.database.ChannelDao
import com.noor.dishtv.data.model.Channel
import com.noor.dishtv.player.MediaPlaybackService
import com.noor.dishtv.player.drm.DrmHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val currentChannel: Channel? = null,
    val exoPlayer: ExoPlayer? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val availableChannels: List<Channel> = emptyList(),
    val currentChannelIndex: Int = -1,
    val error: String? = null
)

@UnstableApi
@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val channelDao: ChannelDao,
    private val drmHandler: DrmHandler
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    
    private var mediaController: MediaController? = null
    private var playbackService: MediaPlaybackService? = null
    
    init {
        loadAvailableChannels()
        initializePlayer()
    }
    
    private fun initializePlayer() {
        // ExoPlayer will be initialized when MediaController is available
    }
    
    private fun loadAvailableChannels() {
        viewModelScope.launch {
            try {
                channelDao.getEnabledChannels().collect { channels ->
                    _uiState.update { currentState ->
                        val currentIndex = if (currentState.currentChannel != null) {
                            channels.indexOfFirst { it.id == currentState.currentChannel.id }
                        } else {
                            -1
                        }
                        
                        currentState.copy(
                            availableChannels = channels,
                            currentChannelIndex = currentIndex
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to load channels: ${e.message}")
                }
            }
        }
    }
    
    fun playChannel(channel: Channel, controller: MediaController?) {
        viewModelScope.launch {
            try {
                mediaController = controller
                
                // Update current channel
                val channelIndex = _uiState.value.availableChannels.indexOfFirst { it.id == channel.id }
                _uiState.update { 
                    it.copy(
                        currentChannel = channel,
                        currentChannelIndex = channelIndex,
                        error = null
                    )
                }
                
                // Create authenticated media item
                val mediaItem = if (channel.drmConfig != null) {
                    drmHandler.createProtectedMediaItem(channel.url, channel.drmConfig)
                } else {
                    androidx.media3.common.MediaItem.fromUri(channel.url)
                }
                
                // Set headers for authentication if needed
                if (channel.headers.isNotEmpty() || channel.cookies.isNotEmpty()) {
                    // Handle authentication headers and cookies
                    // This would be done through custom DataSource.Factory
                }
                
                // Play through MediaController or ExoPlayer
                mediaController?.let { controller ->
                    controller.setMediaItem(mediaItem)
                    controller.prepare()
                    controller.play()
                } ?: run {
                    // Direct ExoPlayer control if MediaController not available
                    _uiState.value.exoPlayer?.let { player ->
                        player.setMediaItem(mediaItem)
                        player.prepare()
                        player.play()
                    }
                }
                
                // Update playback position in database
                channelDao.updatePlaybackPosition(
                    channel.id,
                    0L, // Reset position for live streams
                    System.currentTimeMillis()
                )
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to play channel: ${e.message}")
                }
            }
        }
    }
    
    fun changeChannel(direction: Int) {
        val currentState = _uiState.value
        val availableChannels = currentState.availableChannels
        val currentIndex = currentState.currentChannelIndex
        
        if (availableChannels.isEmpty() || currentIndex == -1) return
        
        val newIndex = when {
            direction > 0 -> (currentIndex + 1) % availableChannels.size
            direction < 0 -> if (currentIndex == 0) availableChannels.size - 1 else currentIndex - 1
            else -> currentIndex
        }
        
        val newChannel = availableChannels[newIndex]
        playChannel(newChannel, mediaController)
    }
    
    fun setExoPlayer(player: ExoPlayer) {
        _uiState.update { it.copy(exoPlayer = player) }
    }
    
    fun togglePlayPause() {
        val player = _uiState.value.exoPlayer ?: mediaController
        player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }
    
    fun seekTo(positionMs: Long) {
        val player = _uiState.value.exoPlayer ?: mediaController
        player?.seekTo(positionMs)
    }
    
    fun setVolume(volume: Float) {
        _uiState.value.exoPlayer?.volume = volume
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        _uiState.value.exoPlayer?.release()
        mediaController?.release()
    }
}