package de.colognecode.musicorganizer.repository.network

import de.colognecode.musicorganizer.repository.network.model.ArtistSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LastFMApiService {
    // TODO: 14.09.21 implement get from artist search 
    @GET
    suspend fun getArtists(
        @Query("method") method: String,
        @Query("artist") artist: String,
        @Query("api_key") apiKey: String,
        @Query("format") format: String
    ): ArtistSearchResponse
}
