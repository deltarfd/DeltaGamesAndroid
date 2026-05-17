package com.deltarfd.deltagamesandroid.presentation.mapper

import com.deltarfd.deltagamesandroid.core.domain.model.Game
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import java.util.Locale

object GamePresentationMapper {

    fun mapDomainToPresentation(game: Game): GameItem = GameItem(
        id = game.id,
        name = game.name,
        slug = game.slug,
        releaseDate = game.released,
        coverUrl = game.backgroundImage,
        rating = game.rating,
        ratingDisplay = String.format(Locale.getDefault(), "%.2f/5", game.rating),
        genres = game.genres,
        platforms = game.platforms,
        playtime = game.playtime,
        metacritic = game.metacritic,
        description = game.description,
        developers = game.developers,
        publishers = game.publishers,
        isFavorite = game.isFavorite
    )

    fun mapListDomainToPresentation(games: List<Game>): List<GameItem> =
        games.map { mapDomainToPresentation(it) }

    fun mapPresentationToDomain(item: GameItem): Game = Game(
        id = item.id,
        name = item.name,
        slug = item.slug,
        released = item.releaseDate,
        backgroundImage = item.coverUrl,
        rating = item.rating,
        ratingsCount = 0,
        metacritic = item.metacritic,
        playtime = item.playtime,
        description = item.description,
        genres = item.genres,
        platforms = item.platforms,
        developers = item.developers,
        publishers = item.publishers,
        isFavorite = item.isFavorite
    )
}
