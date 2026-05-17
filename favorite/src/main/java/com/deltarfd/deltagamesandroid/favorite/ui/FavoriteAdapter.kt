package com.deltarfd.deltagamesandroid.favorite.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.deltarfd.deltagamesandroid.favorite.databinding.ItemFavoriteBinding
import com.deltarfd.deltagamesandroid.presentation.model.GameItem

class FavoriteAdapter(
    private val onItemClick: (GameItem) -> Unit
) : ListAdapter<GameItem, FavoriteAdapter.FavoriteViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavoriteViewHolder(private val binding: ItemFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(game: GameItem) {
            with(binding) {
                tvGameName.text = game.name
                tvRating.text = game.ratingDisplay

                val genre = game.genres.split(",").firstOrNull()?.trim() ?: ""
                tvGenre.text = genre
                tvGenre.visibility = if (genre.isNotEmpty()) View.VISIBLE else View.GONE

                val date = game.releaseDate
                tvReleaseDate.text = date
                tvReleaseDate.visibility = if (date.isNotEmpty()) View.VISIBLE else View.GONE

                Glide.with(itemView.context)
                    .load(game.coverUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(ivGameCover)

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
