package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChannelRepository
import com.example.data.PlaylistItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ImportState {
    object Idle : ImportState
    object Loading : ImportState
    data class Success(val count: Int) : ImportState
    data class Error(val message: String) : ImportState
}

class IPTVViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChannelRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ChannelRepository(database.channelDao())
        
        // Populate default channels if database is completely empty on startup
        viewModelScope.launch {
            repository.initializeDefaultChannelsIfEmpty()
        }
    }

    // Exposed flows from room database
    val allChannels = repository.allChannels
    val favoriteChannels = repository.favoriteChannels
    val playlistNames = repository.playlistNames

    // Local user filters state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedPlaylistFilter = MutableStateFlow("All")
    val selectedPlaylistFilter = _selectedPlaylistFilter.asStateFlow()

    private val _selectedChannel = MutableStateFlow<PlaylistItem?>(null)
    val selectedChannel = _selectedChannel.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState = _importState.asStateFlow()

    // Filtered list flow combining DB stream and filters
    val filteredChannels: StateFlow<List<PlaylistItem>> = combine(
        allChannels,
        _searchQuery,
        _selectedCategory,
        _selectedPlaylistFilter
    ) { channels, query, category, plFilter ->
        channels.filter { channel ->
            val matchesQuery = channel.name.contains(query, ignoreCase = true) || 
                               channel.category.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || channel.category == category
            val matchesPlaylist = plFilter == "All" || channel.playlistName == plFilter
            
            matchesQuery && matchesCategory && matchesPlaylist
        }
    } // Set initial success after database auto-fills
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Extracted categories from channels
    val availableCategories: StateFlow<List<String>> = allChannels.combine(_selectedPlaylistFilter) { channels, plFilter ->
        val categories = channels
            .filter { plFilter == "All" || it.playlistName == plFilter }
            .map { it.category }
            .distinct()
            .sorted()
        listOf("All") + categories
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("All")
    )

    fun selectChannel(item: PlaylistItem?) {
        _selectedChannel.value = item
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectPlaylistFilter(playlistName: String) {
        _selectedPlaylistFilter.value = playlistName
        // Reset category to "All" to prevent filters mismatch when switching playlists
        _selectedCategory.value = "All"
    }

    fun toggleFavorite(channel: PlaylistItem) {
        viewModelScope.launch {
            repository.updateFavorite(channel.id, !channel.isFavorite)
            
            // Sync current active item if it changed
            if (_selectedChannel.value?.id == channel.id) {
                _selectedChannel.value = channel.copy(isFavorite = !channel.isFavorite)
            }
        }
    }

    fun deleteChannel(channel: PlaylistItem) {
        viewModelScope.launch {
            repository.deleteChannel(channel)
            if (_selectedChannel.value?.id == channel.id) {
                _selectedChannel.value = null
            }
        }
    }

    fun deletePlaylist(playlistName: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistName)
            if (_selectedChannel.value?.playlistName == playlistName) {
                _selectedChannel.value = null
            }
        }
    }

    /**
     * Add simple single channel directly
     */
    fun addNewChannel(
        name: String,
        url: String,
        category: String,
        logoUrl: String?,
        playlistName: String
    ) {
        viewModelScope.launch {
            val formattedCategory = if (category.isBlank()) "General" else category.trim()
            val finalPlaylistName = if (playlistName.isBlank()) "My Channels" else playlistName.trim()
            val formatLogo = if (logoUrl?.isBlank() == true) null else logoUrl?.trim()
            
            val item = PlaylistItem(
                name = name.trim(),
                url = url.trim(),
                category = formattedCategory,
                logoUrl = formatLogo,
                playlistName = finalPlaylistName
            )
            repository.insertChannel(item)
        }
    }

    /**
     * Parse and import standard M3U lists over network
     */
    fun importM3U(url: String, playlistName: String) {
        if (url.isBlank() || playlistName.isBlank()) {
            _importState.value = ImportState.Error("URL and Playlist Name cannot be blank")
            return
        }
        
        viewModelScope.launch {
            _importState.value = ImportState.Loading
            try {
                val count = repository.importM3UFromUrl(url.trim(), playlistName.trim())
                _importState.value = ImportState.Success(count)
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.localizedMessage ?: "Failed to import M3U file")
            }
        }
    }

    fun clearImportStatus() {
        _importState.value = ImportState.Idle
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.clearAll()
            _selectedChannel.value = null
            _selectedCategory.value = "All"
            _selectedPlaylistFilter.value = "All"
            _searchQuery.value = ""
            repository.initializeDefaultChannelsIfEmpty()
        }
    }

    // Factory Class pattern
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(IPTVViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return IPTVViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
