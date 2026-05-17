package com.deltarfd.deltagamesandroid.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deltarfd.deltagamesandroid.core.domain.usecase.IGameUseCase
import com.deltarfd.deltagamesandroid.core.utils.Resource
import com.deltarfd.deltagamesandroid.presentation.mapper.GamePresentationMapper
import com.deltarfd.deltagamesandroid.presentation.model.GameItem
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(private val gameUseCase: IGameUseCase) : ViewModel() {

    private val _searchState = MutableStateFlow<Resource<List<GameItem>>>(Resource.Success(emptyList()))
    val searchState: StateFlow<Resource<List<GameItem>>> = _searchState

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    val queryFlow = MutableStateFlow("")

    private var currentQuery  = ""
    private var currentPage   = 1
    private var hasMorePages  = true
    private var isLoadingMoreInProgress = false
    private val allResults = mutableListOf<GameItem>()

    init {
        viewModelScope.launch {
            queryFlow
                .debounce(500)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect { query -> newSearch(query) }
        }
    }

    fun onQueryChanged(query: String) {
        queryFlow.value = query
        if (query.isBlank()) {
            _searchState.value = Resource.Success(emptyList())
            allResults.clear()
            currentPage = 1
        }
    }

    private fun newSearch(query: String) {
        currentQuery = query
        currentPage  = 1
        hasMorePages = true
        allResults.clear()
        viewModelScope.launch {
            gameUseCase.searchGames(query, page = 1).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _searchState.value = Resource.Loading()
                    is Resource.Success -> {
                        val items = GamePresentationMapper.mapListDomainToPresentation(resource.data ?: emptyList())
                        allResults.clear()
                        allResults.addAll(items)
                        _searchState.value = Resource.Success(allResults.toList())
                        if (items.size < 20) hasMorePages = false
                    }
                    is Resource.Error -> _searchState.value = Resource.Error(resource.message ?: "Error")
                }
            }
        }
    }

    fun loadMoreResults() {
        if (isLoadingMoreInProgress || !hasMorePages || currentQuery.isBlank()) return
        isLoadingMoreInProgress = true
        currentPage++
        _isLoadingMore.value = true

        viewModelScope.launch {
            gameUseCase.searchGames(currentQuery, page = currentPage).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        val items = GamePresentationMapper.mapListDomainToPresentation(resource.data ?: emptyList())
                        if (items.isEmpty()) {
                            hasMorePages = false
                        } else {
                            allResults.addAll(items)
                            _searchState.value = Resource.Success(allResults.toList())
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
}
