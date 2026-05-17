package com.deltarfd.deltagamesandroid.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game")
data class GameEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "slug")
    val slug: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "released")
    val released: String?,

    @ColumnInfo(name = "background_image")
    val backgroundImage: String?,

    @ColumnInfo(name = "rating")
    val rating: Double,

    @ColumnInfo(name = "ratings_count")
    val ratingsCount: Int,

    @ColumnInfo(name = "metacritic")
    val metacritic: Int?,

    @ColumnInfo(name = "playtime")
    val playtime: Int,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "genres")
    val genres: String?,         // Comma-separated genre names

    @ColumnInfo(name = "platforms")
    val platforms: String?,      // Comma-separated platform names

    @ColumnInfo(name = "developers")
    val developers: String?,

    @ColumnInfo(name = "publishers")
    val publishers: String?,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
)
