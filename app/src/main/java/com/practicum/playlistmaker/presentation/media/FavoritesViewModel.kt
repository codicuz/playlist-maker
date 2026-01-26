package com.practicum.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.favorites.GetFavoritesUseCase
import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val getFavoritesUseCase: GetFavoritesUseCase
) : ViewModel() {

    private val _favorites = MutableLiveData<List<Track>>()
    val favorites: LiveData<List<Track>> = _favorites

    fun loadFavorites() {
        viewModelScope.launch {
            getFavoritesUseCase.execute().collect { list ->
                _favorites.value = list
            }
        }
    }
}