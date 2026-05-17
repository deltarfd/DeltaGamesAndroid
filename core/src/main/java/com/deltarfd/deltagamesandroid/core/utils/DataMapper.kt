package com.deltarfd.deltagamesandroid.core.utils

import com.deltarfd.deltagamesandroid.core.data.local.entity.GameEntity
import com.deltarfd.deltagamesandroid.core.data.remote.response.GameDetailResponse
import com.deltarfd.deltagamesandroid.core.data.remote.response.GameResponse
import com.deltarfd.deltagamesandroid.core.domain.model.Game

object DataMapper {

    fun mapResponseToEntities(input: List<GameResponse>): List<GameEntity> {
        return input.map { response ->
            GameEntity(
                id = response.id,
                slug = response.slug,
                name = response.name,
                released = response.released,
                backgroundImage = response.backgroundImage,
                rating = response.rating,
                ratingsCount = response.ratingsCount,
                metacritic = response.metacritic,
                playtime = response.playtime,
                description = null,
                genres = response.genres?.joinToString(", ") { it.name },
                platforms = response.platforms?.joinToString(", ") { it.platform.name },
                developers = null,
                publishers = null,
                isFavorite = false
            )
        }
    }

    fun mapDetailResponseToEntity(response: GameDetailResponse, isFavorite: Boolean = false): GameEntity {
        return GameEntity(
            id = response.id,
            slug = response.slug,
            name = response.name,
            released = response.released,
            backgroundImage = response.backgroundImage,
            rating = response.rating,
            ratingsCount = response.ratingsCount,
            metacritic = response.metacritic,
            playtime = response.playtime,
            description = response.descriptionRaw,
            genres = response.genres?.joinToString(", ") { it.name },
            platforms = response.platforms?.joinToString(", ") { it.platform.name },
            developers = response.developers?.joinToString(", ") { it.name },
            publishers = response.publishers?.joinToString(", ") { it.name },
            isFavorite = isFavorite
        )
    }

    fun mapEntitiesToDomain(input: List<GameEntity>): List<Game> =
        input.map { mapEntityToDomain(it) }

    fun mapEntityToDomain(entity: GameEntity): Game = Game(
        id = entity.id,
        slug = entity.slug,
        name = entity.name,
        released = entity.released ?: "N/A",
        backgroundImage = entity.backgroundImage ?: "",
        rating = entity.rating,
        ratingsCount = entity.ratingsCount,
        metacritic = entity.metacritic ?: 0,
        playtime = entity.playtime,
        description = entity.description ?: "",
        genres = entity.genres ?: "",
        platforms = entity.platforms ?: "",
        developers = entity.developers ?: "",
        publishers = entity.publishers ?: "",
        isFavorite = entity.isFavorite
    )

    fun mapDomainToEntity(domain: Game): GameEntity = GameEntity(
        id = domain.id,
        slug = domain.slug,
        name = domain.name,
        released = domain.released,
        backgroundImage = domain.backgroundImage,
        rating = domain.rating,
        ratingsCount = domain.ratingsCount,
        metacritic = domain.metacritic,
        playtime = domain.playtime,
        description = domain.description,
        genres = domain.genres,
        platforms = domain.platforms,
        developers = domain.developers,
        publishers = domain.publishers,
        isFavorite = domain.isFavorite
    )
}
