package com.deltarfd.deltagamesandroid.core.domain.usecase

import com.deltarfd.deltagamesandroid.core.domain.model.Game
import com.deltarfd.deltagamesandroid.core.domain.repository.IGameRepository
import com.deltarfd.deltagamesandroid.core.utils.Resource
import kotlinx.coroutines.flow.Flow

class GameInteractor(private val gameRepository: IGameRepository) : IGameUseCase {

    override fun getAllGames(page: Int): Flow<Resource<List<Game>>> =
        gameRepository.getAllGames(page)

    override fun getTrendingGames(): Flow<Resource<List<Game>>> =
        gameRepository.getTrendingGames()

    override fun getGameDetail(id: Int): Flow<Resource<Game>> =
        gameRepository.getGameDetail(id)

    override fun getFavoriteGames(): Flow<List<Game>> =
        gameRepository.getFavoriteGames()

    override fun searchGames(query: String, page: Int): Flow<Resource<List<Game>>> =
        gameRepository.searchGames(query, page)

    override suspend fun setFavoriteGame(game: Game, isFavorite: Boolean) =
        gameRepository.setFavoriteGame(game, isFavorite)
}
