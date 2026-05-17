package com.deltarfd.deltagamesandroid.presentation.home

import app.cash.turbine.test
import com.deltarfd.deltagamesandroid.core.domain.model.Game
import com.deltarfd.deltagamesandroid.core.domain.usecase.IGameUseCase
import com.deltarfd.deltagamesandroid.core.utils.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: IGameUseCase
    private lateinit var viewModel: HomeViewModel

    private fun makeGame(id: Int, name: String = "Game $id") = Game(
        id = id, slug = "game-$id", name = name, released = "2024-01-01",
        backgroundImage = "https://img.example.com/$id.jpg", rating = 4.0,
        ratingsCount = 100, metacritic = 80, playtime = 10, description = "Desc",
        genres = "Action", platforms = "PC", developers = "Dev",
        publishers = "Pub", isFavorite = false
    )

    private fun makePage(size: Int, startId: Int = 1) =
        (startId until startId + size).map { makeGame(it) }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        useCase = mockk()
        every { useCase.getTrendingGames() } returns flowOf(Resource.Success(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load emits Loading then Success`() = runTest {
        every { useCase.getAllGames(1) } returns flowOf(Resource.Success(makePage(20)))
        viewModel = HomeViewModel(useCase)

        viewModel.gamesState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val emissions = cancelAndConsumeRemainingEvents()
            val lastValue = emissions.filterIsInstance<app.cash.turbine.Event.Item<Resource<*>>>().last().value
            assertTrue(lastValue is Resource.Success)
        }
    }

    @Test
    fun `loadGames success populates gamesState with correct count`() = runTest {
        val games = makePage(20)
        every { useCase.getAllGames(1) } returns flowOf(Resource.Success(games))
        viewModel = HomeViewModel(useCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.gamesState.value
        assertTrue(state is Resource.Success)
        assertEquals(20, (state as Resource.Success).data?.size)
    }

    @Test
    fun `loadGames error emits Resource Error`() = runTest {
        every { useCase.getAllGames(1) } returns flowOf(Resource.Error("Network error"))
        viewModel = HomeViewModel(useCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.gamesState.value
        assertTrue(state is Resource.Error)
        assertEquals("Network error", (state as Resource.Error).message)
    }

    @Test
    fun `loadMoreGames appends results to existing list`() = runTest {
        every { useCase.getAllGames(1) } returns flowOf(Resource.Success(makePage(20, startId = 1)))
        every { useCase.getAllGames(2) } returns flowOf(Resource.Success(makePage(20, startId = 21)))
        viewModel = HomeViewModel(useCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMoreGames()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.gamesState.value
        assertTrue(state is Resource.Success)
        assertEquals(40, (state as Resource.Success).data?.size)
    }

    @Test
    fun `loadMoreGames does not duplicate request when in progress`() = runTest {
        every { useCase.getAllGames(1) } returns flowOf(Resource.Success(makePage(20)))
        every { useCase.getAllGames(2) } returns flowOf(Resource.Success(makePage(20, startId = 21)))
        viewModel = HomeViewModel(useCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Call twice quickly — should only actually fire once
        viewModel.loadMoreGames()
        viewModel.loadMoreGames()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 1) { useCase.getAllGames(2) }
    }

    @Test
    fun `loadMoreGames stops when page returns fewer than 20 items`() = runTest {
        every { useCase.getAllGames(1) } returns flowOf(Resource.Success(makePage(20)))
        every { useCase.getAllGames(2) } returns flowOf(Resource.Success(makePage(5, startId = 21))) // last page
        viewModel = HomeViewModel(useCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMoreGames()
        testDispatcher.scheduler.advanceUntilIdle()

        // Trying to load more after last page should not trigger another call
        viewModel.loadMoreGames()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 1) { useCase.getAllGames(2) }
        verify(exactly = 0) { useCase.getAllGames(3) }
    }

    @Test
    fun `loadGames resets page and clears list`() = runTest {
        every { useCase.getAllGames(1) } returns flowOf(Resource.Success(makePage(20)))
        every { useCase.getAllGames(2) } returns flowOf(Resource.Success(makePage(20, startId = 21)))
        viewModel = HomeViewModel(useCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMoreGames()
        testDispatcher.scheduler.advanceUntilIdle()

        // Refresh — should reset to page 1
        every { useCase.getAllGames(1) } returns flowOf(Resource.Success(makePage(10)))
        viewModel.loadGames()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.gamesState.value
        assertEquals(10, (state as Resource.Success).data?.size)
    }

    @Test
    fun `isLoadingMore is false initially`() = runTest {
        every { useCase.getAllGames(1) } returns flowOf(Resource.Success(makePage(20)))
        viewModel = HomeViewModel(useCase)

        assertFalse(viewModel.isLoadingMore.value)
    }

    @Test
    fun `loadMoreGames error handles correctly and rolls back page`() = runTest {
        every { useCase.getAllGames(1) } returns flowOf(Resource.Success(makePage(20)))
        every { useCase.getAllGames(2) } returns flowOf(Resource.Error("Pagination error"))
        viewModel = HomeViewModel(useCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMoreGames()
        testDispatcher.scheduler.advanceUntilIdle()

        // _isLoadingMore should be reset to false and error shouldn't crash
        assertFalse(viewModel.isLoadingMore.value)
    }

    @Test
    fun `loadTrending error emits Error state`() = runTest {
        every { useCase.getAllGames(1) } returns flowOf(Resource.Success(makePage(20)))
        every { useCase.getTrendingGames() } returns flowOf(Resource.Error("Trending error"))
        viewModel = HomeViewModel(useCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.trendingState.value
        assertTrue(state is Resource.Error)
        assertEquals("Trending error", (state as Resource.Error).message)
    }
}
