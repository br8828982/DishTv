package com.noor.dishtv.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noor.dishtv.data.database.ChannelDao
import com.noor.dishtv.data.model.Channel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChannelListUiState(
    val allChannels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val availableGroups: List<String> = emptyList(),
    val selectedChannel: Channel? = null,
    val currentlyPlayingChannel: Channel? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChannelListViewModel @Inject constructor(
    private val channelDao: ChannelDao
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChannelListUiState())
    val uiState: StateFlow<ChannelListUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedGroup = MutableStateFlow<String?>(null)
    private val _showFavoritesOnly = MutableStateFlow(false)
    
    init {
        loadChannels()
        loadGroups()
    }
    
    private fun loadChannels() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                
                channelDao.getEnabledChannels().collect { channels ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            allChannels = channels,
                            filteredChannels = applyFilters(
                                channels,
                                _searchQuery.value,
                                _selectedGroup.value,
                                _showFavoritesOnly.value
                            ),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load channels: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun loadGroups() {
        viewModelScope.launch {
            try {
                channelDao.getAllGroups().collect { groups ->
                    _uiState.update { it.copy(availableGroups = groups) }
                }
            } catch (e: Exception) {
                // Groups loading failure is not critical
            }
        }
    }
    
    fun filterChannels(
        searchQuery: String,
        selectedGroup: String?,
        showFavoritesOnly: Boolean
    ) {
        _searchQuery.value = searchQuery
        _selectedGroup.value = selectedGroup
        _showFavoritesOnly.value = showFavoritesOnly
        
        val filteredChannels = applyFilters(
            _uiState.value.allChannels,
            searchQuery,
            selectedGroup,
            showFavoritesOnly
        )
        
        _uiState.update { it.copy(filteredChannels = filteredChannels) }
    }
    
    private fun applyFilters(
        channels: List<Channel>,
        searchQuery: String,
        selectedGroup: String?,
        showFavoritesOnly: Boolean
    ): List<Channel> {
        return channels.filter { channel ->
            // Search filter
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                channel.name.contains(searchQuery, ignoreCase = true) ||
                channel.group?.contains(searchQuery, ignoreCase = true) == true
            }
            
            // Group filter
            val matchesGroup = if (selectedGroup == null) {
                true
            } else {
                channel.group == selectedGroup
            }
            
            // Favorites filter
            val matchesFavorites = if (showFavoritesOnly) {
                channel.isFavorite
            } else {
                true
            }
            
            matchesSearch && matchesGroup && matchesFavorites
        }
    }
    
    fun selectChannel(channel: Channel) {
        _uiState.update { it.copy(selectedChannel = channel) }
    }
    
    fun setCurrentlyPlayingChannel(channel: Channel?) {
        _uiState.update { it.copy(currentlyPlayingChannel = channel) }
    }
    
    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            try {
                val updatedChannel = channel.copy(isFavorite = !channel.isFavorite)
                channelDao.updateChannel(updatedChannel)
                
                // Update the local state immediately for better UX
                val updatedChannels = _uiState.value.allChannels.map { 
                    if (it.id == channel.id) updatedChannel else it 
                }
                
                val filteredChannels = applyFilters(
                    updatedChannels,
                    _searchQuery.value,
                    _selectedGroup.value,
                    _showFavoritesOnly.value
                )
                
                _uiState.update { 
                    it.copy(
                        allChannels = updatedChannels,
                        filteredChannels = filteredChannels
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to update favorite: ${e.message}")
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun refreshChannels() {
        loadChannels()
    }
}