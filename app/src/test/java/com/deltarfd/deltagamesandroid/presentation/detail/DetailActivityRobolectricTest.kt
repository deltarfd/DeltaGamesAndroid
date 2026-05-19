package com.deltarfd.deltagamesandroid.presentation.detail

import android.content.Intent
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deltarfd.deltagamesandroid.core.utils.Resource
import com.deltarfd.deltagamesandroid.databinding.ActivityDetailBinding
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], application = android.app.Application::class)
class DetailActivityTest {

    private lateinit var mockViewModel: DetailViewModel
    private val dummyState = MutableStateFlow<Resource<GameItem>>(Resource.Loading())
    private val dummyFavorite = MutableStateFlow(false)

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
        every { mockViewModel.detailState } returns dummyState
        every { mockViewModel.isFavorite } returns dummyFavorite

        startKoin {
            modules(module {
                viewModel { mockViewModel }
            })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `DetailActivity inflates successfully and handles Loading state`() {
        dummyState.value = Resource.Loading()
        
        val intent = Intent(ApplicationProvider.getApplicationContext(), DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_GAME_ID, 123)
        }
        
        ActivityScenario.launch<DetailActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val binding = getBinding(activity)
                assertEquals(android.view.View.VISIBLE, binding.progressBar.visibility)
                assertEquals(android.view.View.INVISIBLE, binding.contentLayout.visibility)
            }
        }
    }

    @Test
    fun `DetailActivity handles Success state and binds data`() {
        val game = GameItem(
            id = 1,
            name = "Test",
            slug = "test",
            coverUrl = "url",
            rating = 5.0,
            ratingDisplay = "4.5",
            releaseDate = "2020",
            metacritic = 90,
            playtime = 10,
            description = "Desc",
            genres = "Action",
            platforms = "PC",
            developers = "Dev",
            publishers = "Pub",
            isFavorite = false
        )
        dummyState.value = Resource.Success(game)
        
        val intent = Intent(ApplicationProvider.getApplicationContext(), DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_GAME_ID, 1)
        }
        
        ActivityScenario.launch<DetailActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val binding = getBinding(activity)
                assertEquals(android.view.View.GONE, binding.progressBar.visibility)
                assertEquals(android.view.View.VISIBLE, binding.contentLayout.visibility)
                assertEquals("Test", binding.tvGameName.text.toString())
                assertEquals(android.view.View.VISIBLE, binding.layoutMetacritic.visibility)
                
                // Emitting a second time should just update the reference
                dummyState.value = Resource.Success(game.copy(name = "Test2"))
            }
        }
    }

    @Test
    fun `DetailActivity handles Error state`() {
        dummyState.value = Resource.Error("Network Error")
        
        val intent = Intent(ApplicationProvider.getApplicationContext(), DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_GAME_ID, 1)
        }
        
        ActivityScenario.launch<DetailActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val binding = getBinding(activity)
                assertEquals(android.view.View.GONE, binding.progressBar.visibility)
            }
        }
    }

    @Test
    fun `DetailActivity formats missing fields correctly`() {
        val game = GameItem(
            id = 1, slug = "test", name = "Test", coverUrl = "", rating = 5.0, ratingDisplay = "4.5", releaseDate = "2020",
            metacritic = 0, playtime = 0, description = "", genres = "", platforms = "",
            developers = "", publishers = "", isFavorite = false
        )
        dummyState.value = Resource.Success(game)
        
        val intent = Intent(ApplicationProvider.getApplicationContext(), DetailActivity::class.java)
        ActivityScenario.launch<DetailActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val binding = getBinding(activity)
                assertEquals(android.view.View.GONE, binding.layoutMetacritic.visibility)
                
                // simulate button clicks
                binding.btnBack.performClick()
                assertTrue(activity.isFinishing)
                
                // favorite click
                binding.btnFavoriteIcon.performClick()
                io.mockk.verify { mockViewModel.toggleFavorite(any()) }
                
                // window focus changed coverage
                activity.onWindowFocusChanged(true)
            }
        }
    }

    @Test
    fun `DetailActivity handles favorite icon state changes`() {
        val game = GameItem(id = 1, slug = "test", name = "Test", coverUrl = "", rating = 5.0, ratingDisplay = "4.5", releaseDate = "2020", metacritic = 0, playtime = 0, description = "", genres = "", platforms = "", developers = "", publishers = "", isFavorite = false)
        dummyState.value = Resource.Success(game)
        
        val intent = Intent(ApplicationProvider.getApplicationContext(), DetailActivity::class.java)
        ActivityScenario.launch<DetailActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                dummyFavorite.value = true
                val binding = getBinding(activity)
                
                // toggle again from true to false
                binding.btnFavoriteIcon.performClick()
            }
        }
    }

    private fun getBinding(activity: DetailActivity): ActivityDetailBinding {
        val bindingField = DetailActivity::class.java.getDeclaredField("binding")
        bindingField.isAccessible = true
        return bindingField.get(activity) as ActivityDetailBinding
    }
}
