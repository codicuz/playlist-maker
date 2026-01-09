package com.practicum.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.track.Track

class AudioPlayerViewModel : ViewModel() {

    private val _state = MutableLiveData(AudioPlayerScreenState())
    val state: LiveData<AudioPlayerScreenState> = _state

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var updateTimeRunnable: Runnable? = null
    private var isPrepared = false

    fun setTrack(track: Track) {
        _state.value = _state.value?.copy(track = track)
        initPlayer(track.previewUrl)
    }

    fun startPlayer() {
        mediaPlayer?.let {
            if (isPrepared && !it.isPlaying) {
                it.start()
                _state.value = _state.value?.copy(isPlaying = true)
                startUpdatingTime()
            }
        }
    }

    fun pausePlayer() {
        mediaPlayer?.let {
            if (isPrepared && it.isPlaying) {
                it.pause()
                _state.value = _state.value?.copy(isPlaying = false)
                stopUpdatingTime()
            }
        }
    }

    private fun startUpdatingTime() {
        updateTimeRunnable = object : Runnable {
            override fun run() {
                _state.value = _state.value?.copy(
                    currentPosition = mediaPlayer?.currentPosition ?: 0
                )
                handler.postDelayed(this, 500)
            }
        }.also { handler.post(it) }
    }

    private fun stopUpdatingTime() {
        updateTimeRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun initPlayer(previewUrl: String?) {
        if (previewUrl.isNullOrEmpty()) return
        releasePlayer()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(previewUrl)
            setOnPreparedListener {
                isPrepared = true
                _state.value = _state.value?.copy(isPlaying = false)
            }
            setOnCompletionListener {
                pausePlayer()
                _state.value = _state.value?.copy(currentPosition = 0)
            }
            prepareAsync()
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
        stopUpdatingTime()
    }

    override fun onCleared() {
        releasePlayer()
    }
}

