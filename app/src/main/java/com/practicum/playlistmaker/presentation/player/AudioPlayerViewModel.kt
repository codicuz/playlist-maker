package com.practicum.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AudioPlayerViewModel(private val mediaPlayer: MediaPlayer) : ViewModel() {

    private val _state = MutableLiveData(AudioPlayerScreenState())
    val state: LiveData<AudioPlayerScreenState> = _state

    private var isPrepared = false
    private var isCompleted = false

    private var updateProgressJob: Job? = null

    fun setTrack(track: Track) {
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
        if (isPrepared && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            _state.value = _state.value?.copy(isPlaying = false)
            stopUpdatingProgress()
        }
    }

    private fun startUpdatingProgress() {
        stopUpdatingProgress()
        updateProgressJob = viewModelScope.launch {
            while (isActive && mediaPlayer.isPlaying) {
                _state.value = _state.value?.copy(
                    currentPosition = mediaPlayer.currentPosition
                )
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
                isCompleted = true
                stopUpdatingProgress()
                _state.value = _state.value?.copy(isPlaying = false, currentPosition = 0)
            }

            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Error init player", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            mediaPlayer.release()
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "MediaPlayer already released", e)
        }
        stopUpdatingProgress()
    }
}
