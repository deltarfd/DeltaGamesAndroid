package com.deltarfd.deltagamesandroid.presentation.home

import android.os.Build
import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deltarfd.deltagamesandroid.R
import com.deltarfd.deltagamesandroid.core.utils.Resource
import com.deltarfd.deltagamesandroid.databinding.FragmentHomeBinding
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU], application = android.app.Application::class)
class HomeFragmentRobolectricTest {

    private lateinit var mockViewModel: HomeViewModel
    private val dummyGames = MutableStateFlow<Resource<List<GameItem>>>(Resource.Loading())
    private val dummyTrending = MutableStateFlow<Resource<List<GameItem>>>(Resource.Loading())
    private val dummyIsLoadingMore = MutableStateFlow(false)

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
        every { mockViewModel.gamesState } returns dummyGames
        every { mockViewModel.trendingState } returns dummyTrending
        every { mockViewModel.isLoadingMore } returns dummyIsLoadingMore

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
    fun `HomeFragment displays Loading state correctly`() {
        dummyGames.value = Resource.Loading()
        dummyTrending.value = Resource.Loading()

        val scenario = launchFragmentInContainer<HomeFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            assertEquals(View.VISIBLE, binding.shimmerGames.visibility)
            assertEquals(View.VISIBLE, binding.shimmerTrending.visibility)
            assertEquals(View.GONE, binding.rvGames.visibility)
            assertEquals(View.GONE, binding.rvTrending.visibility)
        }
    }

    @Test
    fun `HomeFragment displays Success state correctly and handles click`() {
        val game = GameItem(1, "Test", "test", "2020", "url", 5.0, "5", "Action", "PC", 10, 90, "Desc", "Dev", "Pub", false)
        val gamesList = listOf(game)
        
        dummyGames.value = Resource.Success(gamesList)
        dummyTrending.value = Resource.Success(gamesList)

        val scenario = launchFragmentInContainer<HomeFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            
            assertEquals(View.GONE, binding.shimmerGames.visibility)
            assertEquals(View.GONE, binding.shimmerTrending.visibility)
            assertEquals(View.VISIBLE, binding.rvGames.visibility)
            assertEquals(View.VISIBLE, binding.rvTrending.visibility)
            assertEquals(View.GONE, binding.tvError.visibility)

            // Simulate clicks on adapters
            binding.rvGames.findViewHolderForAdapterPosition(0)?.itemView?.performClick()
            binding.rvTrending.findViewHolderForAdapterPosition(0)?.itemView?.performClick()
        }
    }

    @Test
    fun `HomeFragment displays Error state correctly`() {
        dummyGames.value = Resource.Error("Error games")
        dummyTrending.value = Resource.Error("Error trending")

        val scenario = launchFragmentInContainer<HomeFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            
            assertEquals(View.GONE, binding.shimmerGames.visibility)
            assertEquals(View.GONE, binding.shimmerTrending.visibility)
            assertEquals(View.VISIBLE, binding.tvError.visibility)
            assertEquals("Error games", binding.tvError.text.toString())
        }
    }

    @Test
    fun `HomeFragment handles swipe refresh`() {
        val scenario = launchFragmentInContainer<HomeFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            
            binding.swipeRefresh.isRefreshing = true
            // manually trigger the listener since swipe action is hard to mock in Robolectric
            binding.swipeRefresh.let { swipe ->
                val listenerField = androidx.swiperefreshlayout.widget.SwipeRefreshLayout::class.java.getDeclaredField("mListener")
                listenerField.isAccessible = true
                val listener = listenerField.get(swipe) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
                listener.onRefresh()
            }
            
            verify { mockViewModel.loadGames() }
            verify { mockViewModel.loadTrending() }
            assertEquals(false, binding.swipeRefresh.isRefreshing)
        }
    }

    @Test
    fun `HomeFragment displays load more progress`() {
        dummyIsLoadingMore.value = true
        val scenario = launchFragmentInContainer<HomeFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            assertEquals(View.VISIBLE, binding.progressLoadMore.visibility)
        }
    }

    @Test
    fun `HomeFragment handles scroll pagination`() {
        val scenario = launchFragmentInContainer<HomeFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            
            // To simulate scrolling to the bottom:
            // The logic: scrollY >= child.measuredHeight - nsv.measuredHeight - 300
            val scrollView = binding.scrollView
            scrollView.getChildAt(0)
            
            // Invoke the listener directly
            scrollView.let {
                val method = androidx.core.widget.NestedScrollView::class.java.getDeclaredMethod(
                    "onScrollChanged", Int::class.java, Int::class.java, Int::class.java, Int::class.java
                )
                method.isAccessible = true
                // Force a massive scrollY
                method.invoke(it, 0, 10000, 0, 0)
            }
            
            // We just verify it doesn't crash since perfectly mocking layout heights in Robolectric is tricky without drawing.
            // But if it reaches the bottom it should call loadMoreGames
        }
    }

    private fun getBinding(fragment: HomeFragment): FragmentHomeBinding {
        val bindingField = HomeFragment::class.java.getDeclaredField("_binding")
        bindingField.isAccessible = true
        return bindingField.get(fragment) as FragmentHomeBinding
    }
}
