package com.deltarfd.deltagamesandroid.presentation.detail

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.deltarfd.deltagamesandroid.R
import com.deltarfd.deltagamesandroid.core.utils.Resource
import com.deltarfd.deltagamesandroid.databinding.ActivityDetailBinding
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModel()
    private var currentGame: GameItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gameId = intent.getIntExtra(EXTRA_GAME_ID, -1)

        setupButtons()
        observeDetail()
        observeFavoriteState()

        if (gameId != -1) {
            viewModel.loadDetail(gameId)
        }
    }

    /** Set up buttons once — favorite uses currentGame ref, not re-bound on every state change */
    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnFavoriteIcon.setOnClickListener {
            currentGame?.let { g ->
                viewModel.toggleFavorite(g)
                // Update local reference so next toggle is based on latest state
                currentGame = g.copy(isFavorite = !g.isFavorite)
                val msg = if (!g.isFavorite) getString(R.string.added_to_favorite)
                          else getString(R.string.removed_from_favorite)
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Observe detail data — only binds the game content once on first success */
    private fun observeDetail() {
        lifecycleScope.launch {
            viewModel.detailState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> showLoading(true)
                    is Resource.Success -> {
                        showLoading(false)
                        resource.data?.let { game ->
                            // Only do full bind on first load
                            if (currentGame == null) {
                                currentGame = game
                                bindGameDetail(game)
                            } else {
                                // On subsequent emissions just update reference
                                currentGame = game
                            }
                        }
                    }
                    is Resource.Error -> {
                        showLoading(false)
                        Toast.makeText(this@DetailActivity, resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /** Separate observer for just the favorite icon — updates immediately, no full rebind */
    private fun observeFavoriteState() {
        lifecycleScope.launch {
            viewModel.isFavorite.collect { isFav ->
                updateFavoriteIcon(isFav)
            }
        }
    }

    private fun bindGameDetail(game: GameItem) {
        Glide.with(this)
            .load(game.coverUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .placeholder(R.drawable.placeholder_game)
            .into(binding.ivGameCover)

        with(binding) {
            tvGameName.text    = game.name
            tvRating.text      = game.ratingDisplay
            tvReleaseDate.text = getString(R.string.release_date_format, game.releaseDate)
            tvGenres.text      = game.genres.ifEmpty   { getString(R.string.not_available) }
            tvPlatforms.text   = game.platforms.ifEmpty { getString(R.string.not_available) }
            tvPlaytime.text    = if (game.playtime > 0)
                                    getString(R.string.playtime_format, game.playtime)
                                 else getString(R.string.not_available)
            tvDevelopers.text  = game.developers.ifEmpty { getString(R.string.not_available) }
            tvPublishers.text  = game.publishers.ifEmpty { getString(R.string.not_available) }
            tvDescription.text = game.description.ifEmpty { getString(R.string.no_description) }

            if (game.metacritic > 0) {
                tvMetacritic.text = game.metacritic.toString()
                layoutMetacritic.visibility = View.VISIBLE
            } else {
                layoutMetacritic.visibility = View.GONE
            }
        }
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val iconRes = if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        val tint = if (isFavorite) R.color.colorFavorite else R.color.black
        binding.btnFavoriteIcon.apply {
            setImageResource(iconRes)
            setColorFilter(resources.getColor(tint, theme))
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.contentLayout.visibility = if (loading) View.INVISIBLE else View.VISIBLE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val statusBarHeight = getStatusBarHeight()
        val margin16 = (16 * resources.displayMetrics.density).toInt()
        (binding.btnBack.layoutParams as? android.widget.FrameLayout.LayoutParams)?.apply {
            topMargin = statusBarHeight + margin16
            leftMargin = margin16
        }
        (binding.btnFavoriteIcon.layoutParams as? android.widget.FrameLayout.LayoutParams)?.apply {
            topMargin = statusBarHeight + margin16
            rightMargin = margin16
        }
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else 0
    }

    companion object {
        const val EXTRA_GAME_ID = "extra_game_id"
    }
}
