package de.colognecode.musicorganizer.repository.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.colognecode.musicorganizer.repository.network.model.TrackItem

@Entity
data class FavoriteAlbumDetails(
    @PrimaryKey val mbid: String,
    @ColumnInfo(name = "album_image_url") val albumImageUrl: String?,
    @ColumnInfo(name = "album_name") val albumName: String?,
    @ColumnInfo(name = "artist_name") val artistName: String?,
    @ColumnInfo(name = "total_tracks") val totalTracks: Int?,
    @ColumnInfo(name = "total_duration") val totalDuration: Long?,
    @ColumnInfo(name = "tracks") val tracks: List<TrackItem>?
)
