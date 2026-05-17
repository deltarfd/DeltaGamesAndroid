package com.deltarfd.deltagamesandroid.favorite.di

import com.deltarfd.deltagamesandroid.favorite.ui.FavoriteViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val favoriteModule = module {
    viewModel { FavoriteViewModel(get()) }
}
