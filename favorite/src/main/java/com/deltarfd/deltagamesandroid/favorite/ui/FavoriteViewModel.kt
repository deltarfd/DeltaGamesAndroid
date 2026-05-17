package com.deltarfd.deltagamesandroid.favorite.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deltarfd.deltagamesandroid.core.domain.usecase.IGameUseCase
import com.deltarfd.deltagamesandroid.presentation.mapper.GamePresentationMapper
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(private val gameUseCase: IGameUseCase) : ViewModel() {

    private val _favoriteGames = MutableStateFlow<List<GameItem>>(emptyList())
    val favoriteGames: StateFlow<List<GameItem>> = _favoriteGames

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            gameUseCase.getFavoriteGames().collect { games ->
                _favoriteGames.value = GamePresentationMapper.mapListDomainToPresentation(games)
            }
        }
    }
}
