package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_items")
data class PlaylistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val category: String,
    val isFavorite: Boolean = false,
    val logoUrl: String? = null,
    val playlistName: String = "Default"
)
