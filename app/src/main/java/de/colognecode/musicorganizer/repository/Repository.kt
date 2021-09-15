package de.colognecode.musicorganizer.repository

import de.colognecode.musicorganizer.repository.network.LastFMApiService
import de.colognecode.musicorganizer.repository.network.model.Artistmatches
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class Repository @Inject constructor(
    private val lastFMApiService: LastFMApiService,
    private val apiKey: String,
    private val format: String,
    private val dispatcher: CoroutineDispatcher
) {

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
            .catch { throwable ->
                emit(Result.failure(throwable))
            }
    }
}
