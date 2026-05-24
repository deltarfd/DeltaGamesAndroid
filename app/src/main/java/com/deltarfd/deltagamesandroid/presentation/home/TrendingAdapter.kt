package com.deltarfd.deltagamesandroid.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.deltarfd.deltagamesandroid.databinding.ItemTrendingBinding
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import com.deltarfd.deltagamesandroid.util.loadImage

class TrendingAdapter(
    private val onItemClick: (GameItem) -> Unit
) : ListAdapter<GameItem, TrendingAdapter.TrendingViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingViewHolder {
        val binding = ItemTrendingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrendingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrendingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TrendingViewHolder(private val binding: ItemTrendingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(game: GameItem) {
            with(binding) {
                tvGameName.text = game.name
                tvRating.text = game.ratingDisplay

                val genre = game.genres.split(",").firstOrNull()?.trim() ?: ""
                tvGenre.text = genre
                tvGenre.isVisible = genre.isNotEmpty()

                val platform = formatPlatforms(game.platforms)
                tvPlatform.text = platform
                tvPlatform.isVisible = platform.isNotEmpty()

                val date = game.releaseDate
                tvDate.text = date
                tvDate.isVisible = date.isNotEmpty()

                ivGameCover.loadImage(game.coverUrl)

                root.setOnClickListener { onItemClick(game) }
            }
        }
    }

    companion object {
        fun formatPlatforms(platforms: String): String {
            if (platforms.isBlank()) return ""
            return platforms.split(",")
                .asSequence()
                .map { it.trim() }
                .map { p ->
                    when {
                        p.contains("PlayStation", ignoreCase = true) -> "PlayStation"
                        p.contains("Xbox", ignoreCase = true) -> "Xbox"
                        p.contains("Nintendo", ignoreCase = true) -> "Nintendo"
                        p.contains("Android", ignoreCase = true) -> "Android"
                        p.contains("iOS", ignoreCase = true) -> "iOS"
                        p.contains("macOS", ignoreCase = true) || p.contains("Mac", ignoreCase = true) -> "macOS"
                        p.contains("Linux", ignoreCase = true) -> "Linux"
                        p.contains("PC", ignoreCase = true) || p.contains("Windows", ignoreCase = true) -> "PC"
                        else -> p
                    }
                }
                .distinct()
                .take(4)
                .joinToString(", ")
        }

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GameItem>() {
            override fun areItemsTheSame(oldItem: GameItem, newItem: GameItem) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: GameItem, newItem: GameItem) =
                oldItem == newItem
        }
    }
}
