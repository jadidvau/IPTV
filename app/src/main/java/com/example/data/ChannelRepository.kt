package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ChannelRepository(private val channelDao: ChannelDao) {

    val allChannels: Flow<List<PlaylistItem>> = channelDao.getAllItems()
    val favoriteChannels: Flow<List<PlaylistItem>> = channelDao.getFavoriteItems()
    val playlistNames: Flow<List<String>> = channelDao.getPlaylistNames()

    suspend fun insertChannel(item: PlaylistItem) = withContext(Dispatchers.IO) {
        channelDao.insertItem(item)
    }

    suspend fun updateFavorite(id: Int, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        channelDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun updateChannel(item: PlaylistItem) = withContext(Dispatchers.IO) {
        channelDao.updateItem(item)
    }

    suspend fun deleteChannel(item: PlaylistItem) = withContext(Dispatchers.IO) {
        channelDao.deleteItem(item)
    }

    suspend fun deletePlaylist(playlistName: String) = withContext(Dispatchers.IO) {
        channelDao.deletePlaylist(playlistName)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        channelDao.clearAll()
    }

    // Standard pre-populating with reliable benchmark HLS streams
    suspend fun initializeDefaultChannelsIfEmpty() = withContext(Dispatchers.IO) {
        val current = allChannels.first()
        if (current.isEmpty()) {
            val defaults = listOf(
                PlaylistItem(
                    name = "Al Jazeera English News",
                    url = "https://live-aljazeera.akamaized.net/hls/live/2004561/AJE/index.m3u8",
                    category = "News",
                    logoUrl = "https://www.aljazeera.com/wp-content/themes/aje-theme/assets/images/aje-logo.png",
                    playlistName = "Default"
                ),
                PlaylistItem(
                    name = "Global News Feed HD",
                    url = "https://content.jwplatform.com/manifests/vM7nH0Kl.m3u8",
                    category = "News",
                    logoUrl = null,
                    playlistName = "Default"
                ),
                PlaylistItem(
                    name = "Mux Live Demo Stream",
                    url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                    category = "Sports",
                    logoUrl = null,
                    playlistName = "Default"
                ),
                PlaylistItem(
                    name = "Sintel HD VOD",
                    url = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
                    category = "Movies",
                    logoUrl = null,
                    playlistName = "Default"
                ),
                PlaylistItem(
                    name = "Big Buck Bunny Live Feed",
                    url = "https://test-streams.mux.dev/pts_live/playlist.m3u8",
                    category = "Entertainment",
                    logoUrl = null,
                    playlistName = "Default"
                ),
                PlaylistItem(
                    name = "NASA Earth & Cosmic Stream",
                    url = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
                    category = "Science",
                    logoUrl = null,
                    playlistName = "Default"
                ),
                PlaylistItem(
                    name = "Tears of Steel movie loop",
                    url = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                    category = "Movies",
                    logoUrl = null,
                    playlistName = "Default"
                )
            )
            channelDao.insertItems(defaults)
        }
    }

    /**
     * Downloads an M3U file from a URL and parses its channels, saving them to the database.
     */
    suspend fun importM3UFromUrl(playlistUrlString: String, customName: String) = withContext(Dispatchers.IO) {
        val parsedChannels = mutableListOf<PlaylistItem>()
        var connection: HttpURLConnection? = null
        try {
            val url = URL(playlistUrlString)
            connection = url.openConnection() as HttpURLConnection
            connection.readTimeout = 10000
            connection.connectTimeout = 10000
            connection.requestMethod = "GET"
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                
                var currentName = ""
                var currentLogoUrl: String? = null
                var currentCategory = "General"
                
                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line!!.trim()
                    if (trimmed.startsWith("#EXTM3U")) {
                        continue
                    }
                    if (trimmed.startsWith("#EXTINF:")) {
                        // Parse name and metadata from #EXTINF
                        // Extinf format: #EXTINF:-1 tvg-logo="logo.png" group-title="News",Channel name
                        currentName = trimmed.substringAfterLast(",", "Unnamed Channel").trim()
                        
                        currentLogoUrl = null
                        if (trimmed.contains("tvg-logo=")) {
                            val parts = trimmed.split("tvg-logo=\"")
                            if (parts.size > 1) {
                                currentLogoUrl = parts[1].substringBefore("\"")
                            }
                        }
                        
                        currentCategory = "General"
                        if (trimmed.contains("group-title=")) {
                            val parts = trimmed.split("group-title=\"")
                            if (parts.size > 1) {
                                currentCategory = parts[1].substringBefore("\"")
                            }
                        }
                    } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                        // It is a stream URL!
                        val channelName = if (currentName.isNotEmpty()) currentName else "Channel ${parsedChannels.size + 1}"
                        parsedChannels.add(
                            PlaylistItem(
                                name = channelName,
                                url = trimmed,
                                category = currentCategory,
                                logoUrl = currentLogoUrl,
                                playlistName = customName
                            )
                        )
                        // Reset for next node
                        currentName = ""
                        currentLogoUrl = null
                        currentCategory = "General"
                    }
                }
                
                if (parsedChannels.isNotEmpty()) {
                    // Save to DB
                    channelDao.insertItems(parsedChannels)
                }
            } else {
                throw Exception("HTTP error code: $responseCode")
            }
        } catch (e: Exception) {
            Log.e("ChannelRepository", "Error loading M3U", e)
            throw e
        } finally {
            connection?.disconnect()
        }
        parsedChannels.size
    }
}
