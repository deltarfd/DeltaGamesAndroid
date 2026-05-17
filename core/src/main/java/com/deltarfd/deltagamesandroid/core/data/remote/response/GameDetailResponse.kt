package com.deltarfd.deltagamesandroid.core.data.remote.response

import com.google.gson.annotations.SerializedName

data class GameDetailResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("slug") val slug: String,
    @SerializedName("name") val name: String,
    @SerializedName("released") val released: String?,
    @SerializedName("background_image") val backgroundImage: String?,
    @SerializedName("rating") val rating: Double,
    @SerializedName("ratings_count") val ratingsCount: Int,
    @SerializedName("metacritic") val metacritic: Int?,
    @SerializedName("playtime") val playtime: Int,
    @SerializedName("description_raw") val descriptionRaw: String?,
    @SerializedName("website") val website: String?,
    @SerializedName("platforms") val platforms: List<PlatformWrapper>?,
    @SerializedName("genres") val genres: List<GenreResponse>?,
    @SerializedName("tags") val tags: List<TagResponse>?,
    @SerializedName("developers") val developers: List<DeveloperResponse>?,
    @SerializedName("publishers") val publishers: List<PublisherResponse>?
)

data class DeveloperResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class PublisherResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)
