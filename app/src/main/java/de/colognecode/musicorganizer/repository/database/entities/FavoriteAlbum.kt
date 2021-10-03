package de.colognecode.musicorganizer.repository.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoriteAlbum(
    @PrimaryKey val mbid: String,
    @ColumnInfo(name = "album_image_url") val albumImageUrl: String,
    @ColumnInfo(name = "album_name") val albumName: String,
    @ColumnInfo(name = "artist_name") val artistName: String,
    @ColumnInfo(name = "play_count") val playCount: Int
)
