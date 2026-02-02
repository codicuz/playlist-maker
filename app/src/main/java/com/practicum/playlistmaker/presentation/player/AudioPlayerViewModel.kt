package com.practicum.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.favorites.AddToFavoritesUseCase
import com.practicum.playlistmaker.domain.favorites.IsFavoriteUseCase
import com.practicum.playlistmaker.domain.favorites.RemoveFromFavoritesUseCase
import com.practicum.playlistmaker.domain.playlist.AddTrackResult
import com.practicum.playlistmaker.domain.playlist.AddTrackToPlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.GetPlaylistsUseCase
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class AddTrackStatus {
    data class Success(val playlistName: String) : AddTrackStatus()
    data class AlreadyExists(val playlistName: String) : AddTrackStatus()
    class Error(val message: String) : AddTrackStatus()
}

class AudioPlayerViewModel(
    private val mediaPlayer: MediaPlayer,
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase
) : ViewModel() {

    private val _state = MutableLiveData(AudioPlayerScreenState())
    val state: LiveData<AudioPlayerScreenState> = _state

    private var currentTrack: Track? = null
    private var isPrepared = false
    private var isCompleted = false
    private var updateProgressJob: Job? = null

    private var wasPlayingBeforeConfigChange = false
    private var lastPositionBeforeConfigChange = 0

    val playlists: LiveData<List<Playlist>> = getPlaylistsUseCase.execute().asLiveData()

    private val _addTrackStatus = MutableLiveData<AddTrackStatus?>()
    val addTrackStatus: LiveData<AddTrackStatus> = _addTrackStatus as LiveData<AddTrackStatus>

    private val _shouldCloseBottomSheet = MutableLiveData<Boolean?>()
    val shouldCloseBottomSheet: LiveData<Boolean> = _shouldCloseBottomSheet as LiveData<Boolean>

    fun setTrack(track: Track) {
        if (currentTrack?.trackId == track.trackId && isPrepared) {
            updateTrackState(track)
            if (wasPlayingBeforeConfigChange && lastPositionBeforeConfigChange > 0) {
                mediaPlayer.seekTo(lastPositionBeforeConfigChange)
                if (wasPlayingBeforeConfigChange && !mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                    _state.value = _state.value?.copy(isPlaying = true, currentPosition = lastPositionBeforeConfigChange)
                    startUpdatingProgress()
                }
                wasPlayingBeforeConfigChange = false
                lastPositionBeforeConfigChange = 0
            }
            return
        }

        stopPlayer()

        wasPlayingBeforeConfigChange = _state.value?.isPlaying == true
        lastPositionBeforeConfigChange = _state.value?.currentPosition ?: 0

        currentTrack = track
        updateTrackState(track)

        track.previewUrl?.let { initPlayer(it) }
    }

    private fun updateTrackState(track: Track) {
        viewModelScope.launch {
            val isFav = track.trackId?.let { isFavoriteUseCase.execute(it) } ?: false
            _state.value = _state.value?.copy(
                track = track,
                isFavorite = isFav,
                currentPosition = 0
            )
        }
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
        } else if (!isPrepared) {
            currentTrack?.previewUrl?.let {
                initPlayer(it)
                viewModelScope.launch {
                    delay(100)
                    if (isPrepared) {
                        startPlayer()
                    }
                }
            }
        }
    }

    fun resetAddTrackStatus() {
        _addTrackStatus.value = null
    }

    fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        if (playlist.trackIds.contains(track.trackId)) {
            _addTrackStatus.value = AddTrackStatus.AlreadyExists(playlist.title)
            _shouldCloseBottomSheet.value = false
            return
        }

        viewModelScope.launch {
            when (val result = addTrackToPlaylistUseCase.execute(playlist.id, track)) {
                is AddTrackResult.Success -> {
                    _addTrackStatus.value = AddTrackStatus.Success(result.playlistName)
                    _shouldCloseBottomSheet.value = true
                    loadPlaylists()
                }

                is AddTrackResult.AlreadyExists -> {
                    _addTrackStatus.value = AddTrackStatus.AlreadyExists(result.playlistName)
                    _shouldCloseBottomSheet.value = false
                }

                is AddTrackResult.Error -> {
                    _addTrackStatus.value = AddTrackStatus.Error(result.message)
                    _shouldCloseBottomSheet.value = true
                }
            }
        }
    }

    fun resetShouldCloseBottomSheet() {
        _shouldCloseBottomSheet.value = null
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            getPlaylistsUseCase.execute().collect {}
        }
    }

    fun pausePlayer() {
        if (isPrepared && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            _state.value = _state.value?.copy(isPlaying = false)
            stopUpdatingProgress()
        }
    }

    private fun stopPlayer() {
        if (isPrepared) {
            try {
                mediaPlayer.stop()
            } catch (e: IllegalStateException) {
            }
        }
        stopUpdatingProgress()
        isPrepared = false
        isCompleted = false
    }

    private fun startUpdatingProgress() {
        stopUpdatingProgress()
        updateProgressJob = viewModelScope.launch {
            while (isActive) {
                val position = try {
                    if (isPrepared && mediaPlayer.isPlaying) {
                        mediaPlayer.currentPosition
                    } else {
                        _state.value?.currentPosition ?: 0
                    }
                } catch (e: IllegalStateException) {
                    _state.value?.currentPosition ?: 0
                }
                _state.value = _state.value?.copy(currentPosition = position)
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
            stopUpdatingProgress()

            mediaPlayer.reset()
            isPrepared = false
            isCompleted = false

            mediaPlayer.setDataSource(previewUrl)

            mediaPlayer.setOnPreparedListener {
                isPrepared = true
                if (wasPlayingBeforeConfigChange) {
                    mediaPlayer.seekTo(lastPositionBeforeConfigChange)
                    mediaPlayer.start()
                    _state.value = _state.value?.copy(isPlaying = true, currentPosition = lastPositionBeforeConfigChange)
                    startUpdatingProgress()
                    wasPlayingBeforeConfigChange = false
                    lastPositionBeforeConfigChange = 0
                }
            }

            mediaPlayer.setOnCompletionListener {
                stopUpdatingProgress()
                isCompleted = true
                _state.value = _state.value?.copy(isPlaying = false, currentPosition = 0)
            }

            mediaPlayer.setOnErrorListener { mp, what, extra ->
                Log.e("AudioPlayerViewModel", "MediaPlayer error: what=$what, extra=$extra")
                false
            }

            mediaPlayer.prepareAsync()

        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Error init player", e)
            isPrepared = false
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentFav = _state.value?.isFavorite ?: false
            currentTrack?.trackId?.let { id ->
                if (currentFav) removeFromFavoritesUseCase.execute(id)
                else addToFavoritesUseCase.execute(currentTrack!!)
                _state.value = _state.value?.copy(isFavorite = !currentFav)
            }
        }
    }

    fun savePlaybackState() {
        wasPlayingBeforeConfigChange = _state.value?.isPlaying == true
        lastPositionBeforeConfigChange = _state.value?.currentPosition ?: 0
    }

    override fun onCleared() {
        super.onCleared()
        stopUpdatingProgress()
        try {
            mediaPlayer.release()
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Error releasing MediaPlayer", e)
        }
    }
}