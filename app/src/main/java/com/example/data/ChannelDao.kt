package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM playlist_items ORDER BY name ASC")
    fun getAllItems(): Flow<List<PlaylistItem>>

    @Query("SELECT * FROM playlist_items WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteItems(): Flow<List<PlaylistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PlaylistItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PlaylistItem>)

    @Update
    suspend fun updateItem(item: PlaylistItem)

    @Query("UPDATE playlist_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

    @Delete
    suspend fun deleteItem(item: PlaylistItem)

    @Query("DELETE FROM playlist_items WHERE playlistName = :playlistName")
    suspend fun deletePlaylist(playlistName: String)

    @Query("DELETE FROM playlist_items")
    suspend fun clearAll()

    @Query("SELECT DISTINCT playlistName FROM playlist_items")
    fun getPlaylistNames(): Flow<List<String>>
}
