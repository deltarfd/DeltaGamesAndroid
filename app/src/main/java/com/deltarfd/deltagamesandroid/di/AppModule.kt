package com.deltarfd.deltagamesandroid.di

import com.deltarfd.deltagamesandroid.core.domain.usecase.GameInteractor
import com.deltarfd.deltagamesandroid.core.domain.usecase.IGameUseCase
import com.deltarfd.deltagamesandroid.presentation.detail.DetailViewModel
import com.deltarfd.deltagamesandroid.presentation.home.HomeViewModel
import com.deltarfd.deltagamesandroid.presentation.search.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val useCaseModule = module {
    factory<IGameUseCase> { GameInteractor(get()) }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { DetailViewModel(get()) }
    viewModel { SearchViewModel(get()) }
}
