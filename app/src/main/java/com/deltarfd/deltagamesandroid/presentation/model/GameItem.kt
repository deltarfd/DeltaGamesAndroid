package com.deltarfd.deltagamesandroid.presentation.model

data class GameItem(
    val id: Int,
    val name: String,
    val slug: String,
    val releaseDate: String,
    val coverUrl: String,
    val rating: Double,
    val ratingDisplay: String,
    val genres: String,
    val platforms: String,
    val playtime: Int,
    val metacritic: Int,
    val description: String,
    val developers: String,
    val publishers: String,
    val isFavorite: Boolean
)
