package de.colognecode.musicorganizer.repository.network.model

import com.google.gson.annotations.SerializedName

data class TopAlbumsResponse(

    @field:SerializedName("topalbums")
    val topAlbums: TopAlbums
)

data class Artist(

    @field:SerializedName("mbid")
    val mbid: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("url")
    val url: String
)

data class TopAlbums(

    @field:SerializedName("@attr")
    val topAlbumAttr: TopAlbumAttr,

    @field:SerializedName("album")
    val album: List<AlbumItem>
)

data class TopAlbumAttr(

    @field:SerializedName("total")
    val total: String,

    @field:SerializedName("perPage")
    val perPage: String,

    @field:SerializedName("artist")
    val artist: String,

    @field:SerializedName("totalPages")
    val totalPages: String,

    @field:SerializedName("page")
    val page: String
)

data class AlbumItem(

    @field:SerializedName("image")
    val topAlbumsImage: List<TopAlbumsImageItem>,

    @field:SerializedName("artist")
    val artist: Artist,

    @field:SerializedName("playcount")
    val playcount: Int,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("url")
    val url: String,

    @field:SerializedName("mbid")
    val mbid: String
)

data class TopAlbumsImageItem(

    @field:SerializedName("#text")
    val text: String,

    @field:SerializedName("size")
    val size: String
)
