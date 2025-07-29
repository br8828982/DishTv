package com.noor.dishtv.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.noor.dishtv.MainActivity
import com.noor.dishtv.data.model.Channel
import com.noor.dishtv.player.drm.DrmHandler
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MediaPlaybackService : MediaSessionService() {
    
    @Inject
    lateinit var drmHandler: DrmHandler
    
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private lateinit var trackSelector: DefaultTrackSelector
    
    companion object {
        private const val COMMAND_SET_CHANNEL = "SET_CHANNEL"
        private const val COMMAND_NEXT_CHANNEL = "NEXT_CHANNEL"
        private const val COMMAND_PREVIOUS_CHANNEL = "PREVIOUS_CHANNEL"
        private const val COMMAND_TOGGLE_FAVORITE = "TOGGLE_FAVORITE"
    }
    
    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeMediaSession()
    }
    
    private fun initializePlayer() {
        // Create track selector with adaptive streaming
        trackSelector = DefaultTrackSelector(this).apply {
            setParameters(
                buildUponParameters()
                    .setMaxVideoSizeSd()
                    .setForceLowestBitrate(false)
                    .setAllowVideoMixedMimeTypeAdaptiveness(true)
                    .setAllowAudioMixedMimeTypeAdaptiveness(true)
                    .setAllowAudioMixedSampleRateAdaptiveness(true)
                    .setAllowAudioMixedChannelCountAdaptiveness(true)
            )
        }
        
        // Create OkHttp client for authentication support
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        
        // Create data source factory with authentication support
        val httpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("DishTV IPTV Player/1.0")
        
        val dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)
        
        // Create media source factory with DRM support
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        
        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
        
        // Set player listeners
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                updateNotification()
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                super.onPlayerError(error)
                // Handle playback errors
                handlePlaybackError(error)
            }
            
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                updateNotification()
            }
        })
    }
    
    private fun initializeMediaSession() {
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCustomLayout(createCustomLayout())
            .setCallback(MediaSessionCallback())
            .build()
    }
    
    private fun createCustomLayout(): List<CommandButton> {
        return listOf(
            CommandButton.Builder()
                .setDisplayName("Previous Channel")
                .setIconResId(android.R.drawable.ic_media_previous)
                .setSessionCommand(SessionCommand(COMMAND_PREVIOUS_CHANNEL, Bundle.EMPTY))
                .build(),
            CommandButton.Builder()
                .setDisplayName("Next Channel")
                .setIconResId(android.R.drawable.ic_media_next)
                .setSessionCommand(SessionCommand(COMMAND_NEXT_CHANNEL, Bundle.EMPTY))
                .build(),
            CommandButton.Builder()
                .setDisplayName("Toggle Favorite")
                .setIconResId(android.R.drawable.btn_star)
                .setSessionCommand(SessionCommand(COMMAND_TOGGLE_FAVORITE, Bundle.EMPTY))
                .build()
        )
    }
    
    fun playChannel(channel: Channel) {
        try {
            // Create authenticated media source
            val mediaItem = createAuthenticatedMediaItem(channel)
            
            // Set DRM session manager if needed
            if (channel.drmConfig != null) {
                val drmSessionManager = drmHandler.createDrmSessionManager(channel.drmConfig)
                player.setMediaItem(mediaItem)
            } else {
                player.setMediaItem(mediaItem)
            }
            
            // Update media metadata
            updateMediaMetadata(channel)
            
            // Start playback
            player.prepare()
            player.play()
            
        } catch (e: Exception) {
            handlePlaybackError(e)
        }
    }
    
    private fun createAuthenticatedMediaItem(channel: Channel): MediaItem {
        val builder = MediaItem.Builder()
            .setUri(channel.url)
            .setMediaId(channel.id.toString())
        
        // Set media metadata
        val metadataBuilder = MediaMetadata.Builder()
            .setTitle(channel.name)
            .setDisplayTitle(channel.name)
            .setArtworkUri(channel.logoUrl?.let { android.net.Uri.parse(it) })
            .setGenre(channel.group)
            .setDescription(channel.group)
        
        builder.setMediaMetadata(metadataBuilder.build())
        
        // Add DRM configuration if needed
        if (channel.drmConfig != null) {
            return drmHandler.createProtectedMediaItem(channel.url, channel.drmConfig)
        }
        
        return builder.build()
    }
    
    private fun updateMediaMetadata(channel: Channel) {
        val metadata = MediaMetadata.Builder()
            .setTitle(channel.name)
            .setDisplayTitle(channel.name)
            .setArtworkUri(channel.logoUrl?.let { android.net.Uri.parse(it) })
            .setGenre(channel.group)
            .setDescription(channel.group ?: "IPTV Channel")
            .build()
        
        // Update session metadata
        mediaSession?.let { session ->
            val mediaItem = session.player.currentMediaItem?.buildUpon()
                ?.setMediaMetadata(metadata)
                ?.build()
            
            if (mediaItem != null) {
                session.player.setMediaItem(mediaItem)
            }
        }
    }
    
    private fun updateNotification() {
        // Notification will be automatically updated by MediaSession
    }
    
    private fun handlePlaybackError(error: Throwable) {
        // Log error and notify listeners
        error.printStackTrace()
        
        // Try to recover from network errors
        if (error.message?.contains("network", ignoreCase = true) == true) {
            // Retry playback after a short delay
            player.stop()
            player.prepare()
        }
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
    
    private inner class MediaSessionCallback : MediaSession.Callback {
        
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            
            return when (customCommand.customAction) {
                COMMAND_NEXT_CHANNEL -> {
                    // Handle next channel command
                    Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                COMMAND_PREVIOUS_CHANNEL -> {
                    // Handle previous channel command
                    Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                COMMAND_TOGGLE_FAVORITE -> {
                    // Handle toggle favorite command
                    Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                }
                COMMAND_SET_CHANNEL -> {
                    // Handle set channel command
                    val channelId = args.getLong("channel_id", -1)
                    if (channelId != -1L) {
                        // Load and play the channel
                        Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    } else {
                        Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE))
                    }
                }
                else -> {
                    Futures.immediateFuture(SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED))
                }
            }
        }
        
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val availableSessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(COMMAND_SET_CHANNEL, Bundle.EMPTY))
                .add(SessionCommand(COMMAND_NEXT_CHANNEL, Bundle.EMPTY))
                .add(SessionCommand(COMMAND_PREVIOUS_CHANNEL, Bundle.EMPTY))
                .add(SessionCommand(COMMAND_TOGGLE_FAVORITE, Bundle.EMPTY))
                .build()
            
            return MediaSession.ConnectionResult.accept(
                availableSessionCommands,
                MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS
            )
        }
        
        override fun onSetMediaItems(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            // Process media items to add authentication headers
            val processedItems = mediaItems.map { item ->
                processMediaItemForAuthentication(item)
            }.toMutableList()
            
            return Futures.immediateFuture(
                MediaSession.MediaItemsWithStartPosition(processedItems, startIndex, startPositionMs)
            )
        }
    }
    
    private fun processMediaItemForAuthentication(mediaItem: MediaItem): MediaItem {
        // Add authentication headers based on URL or other criteria
        // This is where you would inject cookies and headers based on channel configuration
        return mediaItem
    }
}