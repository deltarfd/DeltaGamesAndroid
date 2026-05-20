package com.deltarfd.deltagamesandroid.core.data.repository

import com.deltarfd.deltagamesandroid.core.data.local.GameDao
import com.deltarfd.deltagamesandroid.core.data.remote.ApiService
import com.deltarfd.deltagamesandroid.core.domain.model.Game
import com.deltarfd.deltagamesandroid.core.domain.repository.IGameRepository
import com.deltarfd.deltagamesandroid.core.utils.DataMapper
import com.deltarfd.deltagamesandroid.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class GameRepositoryImpl(
    private val apiService: ApiService,
    private val gameDao: GameDao
) : IGameRepository {

    companion object {
        private const val DEFAULT_ERROR = "Unknown error"
    }

    override fun getAllGames(page: Int): Flow<Resource<List<Game>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getGames(page = page)
            val entities = DataMapper.mapResponseToEntities(response.results)
            if (page == 1) gameDao.upsertGamesPreservingFavorites(entities)
            val games = DataMapper.mapEntitiesToDomain(entities)
            emit(Resource.Success(games))
        } catch (e: Exception) {
            if (page == 1) {
                gameDao.getAllGames().collect { localData ->
                    if (localData.isNotEmpty()) {
                        emit(Resource.Success(DataMapper.mapEntitiesToDomain(localData)))
                    } else {
                        emit(Resource.Error(e.message ?: DEFAULT_ERROR))
                    }
                }
            } else {
                emit(Resource.Error(e.message ?: DEFAULT_ERROR))
            }
        }
    }

    override fun getTrendingGames(): Flow<Resource<List<Game>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTrendingGames()
            val entities = DataMapper.mapResponseToEntities(response.results)
            val games = DataMapper.mapEntitiesToDomain(entities)
            emit(Resource.Success(games))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: DEFAULT_ERROR))
        }
    }

    override fun getGameDetail(id: Int): Flow<Resource<Game>> = flow {
        emit(Resource.Loading())
        try {
            val localGame = gameDao.getGameById(id)
            localGame.collect { entity ->
                val isFavorite = entity?.isFavorite ?: false
                try {
                    val response = apiService.getGameDetail(id)
                    val updatedEntity = DataMapper.mapDetailResponseToEntity(response, isFavorite)
                    gameDao.insertGame(updatedEntity)
                    emit(Resource.Success(DataMapper.mapEntityToDomain(updatedEntity)))
                } catch (e: Exception) {
                    if (entity != null) {
                        emit(Resource.Success(DataMapper.mapEntityToDomain(entity)))
                    } else {
                        emit(Resource.Error(e.message ?: DEFAULT_ERROR))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: DEFAULT_ERROR))
        }
    }

    override fun getFavoriteGames(): Flow<List<Game>> =
        gameDao.getFavoriteGames().map { entities ->
            DataMapper.mapEntitiesToDomain(entities)
        }

    override fun searchGames(query: String, page: Int): Flow<Resource<List<Game>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.searchGames(query = query, page = page)
            val entities = DataMapper.mapResponseToEntities(response.results)
            val games = DataMapper.mapEntitiesToDomain(entities)
            emit(Resource.Success(games))
        } catch (e: Exception) {
            if (page == 1) {
                gameDao.searchGames(query).collect { localData ->
                    if (localData.isNotEmpty()) {
                        emit(Resource.Success(DataMapper.mapEntitiesToDomain(localData)))
                    } else {
                        emit(Resource.Error(e.message ?: DEFAULT_ERROR))
                    }
                }
            } else {
                emit(Resource.Error(e.message ?: DEFAULT_ERROR))
            }
        }
    }

    override suspend fun setFavoriteGame(game: Game, isFavorite: Boolean) {
        gameDao.setFavorite(game.id, isFavorite)
    }
}
