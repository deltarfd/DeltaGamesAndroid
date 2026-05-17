package com.deltarfd.deltagamesandroid.core.domain.model

data class Game(
    val id: Int,
    val slug: String,
    val name: String,
    val released: String,
    val backgroundImage: String,
    val rating: Double,
    val ratingsCount: Int,
    val metacritic: Int,
    val playtime: Int,
    val description: String,
    val genres: String,
    val platforms: String,
    val developers: String,
    val publishers: String,
    val isFavorite: Boolean
)
