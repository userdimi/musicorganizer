package de.colognecode.musicorganizer.repository

import retrofit2.http.GET

interface LastFMApi {
    // TODO: 14.09.21 implement get from artist search 
    @GET
    fun getArtists()
}
