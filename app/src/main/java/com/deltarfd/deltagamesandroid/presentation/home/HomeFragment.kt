package com.deltarfd.deltagamesandroid.presentation.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.deltarfd.deltagamesandroid.core.utils.Resource
import com.deltarfd.deltagamesandroid.databinding.FragmentHomeBinding
import com.deltarfd.deltagamesandroid.presentation.detail.DetailActivity
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModel()
    private var gamesAdapter: GamesAdapter? = null
    private var trendingAdapter: TrendingAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        observeViewModel()
        setupSwipeRefresh()
        setupScrollPagination()
    }

    private fun setupAdapters() {
        trendingAdapter = TrendingAdapter { game -> navigateToDetail(game) }
        binding.rvTrending.apply {
            adapter = trendingAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        gamesAdapter = GamesAdapter { game -> navigateToDetail(game) }
        binding.rvGames.apply {
            adapter = gamesAdapter
            layoutManager = GridLayoutManager(context, 2)
            isNestedScrollingEnabled = false
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trendingState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showTrendingLoading(true)
                    is Resource.Success -> {
                        showTrendingLoading(false)
                        trendingAdapter?.submitList(resource.data)
                    }
                    is Resource.Error -> showTrendingLoading(false)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.gamesState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showGamesLoading(true)
                    is Resource.Success -> {
                        showGamesLoading(false)
                        binding.tvError.visibility = View.GONE
                        gamesAdapter?.submitList(resource.data)
                    }
                    is Resource.Error -> {
                        showGamesLoading(false)
                        binding.tvError.visibility = View.VISIBLE
                        binding.tvError.text = resource.message
                    }
                }
            }
        }

        // Show a small spinner at the bottom when loading more pages
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoadingMore.collect { loading ->
                binding.progressLoadMore.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }
    }

    /**
     * NestedScrollView correctly forces GridLayoutManager to fully measure ALL items.
     * We listen here for bottom-proximity to trigger the next page.
     */
    private fun setupScrollPagination() {
        binding.scrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { nsv, _, scrollY, _, _ ->
                val child = nsv.getChildAt(0) ?: return@OnScrollChangeListener
                val atBottom = scrollY >= child.measuredHeight - nsv.measuredHeight - 300
                if (atBottom) viewModel.loadMoreGames()
            }
        )
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadGames()
            viewModel.loadTrending()
            binding.swipeRefresh.isRefreshing = false
        }
        binding.swipeRefresh.setColorSchemeResources(com.deltarfd.deltagamesandroid.R.color.colorPrimary)
    }

    private fun showTrendingLoading(loading: Boolean) {
        binding.shimmerTrending.apply {
            if (loading) { visibility = View.VISIBLE; startShimmer() }
            else { stopShimmer(); visibility = View.GONE }
        }
        binding.rvTrending.visibility = if (loading) View.GONE else View.VISIBLE
    }

    private fun showGamesLoading(loading: Boolean) {
        binding.shimmerGames.apply {
            if (loading) { visibility = View.VISIBLE; startShimmer() }
            else { stopShimmer(); visibility = View.GONE }
        }
        binding.rvGames.visibility = if (loading) View.GONE else View.VISIBLE
    }

    private fun navigateToDetail(game: GameItem) {
        startActivity(
            Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_GAME_ID, game.id)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvGames.adapter = null
        binding.rvTrending.adapter = null
        gamesAdapter = null
        trendingAdapter = null
        _binding = null
    }
}
