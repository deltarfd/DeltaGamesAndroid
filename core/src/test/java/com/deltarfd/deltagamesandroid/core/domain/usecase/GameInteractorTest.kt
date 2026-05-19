@file:Suppress("UnusedFlow")

package com.deltarfd.deltagamesandroid.core.domain.usecase

import com.deltarfd.deltagamesandroid.core.domain.model.Game
import com.deltarfd.deltagamesandroid.core.domain.repository.IGameRepository
import com.deltarfd.deltagamesandroid.core.utils.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test

class GameInteractorTest {

    private val gameRepository = mockk<IGameRepository>()
    private val interactor = GameInteractor(gameRepository)

    @Test
    fun `getAllGames should forward call to repository`() {
        val expected: Flow<Resource<List<Game>>> = flowOf(Resource.Loading())
        every { gameRepository.getAllGames(1) } returns expected

        val result = interactor.getAllGames(1)

        assertSame(expected, result)
        verify { gameRepository.getAllGames(1) }
    }

    @Test
    fun `getTrendingGames should forward call to repository`() {
        val expected: Flow<Resource<List<Game>>> = flowOf(Resource.Loading())
        every { gameRepository.getTrendingGames() } returns expected

        val result = interactor.getTrendingGames()

        assertSame(expected, result)
        verify { gameRepository.getTrendingGames() }
    }

    @Test
    fun `getGameDetail should forward call to repository`() {
        val expected: Flow<Resource<Game>> = flowOf(Resource.Loading())
        every { gameRepository.getGameDetail(1) } returns expected

        val result = interactor.getGameDetail(1)

        assertSame(expected, result)
        verify { gameRepository.getGameDetail(1) }
    }

    @Test
    fun `getFavoriteGames should forward call to repository`() {
        val expected: Flow<List<Game>> = flowOf(emptyList())
        every { gameRepository.getFavoriteGames() } returns expected

        val result = interactor.getFavoriteGames()

        assertSame(expected, result)
        verify { gameRepository.getFavoriteGames() }
    }

    @Test
    fun `setFavoriteGame should forward call to repository`() = runTest {
        val game = mockk<Game>()
        coEvery { gameRepository.setFavoriteGame(game, true) } returns Unit

        interactor.setFavoriteGame(game, true)

        coVerify { gameRepository.setFavoriteGame(game, true) }
    }

    @Test
    fun `searchGames should forward call to repository`() {
        val expected: Flow<Resource<List<Game>>> = flowOf(Resource.Loading())
        every { gameRepository.searchGames("test", 1) } returns expected

        val result = interactor.searchGames("test", 1)

        assertSame(expected, result)
        verify { gameRepository.searchGames("test", 1) }
    }
}
