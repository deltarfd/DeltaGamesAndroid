package com.deltarfd.deltagamesandroid

import android.os.Build
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import com.deltarfd.deltagamesandroid.databinding.ActivityMainBinding
import com.deltarfd.deltagamesandroid.presentation.home.HomeViewModel
import com.deltarfd.deltagamesandroid.core.utils.Resource
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], application = android.app.Application::class)
class MainActivityTest {

    private lateinit var mockHomeViewModel: HomeViewModel

    @Before
    fun setup() {
        mockHomeViewModel = mockk(relaxed = true)
        val dummyGames = MutableStateFlow<Resource<List<GameItem>>>(Resource.Loading())
        every { mockHomeViewModel.gamesState } returns dummyGames
        every { mockHomeViewModel.trendingState } returns dummyGames
        every { mockHomeViewModel.isLoadingMore } returns MutableStateFlow(false)

        startKoin {
            modules(module {
                viewModel { mockHomeViewModel }
            })
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun `MainActivity inflates layout and sets up BottomNavigationView`() {
        activityRule.scenario.onActivity { activity ->
            // In a Robolectric environment without Koin set up, MainActivity might crash if
            // fragments are automatically restored and request ViewModels.
            // But just testing inflation and view availability is enough to get coverage
            // on the onCreate flow of MainActivity.
            val bindingField = MainActivity::class.java.getDeclaredField("binding")
            bindingField.isAccessible = true
            val binding = bindingField.get(activity) as ActivityMainBinding
            
            assertNotNull(binding.bottomNavView)
            assertNotNull(binding.navHostFragment)
        }
    }
}
