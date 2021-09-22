package de.colognecode.musicorganizer.repository

import de.colognecode.musicorganizer.di.DispatcherModule
import de.colognecode.musicorganizer.repository.network.LastFMApiService
import de.colognecode.musicorganizer.repository.network.model.Artistmatches
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class Repository @Inject constructor(
    private val lastFMApiService: LastFMApiService,
    @DispatcherModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    companion object {
        const val DELAY_ONE_SECOND = 1000L
        private const val SEARCH_METHOD = "artist.search"
    }

    suspend fun getArtistsSearchResult(
        artist: String
    ): Flow<Artistmatches> {
        return flow {
            val artistsSearchResults = lastFMApiService.getArtists(
                SEARCH_METHOD, artist
            )
            emit(Result.success(artistsSearchResults.results?.artistmatches))
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
}
