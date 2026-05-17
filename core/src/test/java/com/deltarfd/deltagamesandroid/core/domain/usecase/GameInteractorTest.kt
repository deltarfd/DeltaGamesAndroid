package com.deltarfd.deltagamesandroid.core.domain.usecase

import com.deltarfd.deltagamesandroid.core.domain.model.Game
import com.deltarfd.deltagamesandroid.core.domain.repository.IGameRepository
import com.deltarfd.deltagamesandroid.core.utils.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class GameInteractorTest {

    private val gameRepository = mockk<IGameRepository>()
    private val interactor = GameInteractor(gameRepository)

    @Test
    fun `getAllGames should forward call to repository`() {
        val flow = flowOf(Resource.Loading<List<Game>>())
        every { gameRepository.getAllGames(1) } returns flow

        val result = interactor.getAllGames(1)

        assert(result == flow)
        verify { gameRepository.getAllGames(1) }
    }

    @Test
    fun `getTrendingGames should forward call to repository`() {
        val flow = flowOf(Resource.Loading<List<Game>>())
        every { gameRepository.getTrendingGames() } returns flow

        val result = interactor.getTrendingGames()

        assert(result == flow)
        verify { gameRepository.getTrendingGames() }
    }

    @Test
    fun `getGameDetail should forward call to repository`() {
        val flow = flowOf(Resource.Loading<Game>())
        every { gameRepository.getGameDetail(1) } returns flow

        val result = interactor.getGameDetail(1)

        assert(result == flow)
        verify { gameRepository.getGameDetail(1) }
    }

    @Test
    fun `getFavoriteGames should forward call to repository`() {
        val flow = flowOf(listOf<Game>())
        every { gameRepository.getFavoriteGames() } returns flow

        val result = interactor.getFavoriteGames()

        assert(result == flow)
        verify { gameRepository.getFavoriteGames() }
    }

    @Test
    fun `setFavoriteGame should forward call to repository`() = kotlinx.coroutines.test.runTest {
        val game = mockk<Game>()
        io.mockk.coEvery { gameRepository.setFavoriteGame(game, true) } returns Unit

        interactor.setFavoriteGame(game, true)

        io.mockk.coVerify { gameRepository.setFavoriteGame(game, true) }
    }

    @Test
    fun `searchGames should forward call to repository`() {
        val flow = flowOf(Resource.Loading<List<Game>>())
        every { gameRepository.searchGames("test", 1) } returns flow

        val result = interactor.searchGames("test", 1)

        assert(result == flow)
        verify { gameRepository.searchGames("test", 1) }
    }
}
