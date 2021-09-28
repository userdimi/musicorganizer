package de.colognecode.musicorganizer.repository.network.model

import com.google.gson.annotations.SerializedName

data class ArtistSearchResponse(

    @field:SerializedName("results")
    val results: Results? = null
)

data class ArtistItem(

    @field:SerializedName("image")
    val image: List<ImageItem?>? = null,

    @field:SerializedName("mbid")
    val mbid: String? = null,

    @field:SerializedName("listeners")
    val listeners: String? = null,

    @field:SerializedName("streamable")
    val streamable: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("url")
    val url: String? = null
)

data class Results(

    @field:SerializedName("opensearch:Query")
    val opensearchQuery: OpensearchQuery? = null,

    @field:SerializedName("@attr")
    val attr: Attr? = null,

    @field:SerializedName("opensearch:itemsPerPage")
    val opensearchItemsPerPage: String? = null,

    @field:SerializedName("artistmatches")
    val artistmatches: Artistmatches? = null,

    @field:SerializedName("opensearch:startIndex")
    val opensearchStartIndex: String? = null,

    @field:SerializedName("opensearch:totalResults")
    val opensearchTotalResults: String? = null
)

data class OpensearchQuery(

    @field:SerializedName("startPage")
    val startPage: String? = null,

    @field:SerializedName("#text")
    val text: String? = null,

    @field:SerializedName("role")
    val role: String? = null,

    @field:SerializedName("searchTerms")
    val searchTerms: String? = null
)

data class Artistmatches(

    @field:SerializedName("artist")
    val artist: List<ArtistItem?>? = null
)

data class ImageItem(

    @field:SerializedName("#text")
    val text: String? = null,

    @field:SerializedName("size")
    val size: String? = null
)

data class Attr(

    @field:SerializedName("for")
    val jsonMemberFor: String? = null
)
