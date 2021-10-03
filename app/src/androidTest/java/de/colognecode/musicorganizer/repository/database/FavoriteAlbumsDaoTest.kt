package de.colognecode.musicorganizer.repository.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.colognecode.musicorganizer.repository.database.daos.FavoriteAlbumsDao
import de.colognecode.musicorganizer.repository.database.entities.FavoriteAlbum
import de.colognecode.musicorganizer.util.InstrumentedCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class FavoriteAlbumsDaoTest {

    @get:Rule
    val rule = InstrumentedCoroutineTestRule()

    private lateinit var favoriteAlbumsDao: FavoriteAlbumsDao
    private lateinit var database: MusicOrganizerDatabase

    private val favoriteFooAlbum = FavoriteAlbum(
        mbid = "12345",
        albumImageUrl = "https://foo-album-image.com",
        albumName = "Foo Album",
        artistName = "Foo Artist",
        playCount = 12345
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, MusicOrganizerDatabase::class.java,
        )
            .setTransactionExecutor(rule.dispatcher.asExecutor())
            .setQueryExecutor(rule.dispatcher.asExecutor())
            .build()
        favoriteAlbumsDao = database.favoriteAlbumsDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        // database.close()
    }

    @Test
    @Throws(Exception::class)
    fun favoritesAlbumsShouldBeSavedAndReadFromDb() = rule.dispatcher.runBlockingTest {
        // act
        favoriteAlbumsDao.saveFavoriteAlbum(favoriteFooAlbum)

        // assert
        val expectedFavoriteAlbums = favoriteAlbumsDao.getAllFavoriteAlbums()

        assertThat(expectedFavoriteAlbums[0], equalTo(favoriteFooAlbum))
    }
}
