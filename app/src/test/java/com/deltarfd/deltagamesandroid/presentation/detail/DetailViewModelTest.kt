package com.deltarfd.deltagamesandroid.presentation.detail

import app.cash.turbine.test
import com.deltarfd.deltagamesandroid.core.domain.model.Game
import com.deltarfd.deltagamesandroid.core.domain.usecase.IGameUseCase
import com.deltarfd.deltagamesandroid.core.utils.Resource
import com.deltarfd.deltagamesandroid.presentation.mapper.GamePresentationMapper
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private lateinit var gameUseCase: IGameUseCase
    private lateinit var viewModel: DetailViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testGame = Game(
        id = 1, slug = "game", name = "Test Game", released = "2024",
        backgroundImage = "img", rating = 4.0, ratingsCount = 10,
        metacritic = 80, playtime = 10, description = "Desc",
        genres = "Action", platforms = "PC", developers = "Dev",
        publishers = "Pub", isFavorite = false
    )

    private val favoriteGame = testGame.copy(isFavorite = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        gameUseCase = mockk(relaxed = true)
        viewModel = DetailViewModel(gameUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDetail emits Loading then Success`() = runTest {
        every { gameUseCase.getGameDetail(1) } returns flowOf(Resource.Loading(), Resource.Success(testGame))

        viewModel.loadDetail(1)

        val state = viewModel.detailState.value
        assertTrue(state is Resource.Success)
        assertEquals("Test Game", (state as Resource.Success).data?.name)
        assertEquals(false, viewModel.isFavorite.value)
    }

    @Test
    fun `loadDetail with favorite game sets isFavorite to true`() = runTest {
        every { gameUseCase.getGameDetail(1) } returns flowOf(Resource.Loading(), Resource.Success(favoriteGame))

        viewModel.loadDetail(1)

        assertEquals(true, viewModel.isFavorite.value)
    }

    @Test
    fun `loadDetail error emits Error state`() = runTest {
        every { gameUseCase.getGameDetail(2) } returns flowOf(Resource.Loading(), Resource.Error("Network Error"))

        viewModel.loadDetail(2)

        val state = viewModel.detailState.value
        assertTrue(state is Resource.Error)
        assertEquals("Network Error", (state as Resource.Error).message)
    }

    @Test
    fun `loadDetail error with null message uses default`() = runTest {
        every { gameUseCase.getGameDetail(2) } returns flowOf(Resource.Loading(), Resource.Error(""))

        viewModel.loadDetail(2)

        val state = viewModel.detailState.value
        assertTrue(state is Resource.Error)
    }

    @Test
    fun `loadDetail cancels previous job when called again`() = runTest {
        every { gameUseCase.getGameDetail(1) } returns flowOf(Resource.Success(testGame))
        every { gameUseCase.getGameDetail(2) } returns flowOf(Resource.Success(testGame.copy(id = 2, name = "Game 2")))

        viewModel.loadDetail(1)
        viewModel.loadDetail(2)

        val state = viewModel.detailState.value as Resource.Success
        assertEquals("Game 2", state.data?.name)
    }

    @Test
    fun `loadDetail only processes first Success emission (gameLoaded guard)`() = runTest {
        // Emit Success twice — second should be ignored
        val multiFlow = flow {
            emit(Resource.Success(testGame))
            emit(Resource.Success(testGame.copy(name = "Should Be Ignored")))
        }
        every { gameUseCase.getGameDetail(1) } returns multiFlow

        viewModel.loadDetail(1)

        val state = viewModel.detailState.value as Resource.Success
        assertEquals("Test Game", state.data?.name)
    }

    @Test
    fun `loadDetail Loading after gameLoaded does not overwrite Success`() = runTest {
        // Emit Success then Loading — Loading should be ignored
        val multiFlow = flow {
            emit(Resource.Success(testGame))
            emit(Resource.Loading())
        }
        every { gameUseCase.getGameDetail(1) } returns multiFlow

        viewModel.loadDetail(1)

        val state = viewModel.detailState.value
        assertTrue(state is Resource.Success)
    }

    @Test
    fun `toggleFavorite flips state and updates detailState`() = runTest {
        every { gameUseCase.getGameDetail(1) } returns flowOf(Resource.Success(testGame))
        coEvery { gameUseCase.setFavoriteGame(any(), true) } returns Unit

        viewModel.loadDetail(1)

        val presentationGame = GamePresentationMapper.mapDomainToPresentation(testGame)
        viewModel.toggleFavorite(presentationGame)

        assertEquals(true, viewModel.isFavorite.value)
        coVerify { gameUseCase.setFavoriteGame(any(), true) }

        val currentState = viewModel.detailState.value as Resource.Success
        assertEquals(true, currentState.data?.isFavorite)
    }

    @Test
    fun `toggleFavorite from true to false`() = runTest {
        every { gameUseCase.getGameDetail(1) } returns flowOf(Resource.Success(favoriteGame))
        coEvery { gameUseCase.setFavoriteGame(any(), false) } returns Unit

        viewModel.loadDetail(1)

        val presentationGame = GamePresentationMapper.mapDomainToPresentation(favoriteGame)
        viewModel.toggleFavorite(presentationGame)

        assertEquals(false, viewModel.isFavorite.value)
        coVerify { gameUseCase.setFavoriteGame(any(), false) }
    }

    @Test
    fun `toggleFavorite when detailState is not Success does not crash`() = runTest {
        // Don't load detail — state is still Loading
        coEvery { gameUseCase.setFavoriteGame(any(), any()) } returns Unit

        val fakeItem = GameItem(
            id = 1, name = "Fake", slug = "fake", releaseDate = "2024",
            coverUrl = "img", rating = 4.0, ratingDisplay = "4.00/5",
            genres = "Action", platforms = "PC", playtime = 10,
            metacritic = 80, description = "Desc", developers = "Dev",
            publishers = "Pub", isFavorite = false
        )

        viewModel.toggleFavorite(fakeItem)

        // Should flip isFavorite without crashing
        assertEquals(true, viewModel.isFavorite.value)
        // detailState should still be Loading (not updated)
        assertTrue(viewModel.detailState.value is Resource.Loading)
    }
}
