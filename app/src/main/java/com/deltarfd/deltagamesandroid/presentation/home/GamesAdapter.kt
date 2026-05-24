package com.deltarfd.deltagamesandroid.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.deltarfd.deltagamesandroid.databinding.ItemGameBinding
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import com.deltarfd.deltagamesandroid.util.loadImage

class GamesAdapter(
    private val onItemClick: (GameItem) -> Unit
) : ListAdapter<GameItem, GamesAdapter.GameViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GameViewHolder(private val binding: ItemGameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(game: GameItem) {
            with(binding) {
                tvGameName.text = game.name
                tvRating.text = game.ratingDisplay

                val genre = game.genres.split(",").firstOrNull()?.trim() ?: ""
                tvGenre.text = genre
                tvGenre.isVisible = genre.isNotEmpty()

                val platform = TrendingAdapter.formatPlatforms(game.platforms)
                tvPlatform.text = platform
                tvPlatform.isVisible = platform.isNotEmpty()

                val date = game.releaseDate
                tvReleaseDate.text = date
                tvReleaseDate.isVisible = date.isNotEmpty()

                ivGameCover.loadImage(game.coverUrl)

                root.setOnClickListener { onItemClick(game) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GameItem>() {
            override fun areItemsTheSame(oldItem: GameItem, newItem: GameItem) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: GameItem, newItem: GameItem) =
                oldItem == newItem
        }
    }
}
