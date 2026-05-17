package com.deltarfd.deltagamesandroid.core.domain.usecase

import com.deltarfd.deltagamesandroid.core.domain.model.Game
import com.deltarfd.deltagamesandroid.core.utils.Resource
import kotlinx.coroutines.flow.Flow

interface IGameUseCase {
    fun getAllGames(page: Int = 1): Flow<Resource<List<Game>>>
    fun getTrendingGames(): Flow<Resource<List<Game>>>
    fun getGameDetail(id: Int): Flow<Resource<Game>>
    fun getFavoriteGames(): Flow<List<Game>>
    fun searchGames(query: String, page: Int = 1): Flow<Resource<List<Game>>>
    suspend fun setFavoriteGame(game: Game, isFavorite: Boolean)
}
