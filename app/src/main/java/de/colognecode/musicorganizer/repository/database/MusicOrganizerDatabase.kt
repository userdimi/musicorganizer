package de.colognecode.musicorganizer.repository.database

import androidx.room.Database
import androidx.room.RoomDatabase
import de.colognecode.musicorganizer.repository.database.daos.FavoriteAlbumsDao
import de.colognecode.musicorganizer.repository.database.entities.FavoriteAlbum

@Database(entities = [FavoriteAlbum::class], version = 1)
abstract class MusicOrganizerDatabase : RoomDatabase() {
    abstract fun topAlbumsDao(): FavoriteAlbumsDao
}
