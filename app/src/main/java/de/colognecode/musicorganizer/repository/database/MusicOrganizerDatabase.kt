package de.colognecode.musicorganizer.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.colognecode.musicorganizer.repository.database.daos.FavoriteAlbumsDao
import de.colognecode.musicorganizer.repository.database.entities.DataConverter
import de.colognecode.musicorganizer.repository.database.entities.FavoriteAlbum
import de.colognecode.musicorganizer.repository.database.entities.FavoriteAlbumDetails

@Database(entities = [FavoriteAlbum::class, FavoriteAlbumDetails::class], version = 1)
@TypeConverters(DataConverter::class)
abstract class MusicOrganizerDatabase : RoomDatabase() {
    abstract fun favoriteAlbumsDao(): FavoriteAlbumsDao
}
