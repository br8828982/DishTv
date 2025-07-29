package com.noor.dishtv

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.common.util.concurrent.ListenableFuture
import com.noor.dishtv.data.model.Channel
import com.noor.dishtv.player.MediaPlaybackService
import com.noor.dishtv.ui.components.PlayerView
import com.noor.dishtv.ui.screens.ChannelListScreen
import com.noor.dishtv.ui.theme.DishTvTheme
import com.noor.dishtv.ui.viewmodel.ChannelListViewModel
import com.noor.dishtv.ui.viewmodel.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@UnstableApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private var mediaController: MediaController? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        initializeMediaController()
        
        setContent {
            DishTvTheme {
                IPTVApp(
                    mediaController = mediaController,
                    onFinish = { finish() }
                )
            }
        }
    }
    
    private fun initializeMediaController() {
        val sessionToken = SessionToken(
            this,
            ComponentName(this, MediaPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
        }, mainExecutor)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaController?.release()
        if (::controllerFuture.isInitialized) {
            MediaController.releaseFuture(controllerFuture)
        }
    }
}

@UnstableApi
@Composable
fun IPTVApp(
    mediaController: MediaController?,
    onFinish: () -> Unit = {}
) {
    val navController = rememberNavController()
    var currentChannel by remember { mutableStateOf<Channel?>(null) }
    var isPlayerFullscreen by remember { mutableStateOf(false) }
    
    // Handle back press to exit fullscreen or close app
    BackHandler(enabled = isPlayerFullscreen) {
        isPlayerFullscreen = false
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "channel_list",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Channel List Screen
            composable("channel_list") {
                val viewModel: ChannelListViewModel = hiltViewModel()
                
                ChannelListScreen(
                    onChannelClick = { channel ->
                        currentChannel = channel
                        viewModel.setCurrentlyPlayingChannel(channel)
                        navController.navigate("player")
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    },
                    onAddPlaylistClick = {
                        navController.navigate("add_playlist")
                    },
                    viewModel = viewModel
                )
            }
            
            // Player Screen
            composable("player") {
                val playerViewModel: PlayerViewModel = hiltViewModel()
                val uiState by playerViewModel.uiState.collectAsStateWithLifecycle()
                
                // Initialize player with current channel
                LaunchedEffect(currentChannel) {
                    currentChannel?.let { channel ->
                        playerViewModel.playChannel(channel, mediaController)
                    }
                }
                
                PlayerScreen(
                    uiState = uiState,
                    isFullscreen = isPlayerFullscreen,
                    onFullscreenToggle = { isPlayerFullscreen = !isPlayerFullscreen },
                    onChannelChange = { direction ->
                        playerViewModel.changeChannel(direction)
                    },
                    onBackClick = {
                        if (isPlayerFullscreen) {
                            isPlayerFullscreen = false
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    },
                    viewModel = playerViewModel
                )
            }
            
            // Settings Screen (placeholder)
            composable("settings") {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            // Add Playlist Screen (placeholder)
            composable("add_playlist") {
                AddPlaylistScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@UnstableApi
@Composable
fun PlayerScreen(
    uiState: PlayerUiState,
    isFullscreen: Boolean,
    onFullscreenToggle: () -> Unit,
    onChannelChange: (Int) -> Unit,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: PlayerViewModel
) {
    val context = LocalContext.current
    
    if (isFullscreen) {
        // Fullscreen player
        PlayerView(
            player = uiState.exoPlayer,
            channel = uiState.currentChannel,
            isFullscreen = true,
            onFullscreenToggle = onFullscreenToggle,
            onChannelChange = onChannelChange,
            onSettingsClick = onSettingsClick,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // Split view: Player + Channel list
        Row(modifier = Modifier.fillMaxSize()) {
            // Player side (2/3 of screen)
            PlayerView(
                player = uiState.exoPlayer,
                channel = uiState.currentChannel,
                isFullscreen = false,
                onFullscreenToggle = onFullscreenToggle,
                onChannelChange = onChannelChange,
                onSettingsClick = onSettingsClick,
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            )
            
            // Channel list side (1/3 of screen)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                val channelListViewModel: ChannelListViewModel = hiltViewModel()
                
                ChannelListScreen(
                    onChannelClick = { channel ->
                        viewModel.playChannel(channel, null) // MediaController handled in ViewModel
                    },
                    onSettingsClick = onSettingsClick,
                    onAddPlaylistClick = { /* Handle add playlist */ },
                    viewModel = channelListViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    // Placeholder settings screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Settings screen implementation coming soon...",
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onBackClick) {
            Text("Back")
        }
    }
}

@Composable
fun AddPlaylistScreen(
    onBackClick: () -> Unit
) {
    // Placeholder add playlist screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Add Playlist",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Add playlist screen implementation coming soon...",
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onBackClick) {
            Text("Back")
        }
    }
}

// BackHandler for handling back button presses
@Composable
fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    val currentOnBack by rememberUpdatedState(onBack)
    val backCallback = remember {
        object : androidx.activity.OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBack()
            }
        }
    }
    
    LaunchedEffect(enabled) {
        backCallback.isEnabled = enabled
    }
    
    val context = LocalContext.current
    LaunchedEffect(backCallback) {
        (context as? ComponentActivity)?.onBackPressedDispatcher?.addCallback(backCallback)
    }
}