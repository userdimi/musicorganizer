package de.colognecode.musicorganizer.repository.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoriteAlbumTrackItem(

    @PrimaryKey(autoGenerate = true) val trackId: Int = 0,
    @ColumnInfo(name = "position") val position: Int,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "duration") val duration: Long

)

