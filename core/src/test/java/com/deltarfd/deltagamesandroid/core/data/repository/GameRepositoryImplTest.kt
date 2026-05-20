package com.deltarfd.deltagamesandroid.core.data.repository

import com.deltarfd.deltagamesandroid.core.data.local.GameDao
import com.deltarfd.deltagamesandroid.core.data.local.entity.GameEntity
import com.deltarfd.deltagamesandroid.core.data.remote.ApiService
import com.deltarfd.deltagamesandroid.core.data.remote.response.GameListResponse
import com.deltarfd.deltagamesandroid.core.data.remote.response.GameResponse
import com.deltarfd.deltagamesandroid.core.domain.model.Game
import com.deltarfd.deltagamesandroid.core.utils.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GameRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var gameDao: GameDao
    private lateinit var repository: GameRepositoryImpl

    // ── test data builders ────────────────────────────────────────────────

    private fun makeGameResponse(id: Int = 1, name: String = "Game $id") = GameResponse(
        id = id, slug = "game-$id", name = name, released = "2024-01-01",
        backgroundImage = "https://img.example.com/$id.jpg", rating = 4.0,
        ratingsCount = 100, metacritic = 80, playtime = 10,
        platforms = null, genres = null, tags = null, screenshots = null
    )

    private fun makeGameEntity(id: Int = 1, name: String = "Game $id", isFavorite: Boolean = false) = GameEntity(
        id = id, slug = "game-$id", name = name, released = "2024-01-01",
        backgroundImage = "https://img.example.com/$id.jpg", rating = 4.0,
        ratingsCount = 100, metacritic = 80, playtime = 10,
        description = null, genres = null, platforms = null,
        developers = null, publishers = null, isFavorite = isFavorite
    )

    private fun makeListResponse(vararg responses: GameResponse) =
        GameListResponse(count = responses.size, next = null, results = responses.toList())

    @Before
    fun setup() {
        apiService = mockk()
        gameDao   = mockk(relaxed = true)
        repository = GameRepositoryImpl(apiService, gameDao)
    }

    // ── getAllGames ────────────────────────────────────────────────────────

    @Test
    fun `getAllGames page 1 emits Loading then Success from API`() = runTest {
        coEvery { apiService.getGames(page = 1) } returns makeListResponse(makeGameResponse())

        val emissions = repository.getAllGames(1).toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)
        assertEquals(1, (emissions[1] as Resource.Success).data?.size)
    }

    @Test
    fun `getAllGames page 1 caches results to Room`() = runTest {
        coEvery { apiService.getGames(page = 1) } returns makeListResponse(makeGameResponse())

        repository.getAllGames(1).toList()

        coVerify { gameDao.upsertGamesPreservingFavorites(any()) }
    }

    @Test
    fun `getAllGames page 2 does NOT cache to Room`() = runTest {
        coEvery { apiService.getGames(page = 2) } returns makeListResponse(makeGameResponse())

        repository.getAllGames(2).toList()

        coVerify(exactly = 0) { gameDao.upsertGamesPreservingFavorites(any()) }
    }

    @Test
    fun `getAllGames page 1 API error falls back to Room cache`() = runTest {
        coEvery { apiService.getGames(page = 1) } throws RuntimeException("No internet")
        every { gameDao.getAllGames() } returns flowOf(listOf(makeGameEntity()))

        val emissions = repository.getAllGames(1).toList()

        val successEmission = emissions.filterIsInstance<Resource.Success<List<Game>>>().firstOrNull()
        assertTrue(successEmission != null)
        assertEquals(1, successEmission?.data?.size)
    }

    @Test
    fun `getAllGames page 1 API error with empty cache emits Error`() = runTest {
        coEvery { apiService.getGames(page = 1) } throws RuntimeException("No internet")
        every { gameDao.getAllGames() } returns flowOf(emptyList())

        val emissions = repository.getAllGames(1).toList()

        val hasError = emissions.any { it is Resource.Error }
        assertTrue(hasError)
    }

    @Test
    fun `getAllGames page 1 API error with empty cache and null message uses default error`() = runTest {
        coEvery { apiService.getGames(page = 1) } throws RuntimeException()
        every { gameDao.getAllGames() } returns flowOf(emptyList())

        val emissions = repository.getAllGames(1).toList()

        val error = emissions.filterIsInstance<Resource.Error<List<Game>>>().first()
        assertEquals("Unknown error", error.message)
    }

    @Test
    fun `getAllGames page 2 API error emits Error directly`() = runTest {
        coEvery { apiService.getGames(page = 2) } throws RuntimeException("Timeout")

        val emissions = repository.getAllGames(2).toList()

        assertTrue(emissions.last() is Resource.Error)
        assertEquals("Timeout", (emissions.last() as Resource.Error).message)
    }

    @Test
    fun `getAllGames page 2 API error with null message uses default error`() = runTest {
        coEvery { apiService.getGames(page = 2) } throws RuntimeException()

        val emissions = repository.getAllGames(2).toList()

        assertTrue(emissions.last() is Resource.Error)
        assertEquals("Unknown error", (emissions.last() as Resource.Error).message)
    }

    @Test
    fun `getAllGames maps response name correctly`() = runTest {
        coEvery { apiService.getGames(page = 1) } returns makeListResponse(
            makeGameResponse(id = 42, name = "Elden Ring")
        )

        val emissions = repository.getAllGames(1).toList()
        val data = (emissions.last() as Resource.Success).data

        assertEquals("Elden Ring", data?.first()?.name)
        assertEquals(42, data?.first()?.id)
    }

    // ── getGameDetail ──────────────────────────────────────────────────────

    @Test
    fun `getGameDetail emits Loading then Success and caches to Room`() = runTest {
        val detailResponse = com.deltarfd.deltagamesandroid.core.data.remote.response.GameDetailResponse(
            id = 1, slug = "game-1", name = "Game 1", released = "2024",
            backgroundImage = "img", rating = 4.0, ratingsCount = 10,
            metacritic = 80, playtime = 10, descriptionRaw = "Desc", website = "url", tags = emptyList(),
            genres = emptyList(), platforms = emptyList(), developers = emptyList(), publishers = emptyList()
        )
        coEvery { apiService.getGameDetail(1) } returns detailResponse
        every { gameDao.getGameById(1) } returns flowOf(makeGameEntity(id = 1, isFavorite = true))

        val emissions = repository.getGameDetail(1).toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)
        assertEquals("Game 1", (emissions[1] as Resource.Success).data?.name)
        assertEquals(true, (emissions[1] as Resource.Success).data?.isFavorite)
        coVerify { gameDao.insertGame(any()) }
    }

    @Test
    fun `getGameDetail API error emits Error`() = runTest {
        every { gameDao.getGameById(1) } returns kotlinx.coroutines.flow.flowOf(null)
        coEvery { apiService.getGameDetail(1) } throws RuntimeException("Network Error")

        val emissions = repository.getGameDetail(1).toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Error)
        assertEquals("Network Error", (emissions[1] as Resource.Error).message)
    }

    @Test
    fun `getGameDetail API error with null message uses default error`() = runTest {
        every { gameDao.getGameById(1) } returns kotlinx.coroutines.flow.flowOf(null)
        coEvery { apiService.getGameDetail(1) } throws RuntimeException()

        val emissions = repository.getGameDetail(1).toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Error)
        assertEquals("Unknown error", (emissions[1] as Resource.Error).message)
    }

    @Test
    fun `getGameDetail API error with cached data emits Success`() = runTest {
        every { gameDao.getGameById(1) } returns kotlinx.coroutines.flow.flowOf(makeGameEntity(id = 1))
        coEvery { apiService.getGameDetail(1) } throws RuntimeException("Network Error")

        val emissions = repository.getGameDetail(1).toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)
        assertEquals(1, (emissions[1] as Resource.Success).data?.id)
    }

    @Test
    fun `getGameDetail outer exception emits Error`() = runTest {
        // Simulate an exception thrown by the DAO flow itself
        every { gameDao.getGameById(1) } throws RuntimeException("DB crashed")

        val emissions = repository.getGameDetail(1).toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Error)
        assertEquals("DB crashed", (emissions[1] as Resource.Error).message)
    }

    @Test
    fun `getGameDetail outer exception with null message uses default error`() = runTest {
        every { gameDao.getGameById(1) } throws RuntimeException()

        val emissions = repository.getGameDetail(1).toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Error)
        assertEquals("Unknown error", (emissions[1] as Resource.Error).message)
    }

    // ── getTrendingGames ───────────────────────────────────────────────────

    @Test
    fun `getTrendingGames emits Loading then Success`() = runTest {
        coEvery { apiService.getTrendingGames() } returns makeListResponse(makeGameResponse())

        val emissions = repository.getTrendingGames().toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Success)
    }

    @Test
    fun `getTrendingGames API error emits Error`() = runTest {
        coEvery { apiService.getTrendingGames() } throws RuntimeException("Server error")

        val emissions = repository.getTrendingGames().toList()

        assertTrue(emissions.last() is Resource.Error)
        assertEquals("Server error", (emissions.last() as Resource.Error).message)
    }

    @Test
    fun `getTrendingGames API error with null message uses default error`() = runTest {
        coEvery { apiService.getTrendingGames() } throws RuntimeException()

        val emissions = repository.getTrendingGames().toList()

        assertTrue(emissions.last() is Resource.Error)
        assertEquals("Unknown error", (emissions.last() as Resource.Error).message)
    }

    // ── searchGames ────────────────────────────────────────────────────────

    @Test
    fun `searchGames success emits Loading then Success`() = runTest {
        coEvery { apiService.searchGames(query = "zelda", page = 1) } returns
            makeListResponse(makeGameResponse(name = "Zelda"))

        val emissions = repository.searchGames("zelda", 1).toList()

        assertTrue(emissions[0] is Resource.Loading)
        assertEquals("Zelda", (emissions[1] as Resource.Success).data?.first()?.name)
    }

    @Test
    fun `searchGames page 1 error falls back to local search`() = runTest {
        coEvery { apiService.searchGames(query = "zelda", page = 1) } throws RuntimeException("Offline")
        every { gameDao.searchGames("zelda") } returns flowOf(listOf(makeGameEntity(name = "Zelda Local")))

        val emissions = repository.searchGames("zelda", 1).toList()

        val success = emissions.filterIsInstance<Resource.Success<List<Game>>>().firstOrNull()
        assertEquals("Zelda Local", success?.data?.first()?.name)
    }

    @Test
    fun `searchGames page 1 error with empty local emits Error`() = runTest {
        coEvery { apiService.searchGames(query = "zzz", page = 1) } throws RuntimeException("Offline")
        every { gameDao.searchGames("zzz") } returns flowOf(emptyList())

        val emissions = repository.searchGames("zzz", 1).toList()

        assertTrue(emissions.any { it is Resource.Error })
    }

    @Test
    fun `searchGames page 1 error with empty local and null message uses default error`() = runTest {
        coEvery { apiService.searchGames(query = "zzz", page = 1) } throws RuntimeException()
        every { gameDao.searchGames("zzz") } returns flowOf(emptyList())

        val emissions = repository.searchGames("zzz", 1).toList()

        val error = emissions.filterIsInstance<Resource.Error<List<Game>>>().first()
        assertEquals("Unknown error", error.message)
    }

    @Test
    fun `searchGames page 2 error emits Error directly`() = runTest {
        coEvery { apiService.searchGames(query = "zelda", page = 2) } throws RuntimeException("Timeout")

        val emissions = repository.searchGames("zelda", 2).toList()

        assertTrue(emissions.last() is Resource.Error)
        assertEquals("Timeout", (emissions.last() as Resource.Error).message)
    }

    @Test
    fun `searchGames page 2 error with null message uses default error`() = runTest {
        coEvery { apiService.searchGames(query = "zelda", page = 2) } throws RuntimeException()

        val emissions = repository.searchGames("zelda", 2).toList()

        assertTrue(emissions.last() is Resource.Error)
        assertEquals("Unknown error", (emissions.last() as Resource.Error).message)
    }

    // ── getFavoriteGames ───────────────────────────────────────────────────

    @Test
    fun `getFavoriteGames maps DAO emission to domain list`() = runTest {
        every { gameDao.getFavoriteGames() } returns flowOf(
            listOf(makeGameEntity(id = 5, name = "Favorite Game", isFavorite = true))
        )

        val games = repository.getFavoriteGames().toList().flatten()

        assertEquals(1, games.size)
        assertEquals("Favorite Game", games.first().name)
        assertEquals(true, games.first().isFavorite)
    }

    @Test
    fun `getFavoriteGames returns empty list when no favorites`() = runTest {
        every { gameDao.getFavoriteGames() } returns flowOf(emptyList())

        val games = repository.getFavoriteGames().toList().flatten()

        assertEquals(0, games.size)
    }

    // ── setFavoriteGame ────────────────────────────────────────────────────

    @Test
    fun `setFavoriteGame calls setFavorite on DAO with correct id and state`() = runTest {
        val game = Game(
            id = 7, slug = "game-7", name = "Game 7", released = "2024-01-01",
            backgroundImage = "", rating = 4.0, ratingsCount = 50, metacritic = 75,
            playtime = 5, description = "", genres = "", platforms = "",
            developers = "", publishers = "", isFavorite = false
        )

        repository.setFavoriteGame(game, true)

        coVerify { gameDao.setFavorite(7, true) }
    }

    @Test
    fun `setFavoriteGame passes false correctly to DAO`() = runTest {
        val game = Game(
            id = 3, slug = "game-3", name = "Game 3", released = "2024-01-01",
            backgroundImage = "", rating = 3.0, ratingsCount = 20, metacritic = 60,
            playtime = 3, description = "", genres = "", platforms = "",
            developers = "", publishers = "", isFavorite = true
        )

        repository.setFavoriteGame(game, false)

        coVerify { gameDao.setFavorite(3, false) }
    }
}
