package com.deltarfd.deltagamesandroid.core.data.remote.response

import com.google.gson.annotations.SerializedName

data class GameListResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("results") val results: List<GameResponse>
)

data class GameResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("slug") val slug: String,
    @SerializedName("name") val name: String,
    @SerializedName("released") val released: String?,
    @SerializedName("background_image") val backgroundImage: String?,
    @SerializedName("rating") val rating: Double,
    @SerializedName("ratings_count") val ratingsCount: Int,
    @SerializedName("metacritic") val metacritic: Int?,
    @SerializedName("playtime") val playtime: Int,
    @SerializedName("platforms") val platforms: List<PlatformWrapper>?,
    @SerializedName("genres") val genres: List<GenreResponse>?,
    @SerializedName("tags") val tags: List<TagResponse>?,
    @SerializedName("short_screenshots") val screenshots: List<ScreenshotResponse>?
)

data class PlatformWrapper(
    @SerializedName("platform") val platform: PlatformResponse
)

data class PlatformResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String
)

data class GenreResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("slug") val slug: String
)

data class TagResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class ScreenshotResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("image") val image: String
)
