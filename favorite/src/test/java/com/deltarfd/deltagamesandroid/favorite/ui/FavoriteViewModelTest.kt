package com.deltarfd.deltagamesandroid.favorite.ui

import app.cash.turbine.test
import com.deltarfd.deltagamesandroid.core.domain.model.Game
import com.deltarfd.deltagamesandroid.core.domain.usecase.IGameUseCase
import com.deltarfd.deltagamesandroid.presentation.mapper.GamePresentationMapper
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteViewModelTest {

    private lateinit var gameUseCase: IGameUseCase
    private lateinit var viewModel: FavoriteViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        gameUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `favoriteGames emits mapped list from useCase`() = runTest(testDispatcher) {
        val games = listOf(
            Game(
                id = 1,
                slug = "slug",
                name = "Name",
                released = "2020",
                backgroundImage = "img",
                rating = 4.5,
                ratingsCount = 10,
                metacritic = 90,
                playtime = 5,
                description = "Desc",
                genres = "Action",
                platforms = "PC",
                developers = "Dev",
                publishers = "Pub",
                isFavorite = true
            )
        )
        val expectedMapped = GamePresentationMapper.mapListDomainToPresentation(games)

        coEvery { gameUseCase.getFavoriteGames() } returns flowOf(games)

        viewModel = FavoriteViewModel(gameUseCase)

        viewModel.favoriteGames.test {
            val item = awaitItem()
            // It will emit the default emptyList first if we don't start the test fast enough,
            // or if it collects immediately, it will emit the mapped list.
            // Let's just assert we receive the mapped items at some point.
            if (item.isEmpty()) {
                val nextItem = awaitItem()
                assertEquals(expectedMapped, nextItem)
            } else {
                assertEquals(expectedMapped, item)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
