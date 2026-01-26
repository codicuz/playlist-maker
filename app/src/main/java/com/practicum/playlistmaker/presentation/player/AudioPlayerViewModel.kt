package com.practicum.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.favorites.AddToFavoritesUseCase
import com.practicum.playlistmaker.domain.favorites.IsFavoriteUseCase
import com.practicum.playlistmaker.domain.favorites.RemoveFromFavoritesUseCase
import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerViewModel(
    private val mediaPlayer: MediaPlayer,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase
) : ViewModel() {

    private val _state = MutableLiveData(AudioPlayerScreenState())
    val state: LiveData<AudioPlayerScreenState> = _state

    private lateinit var track: Track

    private var isPrepared = false
    private var isCompleted = false

    private var updateProgressJob: Job? = null

    fun setTrack(track: Track) {
        this.track = track

        viewModelScope.launch {
            val isFav = track.trackId?.let { isFavoriteUseCase.execute(it) } ?: false
            _state.update { it.copy(isFavorite = isFav) }
        }

        if (_state.value?.track?.trackId == track.trackId) return
        _state.value = _state.value?.copy(track = track)
        track.previewUrl?.let { initPlayer(it) }
    }

    fun startPlayer() {
        if (isPrepared && !mediaPlayer.isPlaying) {
            if (isCompleted) {
                mediaPlayer.seekTo(0)
                isCompleted = false
            }
            mediaPlayer.start()
            _state.value = _state.value?.copy(isPlaying = true)
            startUpdatingProgress()
        }
    }

    fun pausePlayer() {
        if (isPrepared) {
            mediaPlayer.pause()
            _state.value = _state.value?.copy(isPlaying = false)
            stopUpdatingProgress()
        }
    }

    private fun startUpdatingProgress() {
        stopUpdatingProgress()
        updateProgressJob = viewModelScope.launch {
            while (isActive) {
                _state.value = _state.value?.copy(
                    currentPosition = mediaPlayer.currentPosition
                )
//                Log.d("CURR", "${_state.value?.currentPosition}")
                delay(300)
            }
        }
    }

    private fun stopUpdatingProgress() {
        updateProgressJob?.cancel()
        updateProgressJob = null
    }

    private fun initPlayer(previewUrl: String) {
        try {
            mediaPlayer.reset()
            isPrepared = false
            isCompleted = false

            mediaPlayer.setDataSource(previewUrl)

            mediaPlayer.setOnPreparedListener {
                isPrepared = true
                _state.value = _state.value?.copy(isPlaying = false, currentPosition = 0)
            }

            mediaPlayer.setOnCompletionListener {
                stopUpdatingProgress()
                isCompleted = true
                _state.value = _state.value?.copy(
                    isPlaying = false, currentPosition = 0
                )
            }

            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Error init player", e)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentFav = _state.value?.isFavorite ?: false
            track.trackId?.let { id ->
                if (currentFav) {
                    removeFromFavoritesUseCase.execute(id)
                } else {
                    addToFavoritesUseCase.execute(track)
                }
                _state.value = _state.value?.copy(isFavorite = !currentFav)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopUpdatingProgress()
        mediaPlayer.release()
    }

    private fun <T> MutableLiveData<T>.update(block: (T) -> T) {
        value = value?.let(block)
    }
}
