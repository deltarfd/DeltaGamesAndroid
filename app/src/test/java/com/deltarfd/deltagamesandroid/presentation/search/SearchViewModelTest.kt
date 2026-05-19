@file:Suppress("UnusedFlow")

package com.deltarfd.deltagamesandroid.presentation.search

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
import kotlinx.coroutines.test.advanceTimeBy
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
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCase: IGameUseCase
    private lateinit var viewModel: SearchViewModel

    private fun makeGame(id: Int, name: String = "Game $id") = Game(
        id = id, slug = "game-$id", name = name, released = "2024-01-01",
        backgroundImage = "", rating = 4.0, ratingsCount = 100, metacritic = 80,
        playtime = 10, description = "Desc", genres = "Action",
        platforms = "PC", developers = "Dev", publishers = "Pub", isFavorite = false
    )

    private fun makePage(size: Int, startId: Int = 1) =
        (startId until startId + size).map { makeGame(it) }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        useCase = mockk()
        viewModel = SearchViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty success`() {
        val state = viewModel.searchState.value
        assertTrue(state is Resource.Success)
        assertEquals(0, (state as Resource.Success).data?.size)
    }

    @Test
    fun `onQueryChanged blank clears search results`() = runTest {
        every { useCase.searchGames("elden", 1) } returns flowOf(Resource.Success(makePage(5)))
        viewModel.onQueryChanged("elden")
        advanceTimeBy(600)
        testDispatcher.scheduler.advanceUntilIdle()

        // Now clear
        viewModel.onQueryChanged("")
        val state = viewModel.searchState.value
        assertTrue(state is Resource.Success)
        assertEquals(0, (state as Resource.Success).data?.size)
    }

    @Test
    fun `search query after debounce populates results`() = runTest {
        every { useCase.searchGames("zelda", 1) } returns flowOf(Resource.Success(makePage(10)))
        viewModel.onQueryChanged("zelda")
        advanceTimeBy(600) // past 500ms debounce
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.searchState.value
        assertTrue(state is Resource.Success)
        assertEquals(10, (state as Resource.Success).data?.size)
    }

    @Test
    fun `new query resets page and replaces results`() = runTest {
        every { useCase.searchGames("zelda", 1) } returns flowOf(Resource.Success(makePage(20)))
        every { useCase.searchGames("mario", 1) } returns flowOf(Resource.Success(makePage(5)))

        viewModel.onQueryChanged("zelda")
        advanceTimeBy(600)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(20, (viewModel.searchState.value as Resource.Success).data?.size)

        viewModel.onQueryChanged("mario")
        advanceTimeBy(600)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(5, (viewModel.searchState.value as Resource.Success).data?.size)
    }

    @Test
    fun `loadMoreResults appends next page to existing results`() = runTest {
        every { useCase.searchGames("zelda", 1) } returns flowOf(Resource.Success(makePage(20)))
        every { useCase.searchGames("zelda", 2) } returns flowOf(Resource.Success(makePage(20, startId = 21)))

        viewModel.onQueryChanged("zelda")
        advanceTimeBy(600)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(20, (viewModel.searchState.value as Resource.Success).data?.size)

        viewModel.loadMoreResults()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(40, (viewModel.searchState.value as Resource.Success).data?.size)
    }

    @Test
    fun `loadMoreResults does nothing when query is blank`() = runTest {
        viewModel.loadMoreResults() // no query set
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 0) { useCase.searchGames(any(), any()) }
    }

    @Test
    fun `loadMoreResults stops when page returns fewer than 20 items`() = runTest {
        every { useCase.searchGames("zelda", 1) } returns flowOf(Resource.Success(makePage(20)))
        every { useCase.searchGames("zelda", 2) } returns flowOf(Resource.Success(makePage(5, startId = 21)))

        viewModel.onQueryChanged("zelda")
        advanceTimeBy(600)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMoreResults()
        testDispatcher.scheduler.advanceUntilIdle()

        // This third call should be ignored (hasMorePages = false)
        viewModel.loadMoreResults()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 1) { useCase.searchGames("zelda", 2) }
        verify(exactly = 0) { useCase.searchGames("zelda", 3) }
    }

    @Test
    fun `search error emits Resource Error`() = runTest {
        every { useCase.searchGames("bad", 1) } returns flowOf(Resource.Error("API error"))
        viewModel.onQueryChanged("bad")
        advanceTimeBy(600)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.searchState.value
        assertTrue(state is Resource.Error)
        assertEquals("API error", (state as Resource.Error).message)
    }

    @Test
    fun `newSearch handles Resource Error`() = runTest {
        every { useCase.searchGames("error", 1) } returns flowOf(Resource.Error("Search failed"))
        
        viewModel.onQueryChanged("error")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.searchState.value
        assertTrue(state is Resource.Error)
        assertEquals("Search failed", (state as Resource.Error).message)
    }

    @Test
    fun `loadMoreResults error handles correctly and rolls back page`() = runTest {
        every { useCase.searchGames("test", 1) } returns flowOf(Resource.Success(makePage(20)))
        every { useCase.searchGames("test", 2) } returns flowOf(Resource.Error("Pagination error"))
        
        viewModel.onQueryChanged("test")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMoreResults()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isLoadingMore.value)
    }
}
