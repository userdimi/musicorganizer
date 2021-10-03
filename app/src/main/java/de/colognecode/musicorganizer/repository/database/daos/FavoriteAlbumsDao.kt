package de.colognecode.musicorganizer.repository.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.colognecode.musicorganizer.repository.database.entities.FavoriteAlbum

@Dao
interface FavoriteAlbumsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavoriteAlbum(favoriteAlbum: FavoriteAlbum)

    @Query("SELECT * FROM FavoriteAlbum")
    suspend fun getAllFavoriteAlbums(): List<FavoriteAlbum>
}
