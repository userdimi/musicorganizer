package de.colognecode.musicorganizer.repository.network

import de.colognecode.musicorganizer.BuildConfig
import de.colognecode.musicorganizer.repository.network.model.ArtistSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LastFMApiService {

    companion object {
        private const val API_KEY = BuildConfig.API_KEY
        private const val FORMAT = "json"
    }

    @GET("2.0/")
    suspend fun getArtists(
        @Query("method") method: String,
        @Query("artist") artist: String,
        @Query("api_key") apiKey: String = API_KEY,
        @Query("format") format: String = FORMAT
    ): ArtistSearchResponse
}
