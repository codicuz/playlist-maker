package com.practicum.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.track.Track

class AudioPlayerViewModel : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var updateTimeRunnable: Runnable? = null

    private val _trackLiveData = MutableLiveData<Track>()
    val trackLiveData: LiveData<Track> = _trackLiveData

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData(0)
    val currentPosition: LiveData<Int> = _currentPosition

    private var isPrepared = false

    fun setTrack(track: Track) {
        _trackLiveData.value = track
        initPlayer(track.previewUrl)
    }

    private fun initPlayer(previewUrl: String?) {
        if (previewUrl.isNullOrEmpty()) return

        releasePlayer()

        mediaPlayer = MediaPlayer().apply {
            setDataSource(previewUrl)
            setOnPreparedListener {
                isPrepared = true
                _isPlaying.value = false
            }
            setOnCompletionListener {
                stopPlayer()
            }
            prepareAsync()
        }
    }

    fun startPlayer() {
        mediaPlayer?.let {
            if (isPrepared && !it.isPlaying) {
                it.start()
                _isPlaying.value = true
                startUpdatingTime()
            }
        }
    }

    fun pausePlayer() {
        mediaPlayer?.let {
            if (isPrepared && it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                stopUpdatingTime()
            }
        }
    }

    private fun stopPlayer() {
        mediaPlayer?.let {
            if (isPrepared) {
                it.pause()
                it.seekTo(0)
                _isPlaying.value = false
                _currentPosition.value = 0
                stopUpdatingTime()
            }
        }
    }

    private fun startUpdatingTime() {
        updateTimeRunnable = object : Runnable {
            override fun run() {
                _currentPosition.value = mediaPlayer?.currentPosition ?: 0
                handler.postDelayed(this, 500)
            }
        }.also { handler.post(it) }
    }

    private fun stopUpdatingTime() {
        updateTimeRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
        stopUpdatingTime()
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}
