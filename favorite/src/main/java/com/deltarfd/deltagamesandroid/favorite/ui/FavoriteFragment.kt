package com.deltarfd.deltagamesandroid.favorite.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.deltarfd.deltagamesandroid.favorite.databinding.FragmentFavoriteBinding
import com.deltarfd.deltagamesandroid.favorite.di.favoriteModule
import com.deltarfd.deltagamesandroid.presentation.detail.DetailActivity
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    // Lazy — Koin resolves after loadKoinModules in onCreate
    private val viewModel: FavoriteViewModel by viewModel()
    private var favoriteAdapter: FavoriteAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load favorite Koin module — safe to call multiple times (Koin deduplicates)
        loadKoinModules(favoriteModule)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEdgeToEdge()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupEdgeToEdge() {
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBar = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, statusBar, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteAdapter { game -> navigateToDetail(game) }
        binding.rvFavorites.apply {
            adapter = favoriteAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteGames.collect { games ->
                favoriteAdapter?.submitList(games)
                binding.layoutEmpty.isVisible = games.isEmpty()
                binding.rvFavorites.isVisible = games.isNotEmpty()
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
        binding.rvFavorites.adapter = null
        favoriteAdapter = null
        _binding = null
    }
}
