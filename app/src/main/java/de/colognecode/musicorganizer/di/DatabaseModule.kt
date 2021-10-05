package de.colognecode.musicorganizer.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.colognecode.musicorganizer.repository.database.MusicOrganizerDatabase
import de.colognecode.musicorganizer.repository.database.daos.FavoriteAlbumsDao

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    fun providesMusicOrganizerDatabase(application: Application): MusicOrganizerDatabase {
        return Room.databaseBuilder(
            application,
            MusicOrganizerDatabase::class.java, "music_organizer"
        ).build()
    }

    @Provides
    fun providesFavoriteAlbumsDao(database: MusicOrganizerDatabase): FavoriteAlbumsDao {
        return database.favoriteAlbumsDao()
    }
}
