package com.deltarfd.deltagamesandroid.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deltarfd.deltagamesandroid.core.domain.usecase.IGameUseCase
import com.deltarfd.deltagamesandroid.core.utils.Resource
import com.deltarfd.deltagamesandroid.presentation.mapper.GamePresentationMapper
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(private val gameUseCase: IGameUseCase) : ViewModel() {

    private val _detailState = MutableStateFlow<Resource<GameItem>>(Resource.Loading())
    val detailState: StateFlow<Resource<GameItem>> = _detailState

    // Dedicated StateFlow for the heart icon — decoupled from detail re-loads
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    private var detailJob: Job? = null
    private var gameLoaded = false  // Guard: stop re-emitting after first success

    fun loadDetail(id: Int) {
        detailJob?.cancel()
        gameLoaded = false
        detailJob = viewModelScope.launch {
            gameUseCase.getGameDetail(id).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        if (!gameLoaded) _detailState.value = Resource.Loading()
                    }
                    is Resource.Success -> {
                        if (!gameLoaded) {
                            gameLoaded = true
                            val item = GamePresentationMapper.mapDomainToPresentation(resource.data!!)
                            _detailState.value = Resource.Success(item)
                            _isFavorite.value = item.isFavorite
                            // Cancel collection — prevents DB-write re-emissions from flickering the icon
                            detailJob?.cancel()
                        }
                    }
                    is Resource.Error -> {
                        _detailState.value = Resource.Error(resource.message ?: "Error")
                    }
                }
            }
        }
    }

    fun toggleFavorite(game: GameItem) {
        val newState = !game.isFavorite
        _isFavorite.value = newState
        viewModelScope.launch {
            val domainGame = GamePresentationMapper.mapPresentationToDomain(game)
            gameUseCase.setFavoriteGame(domainGame, newState)
            val current = _detailState.value
            if (current is Resource.Success) {
                _detailState.value = Resource.Success(current.data!!.copy(isFavorite = newState))
            }
        }
    }
}
