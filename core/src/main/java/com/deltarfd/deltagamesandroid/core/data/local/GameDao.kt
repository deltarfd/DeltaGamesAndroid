package com.deltarfd.deltagamesandroid.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.deltarfd.deltagamesandroid.core.data.local.entity.GameEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

@Dao
interface GameDao {

    @Query("SELECT * FROM game ORDER BY rating DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM game WHERE is_favorite = 1")
    fun getFavoriteGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM game WHERE id = :id")
    fun getGameById(id: Int): Flow<GameEntity?>

    @Query("SELECT is_favorite FROM game WHERE id = :id")
    suspend fun isFavorite(id: Int): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Update
    suspend fun updateGame(game: GameEntity)

    @Query("UPDATE game SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Int, isFavorite: Boolean)

    @Query("SELECT * FROM game WHERE name LIKE '%' || :query || '%' ORDER BY rating DESC")
    fun searchGames(query: String): Flow<List<GameEntity>>

    /**
     * Upserts games from the API without overwriting the is_favorite column.
     * Preserves existing favorite status for games already in the database.
     */
    @Transaction
    suspend fun upsertGamesPreservingFavorites(games: List<GameEntity>) {
        games.forEach { game ->
            val currentFavorite = isFavorite(game.id) ?: false
            insertGame(game.copy(isFavorite = currentFavorite))
        }
    }
}
