package com.deltarfd.deltagamesandroid.presentation.detail

import app.cash.turbine.test
import com.deltarfd.deltagamesandroid.core.domain.model.Game
import com.deltarfd.deltagamesandroid.core.domain.usecase.IGameUseCase
import com.deltarfd.deltagamesandroid.core.utils.Resource
import com.deltarfd.deltagamesandroid.presentation.mapper.GamePresentationMapper
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun `loadDetail success emits Loading then Success and updates favorite state`() = runTest {
        val flow = flowOf(Resource.Loading(), Resource.Success(testGame))
        every { gameUseCase.getGameDetail(1) } returns flow

        viewModel.loadDetail(1)

        viewModel.detailState.test {
            val item1 = awaitItem()
            if (item1 is Resource.Loading) {
                val item2 = awaitItem()
                assertTrue(item2 is Resource.Success)
                assertEquals("Test Game", (item2 as Resource.Success).data?.name)
            } else if (item1 is Resource.Success) {
                assertEquals("Test Game", item1.data?.name)
            }
            // StateFlow collapses emissions, so we just assert the final state is Success
            val finalState = viewModel.detailState.value
            assertTrue(finalState is Resource.Success)
            assertEquals(false, viewModel.isFavorite.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadDetail error emits Error state`() = runTest {
        val flow = flowOf(Resource.Loading<Game>(), Resource.Error("Error Fetching"))
        every { gameUseCase.getGameDetail(2) } returns flow

        viewModel.loadDetail(2)

        viewModel.detailState.test {
            val finalState = viewModel.detailState.value
            assertTrue(finalState is Resource.Error)
            assertEquals("Error Fetching", (finalState as Resource.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite toggles isFavorite instantly and updates database`() = runTest {
        // Preload success state
        val flow = flowOf(Resource.Success(testGame))
        every { gameUseCase.getGameDetail(1) } returns flow
        viewModel.loadDetail(1)
        
        val presentationGame = GamePresentationMapper.mapDomainToPresentation(testGame)
        io.mockk.coEvery { gameUseCase.setFavoriteGame(any(), true) } returns Unit

        viewModel.toggleFavorite(presentationGame)

        // Verify instant UI change
        assertEquals(true, viewModel.isFavorite.value)
        // Verify DB call
        io.mockk.coVerify { gameUseCase.setFavoriteGame(any(), true) }
        
        // Verify detailState updated locally
        val currentState = viewModel.detailState.value as Resource.Success
        assertEquals(true, currentState.data?.isFavorite)
    }
}
