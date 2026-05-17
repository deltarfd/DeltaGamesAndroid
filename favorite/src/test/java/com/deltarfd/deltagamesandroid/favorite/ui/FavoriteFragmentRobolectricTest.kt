package com.deltarfd.deltagamesandroid.favorite.ui

import android.os.Build
import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deltarfd.deltagamesandroid.R
import com.deltarfd.deltagamesandroid.favorite.databinding.FragmentFavoriteBinding
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], application = android.app.Application::class, manifest = "favorite/src/test/AndroidManifest.xml")
class FavoriteFragmentRobolectricTest {

    private lateinit var mockViewModel: FavoriteViewModel
    private val dummyFavorites = MutableStateFlow<List<GameItem>>(emptyList())

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
        every { mockViewModel.favoriteGames } returns dummyFavorites

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
    fun `FavoriteFragment displays empty state correctly`() {
        dummyFavorites.value = emptyList()
        
        val scenario = launchFragmentInContainer<FavoriteFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            assertEquals(View.VISIBLE, binding.layoutEmpty.visibility)
            assertEquals(View.GONE, binding.rvFavorites.visibility)
        }
    }

    @Test
    fun `FavoriteFragment displays data correctly and handles clicks`() {
        val games = listOf(GameItem(1, "Test", "test", "2020", "url", 5.0, "5", "Action", "PC", 10, 90, "Desc", "Dev", "Pub", true))
        dummyFavorites.value = games
        
        val scenario = launchFragmentInContainer<FavoriteFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            assertEquals(View.GONE, binding.layoutEmpty.visibility)
            assertEquals(View.VISIBLE, binding.rvFavorites.visibility)

            // Simulate click
            binding.rvFavorites.findViewHolderForAdapterPosition(0)?.itemView?.performClick()
        }
    }

    private fun getBinding(fragment: FavoriteFragment): FragmentFavoriteBinding {
        val bindingField = FavoriteFragment::class.java.getDeclaredField("_binding")
        bindingField.isAccessible = true
        return bindingField.get(fragment) as FragmentFavoriteBinding
    }
}
