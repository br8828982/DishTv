package com.noor.dishtv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.noor.dishtv.data.model.Channel
import com.noor.dishtv.ui.components.ChannelCard
import com.noor.dishtv.ui.viewmodel.ChannelListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(
    onChannelClick: (Channel) -> Unit,
    onSettingsClick: () -> Unit,
    onAddPlaylistClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChannelListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<String?>(null) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    
    // Apply filters when state changes
    LaunchedEffect(searchQuery, selectedGroup, showFavoritesOnly) {
        viewModel.filterChannels(searchQuery, selectedGroup, showFavoritesOnly)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar with large elements for elderly users
        TopAppBar(
            title = {
                Text(
                    text = "IPTV Channels",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp, // Large font for elderly users
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            actions = {
                // Favorites Filter Button
                IconButton(
                    onClick = { 
                        showFavoritesOnly = !showFavoritesOnly
                        viewModel.filterChannels(searchQuery, selectedGroup, showFavoritesOnly)
                    },
                    modifier = Modifier.size(56.dp) // Large button
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Show Favorites",
                        tint = if (showFavoritesOnly) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Filter Button
                IconButton(
                    onClick = { showFilters = !showFilters },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter Channels",
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Add Playlist Button
                IconButton(
                    onClick = onAddPlaylistClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Playlist",
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Settings Button
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        
        // Search Bar - Large for elderly users
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { 
                Text(
                    "Search channels...",
                    fontSize = 18.sp // Large font
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(28.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )
        
        // Filter Options
        if (showFilters) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Filter by Group",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    // Group filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                onClick = { selectedGroup = null },
                                label = { 
                                    Text(
                                        "All Groups",
                                        fontSize = 16.sp
                                    ) 
                                },
                                selected = selectedGroup == null,
                                modifier = Modifier.height(48.dp) // Large chips
                            )
                        }
                        
                        items(uiState.availableGroups) { group ->
                            FilterChip(
                                onClick = { selectedGroup = group },
                                label = { 
                                    Text(
                                        group,
                                        fontSize = 16.sp
                                    ) 
                                },
                                selected = selectedGroup == group,
                                modifier = Modifier.height(48.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Channel Count and Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${uiState.filteredChannels.size} channels",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (uiState.isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Channel List
        if (uiState.filteredChannels.isEmpty() && !uiState.isLoading) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty() || selectedGroup != null || showFavoritesOnly) {
                            "No channels match your filters"
                        } else {
                            "No channels available\nTap + to add a playlist"
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp // Large font for elderly users
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    if (uiState.filteredChannels.isEmpty() && uiState.allChannels.isEmpty()) {
                        Button(
                            onClick = onAddPlaylistClick,
                            modifier = Modifier
                                .height(56.dp)
                                .padding(horizontal = 32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Playlist",
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing for easier navigation
            ) {
                items(
                    items = uiState.filteredChannels,
                    key = { it.id }
                ) { channel ->
                    ChannelCard(
                        channel = channel,
                        isSelected = uiState.selectedChannel?.id == channel.id,
                        isPlaying = uiState.currentlyPlayingChannel?.id == channel.id,
                        onChannelClick = {
                            viewModel.selectChannel(it)
                            onChannelClick(it)
                        },
                        onFavoriteClick = { viewModel.toggleFavorite(it) }
                    )
                }
                
                // Bottom padding for navigation
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        
        // Error handling
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show error snackbar or dialog
            }
        }
    }
}