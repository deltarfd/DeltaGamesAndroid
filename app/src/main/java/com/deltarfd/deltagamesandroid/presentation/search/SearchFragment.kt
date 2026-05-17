package com.deltarfd.deltagamesandroid.presentation.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deltarfd.deltagamesandroid.core.utils.Resource
import com.deltarfd.deltagamesandroid.databinding.FragmentSearchBinding
import com.deltarfd.deltagamesandroid.presentation.detail.DetailActivity
import com.deltarfd.deltagamesandroid.presentation.home.GamesAdapter
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()
    private lateinit var searchAdapter: GamesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        searchAdapter = GamesAdapter { game -> navigateToDetail(game) }
        val layoutManager = GridLayoutManager(context, 2)
        binding.rvSearchResults.apply {
            adapter = searchAdapter
            this.layoutManager = layoutManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy <= 0) return
                    val visibleCount = layoutManager.childCount
                    val totalCount   = layoutManager.itemCount
                    val firstVisible = layoutManager.findFirstVisibleItemPosition()
                    
                    if (firstVisible + visibleCount >= totalCount - 6) {
                        viewModel.loadMoreResults()
                    }
                }
            })
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            viewModel.onQueryChanged(text.toString())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvSearchResults.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        val data = resource.data ?: emptyList()
                        searchAdapter.submitList(data)
                        val hasQuery = binding.etSearch.text.isNotEmpty()
                        binding.rvSearchResults.visibility = if (data.isNotEmpty()) View.VISIBLE else View.GONE
                        binding.tvEmpty.visibility =
                            if (data.isEmpty() && hasQuery) View.VISIBLE
                            else if (!hasQuery) View.VISIBLE
                            else View.GONE
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvSearchResults.visibility = View.GONE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoadingMore.collect { loading ->
                binding.progressLoadMore.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }
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
        _binding = null
    }
}
