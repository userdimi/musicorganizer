package de.colognecode.musicorganizer.repository

import de.colognecode.musicorganizer.repository.network.LastFMApiService
import de.colognecode.musicorganizer.repository.network.model.Artistmatches
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class Repository @Inject constructor(
    private val lastFMApiService: LastFMApiService,
    private val apiKey: String,
    private val format: String,
    private val dispatcher: CoroutineDispatcher
) {

    companion object {
        const val DELAY_ONE_SECOND = 1000L
    }

    suspend fun getArtistsSearchResult(
        method: String,
        artist: String
    ): Flow<Result<Artistmatches?>> {
        return flow {
            val artistsSearchResults = lastFMApiService.getArtists(
                method, artist, apiKey, format
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
            .flowOn(dispatcher)
    }
}
