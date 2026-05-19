package com.deltarfd.deltagamesandroid.presentation.search

import android.os.Build
import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.deltarfd.deltagamesandroid.R
import com.deltarfd.deltagamesandroid.core.utils.Resource
import com.deltarfd.deltagamesandroid.databinding.FragmentSearchBinding
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
class SearchFragmentRobolectricTest {

    private lateinit var mockViewModel: SearchViewModel
    private val dummySearch = MutableStateFlow<Resource<List<GameItem>>>(Resource.Success(emptyList()))
    private val dummyIsLoadingMore = MutableStateFlow(false)

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
        
        every { mockViewModel.searchState } returns dummySearch
        every { mockViewModel.isLoadingMore } returns dummyIsLoadingMore
        every { mockViewModel.queryFlow } returns MutableStateFlow("")

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
    fun `SearchFragment displays Loading state`() {
        dummySearch.value = Resource.Loading()
        
        val scenario = launchFragmentInContainer<SearchFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            assertEquals(View.VISIBLE, binding.progressBar.visibility)
            assertEquals(View.GONE, binding.tvEmpty.visibility)
            assertEquals(View.GONE, binding.rvSearchResults.visibility)
        }
    }

    @Test
    fun `SearchFragment displays Error state`() {
        dummySearch.value = Resource.Error("Error")
        
        val scenario = launchFragmentInContainer<SearchFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            assertEquals(View.GONE, binding.progressBar.visibility)
            assertEquals(View.GONE, binding.tvEmpty.visibility)
            assertEquals(View.VISIBLE, binding.layoutNoResults.visibility)
            assertEquals(View.GONE, binding.rvSearchResults.visibility)
        }
    }

    @Test
    fun `SearchFragment displays Success with Data and handles clicks`() {
        val games = listOf(GameItem(1, "Test", "test", "2020", "url", 5.0, "5", "Action", "PC", 10, 90, "Desc", "Dev", "Pub", false))
        
        val scenario = launchFragmentInContainer<SearchFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            binding.etSearch.setText("query")
            dummySearch.value = Resource.Success(games)
            
            assertEquals(View.GONE, binding.progressBar.visibility)
            assertEquals(View.VISIBLE, binding.rvSearchResults.visibility)
            assertEquals(View.GONE, binding.tvEmpty.visibility)
            
            verify { mockViewModel.onQueryChanged("query") }
            
            binding.rvSearchResults.findViewHolderForAdapterPosition(0)?.itemView?.performClick()

            binding.rvSearchResults.layoutManager as androidx.recyclerview.widget.GridLayoutManager
            val scrollListenerField = androidx.recyclerview.widget.RecyclerView::class.java.getDeclaredField("mScrollListeners")
            scrollListenerField.isAccessible = true
            val listeners = scrollListenerField.get(binding.rvSearchResults) as List<androidx.recyclerview.widget.RecyclerView.OnScrollListener>?
            listeners?.forEach { it.onScrolled(binding.rvSearchResults, 0, 10) }
        }
    }
    
    @Test
    fun `SearchFragment displays Success without Data but with Query`() {
        val scenario = launchFragmentInContainer<SearchFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            binding.etSearch.setText("nonexistent")
            dummySearch.value = Resource.Success(emptyList())
            
            assertEquals(View.GONE, binding.progressBar.visibility)
            assertEquals(View.GONE, binding.rvSearchResults.visibility)
            assertEquals(View.GONE, binding.tvEmpty.visibility)
            assertEquals(View.VISIBLE, binding.layoutNoResults.visibility)
        }
    }

    @Test
    fun `SearchFragment displays Success without Data and without Query`() {
        val scenario = launchFragmentInContainer<SearchFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            binding.etSearch.setText("")
            dummySearch.value = Resource.Success(emptyList())
            
            assertEquals(View.GONE, binding.progressBar.visibility)
            assertEquals(View.GONE, binding.rvSearchResults.visibility)
            assertEquals(View.VISIBLE, binding.tvEmpty.visibility)
        }
    }

    @Test
    fun `SearchFragment displays load more progress`() {
        dummyIsLoadingMore.value = true
        val scenario = launchFragmentInContainer<SearchFragment>(themeResId = R.style.Theme_DeltaGames)
        scenario.onFragment { fragment ->
            val binding = getBinding(fragment)
            assertEquals(View.VISIBLE, binding.progressLoadMore.visibility)
        }
    }

    private fun getBinding(fragment: SearchFragment): FragmentSearchBinding {
        val bindingField = SearchFragment::class.java.getDeclaredField("_binding")
        bindingField.isAccessible = true
        return bindingField.get(fragment) as FragmentSearchBinding
    }
}
