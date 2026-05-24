package com.deltarfd.deltagamesandroid.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.deltarfd.deltagamesandroid.R

fun ImageView.loadImage(url: String) {
    Glide.with(this.context)
        .load(url)
        .transition(DrawableTransitionOptions.withCrossFade())
        .placeholder(R.drawable.placeholder_game)
        .error(R.drawable.placeholder_game)
        .centerCrop()
        .into(this)
}
