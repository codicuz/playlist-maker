package com.practicum.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.track.Track

class AudioPlayerViewModel(private val mediaPlayer: MediaPlayer) : ViewModel() {

    private val _state = MutableLiveData(AudioPlayerScreenState())
    val state: LiveData<AudioPlayerScreenState> = _state
    private val handler = Handler(Looper.getMainLooper())
    private var updateTimeRunnable: Runnable? = null
    private var isPrepared = false
    private var isCompleted = false

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
            startUpdatingTime()
        }
    }

    fun pausePlayer() {
        if (isPrepared && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            _state.value = _state.value?.copy(isPlaying = false)
            stopUpdatingTime()
        }
    }

    private fun startUpdatingTime() {
        stopUpdatingTime()
        updateTimeRunnable = object : Runnable {
            override fun run() {
                _state.value = _state.value?.copy(currentPosition = mediaPlayer.currentPosition)
                handler.postDelayed(this, 500)
            }
        }
        handler.post(updateTimeRunnable!!)
    }

    private fun stopUpdatingTime() {
        updateTimeRunnable?.let { handler.removeCallbacks(it) }
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
                stopUpdatingTime()
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
        stopUpdatingTime()
    }
}
