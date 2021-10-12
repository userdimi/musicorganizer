package de.colognecode.musicorganizer.repository.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.colognecode.musicorganizer.repository.database.entities.FavoriteAlbum
import de.colognecode.musicorganizer.repository.database.entities.FavoriteAlbumDetails

@Dao
interface FavoriteAlbumsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavoriteAlbum(
        favoriteAlbum: FavoriteAlbum
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveFavoriteAlbumDetails(
        favoriteAlbumDetails: FavoriteAlbumDetails
    )

    @Query("SELECT * FROM FavoriteAlbum")
    suspend fun getAllFavoriteAlbums(): List<FavoriteAlbum>

    @Query("SELECT * FROM FavoriteAlbumDetails WHERE mbid=:mbid")
    suspend fun getFavoriteAlbumDetailsByMbid(mbid: String): FavoriteAlbumDetails
}
