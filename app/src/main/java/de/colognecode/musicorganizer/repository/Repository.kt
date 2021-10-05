package de.colognecode.musicorganizer.repository

import de.colognecode.musicorganizer.di.DispatcherModule
import de.colognecode.musicorganizer.repository.database.daos.FavoriteAlbumsDao
import de.colognecode.musicorganizer.repository.database.entities.FavoriteAlbum
import de.colognecode.musicorganizer.repository.network.LastFMApiService
import de.colognecode.musicorganizer.repository.network.model.Artistmatches
import de.colognecode.musicorganizer.repository.network.model.TopAlbums
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class Repository @Inject constructor(
    private val lastFMApiService: LastFMApiService,
    @DispatcherModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val favoriteAlbumsDao: FavoriteAlbumsDao
) {

    companion object {
        const val DELAY_ONE_SECOND = 1000L
        private const val SEARCH_METHOD = "artist.search"
        private const val TOP_ALBUMS_METHOD = "artist.gettopalbums"
    }

    suspend fun getArtistsSearchResult(
        artist: String,
        page: Int
    ): Flow<Artistmatches> {
        return flow {
            val artistsSearchResultsResponse = lastFMApiService.getArtists(
                method = SEARCH_METHOD,
                artist = artist,
                page = page
            )
            emit(Result.success(artistsSearchResultsResponse.results?.artistmatches))
        }
            .retry(2) { throwable ->
                (throwable is Exception).also { isException ->
                    if (isException) delay(DELAY_ONE_SECOND)
                }
            }
            .catch { throwable ->
                emit(Result.failure(throwable))
            }
            .mapNotNull {
                it.getOrNull()
            }
            .flowOn(ioDispatcher)
    }

    suspend fun getTopAlbums(artist: String, page: Int): Flow<TopAlbums?> {
        return flow {
            val topAlbumsResponse = lastFMApiService.getTopAlbums(
                method = TOP_ALBUMS_METHOD,
                artist = artist,
                page = page
            )
            emit(Result.success(topAlbumsResponse.topAlbums))
        }
            .retry(2) { throwable ->
                (throwable is Exception).also { isException ->
                    if (isException) delay(DELAY_ONE_SECOND)
                }
            }
            .catch { throwable ->
                emit(Result.failure(throwable))
            }
            .mapNotNull {
                it.getOrNull()
            }
            .flowOn(ioDispatcher)
    }

    suspend fun saveFavoriteAlbumToDatabase(favoriteAlbum: FavoriteAlbum) {
        favoriteAlbumsDao.saveFavoriteAlbum(favoriteAlbum)
    }
}
