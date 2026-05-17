package com.deltarfd.deltagamesandroid.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deltarfd.deltagamesandroid.core.domain.usecase.IGameUseCase
import com.deltarfd.deltagamesandroid.core.utils.Resource
import com.deltarfd.deltagamesandroid.presentation.mapper.GamePresentationMapper
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val gameUseCase: IGameUseCase) : ViewModel() {

    private val _gamesState = MutableStateFlow<Resource<List<GameItem>>>(Resource.Loading())
    val gamesState: StateFlow<Resource<List<GameItem>>> = _gamesState

    private val _trendingState = MutableStateFlow<Resource<List<GameItem>>>(Resource.Loading())
    val trendingState: StateFlow<Resource<List<GameItem>>> = _trendingState

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private var currentPage = 1
    private var isLoadingMoreInProgress = false
    private var hasMorePages = true

    private val allGames = mutableListOf<GameItem>()

    init {
        loadGames()
        loadTrending()
    }

    fun loadGames() {
        currentPage = 1
        hasMorePages = true
        allGames.clear()
        viewModelScope.launch {
            gameUseCase.getAllGames(page = 1).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _gamesState.value = Resource.Loading()
                    is Resource.Success -> {
                        val items = GamePresentationMapper.mapListDomainToPresentation(resource.data ?: emptyList())
                        allGames.clear()
                        allGames.addAll(items)
                        _gamesState.value = Resource.Success(allGames.toList())
                        if (items.size < 20) hasMorePages = false
                    }
                    is Resource.Error -> _gamesState.value = Resource.Error(resource.message ?: "Error")
                }
            }
        }
    }

    fun loadMoreGames() {
        if (isLoadingMoreInProgress || !hasMorePages) return
        isLoadingMoreInProgress = true
        currentPage++
        _isLoadingMore.value = true

        viewModelScope.launch {
            gameUseCase.getAllGames(page = currentPage).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        val items = GamePresentationMapper.mapListDomainToPresentation(resource.data ?: emptyList())
                        if (items.isEmpty()) {
                            hasMorePages = false
                        } else {
                            allGames.addAll(items)
                            _gamesState.value = Resource.Success(allGames.toList())
                            if (items.size < 20) hasMorePages = false
                        }
                        _isLoadingMore.value = false
                        isLoadingMoreInProgress = false
                    }
                    is Resource.Error -> {
                        currentPage--
                        _isLoadingMore.value = false
                        isLoadingMoreInProgress = false
                    }
                }
            }
        }
    }

    fun loadTrending() {
        viewModelScope.launch {
            gameUseCase.getTrendingGames().collect { resource ->
                when (resource) {
                    is Resource.Loading -> _trendingState.value = Resource.Loading()
                    is Resource.Success -> _trendingState.value = Resource.Success(
                        GamePresentationMapper.mapListDomainToPresentation(resource.data ?: emptyList())
                    )
                    is Resource.Error -> _trendingState.value = Resource.Error(resource.message ?: "Error")
                }
            }
        }
    }
}
