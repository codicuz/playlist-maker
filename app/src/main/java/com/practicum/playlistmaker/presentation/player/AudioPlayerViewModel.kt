package com.practicum.playlistmaker.presentation.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
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
import kotlinx.coroutines.launch

sealed class AddTrackStatus {
    data class Success(val playlistName: String) : AddTrackStatus()
    data class AlreadyExists(val playlistName: String) : AddTrackStatus()
    class Error(val message: String) : AddTrackStatus()
}

class AudioPlayerViewModel(
    private val addToFavoritesUseCase: AddToFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase,
    private val isFavoriteUseCase: IsFavoriteUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase
) : ViewModel() {

    private var audioPlayerService: AudioPlayerServiceInterface? = null

    private val _state = MutableLiveData(AudioPlayerScreenState())
    val state: LiveData<AudioPlayerScreenState> = _state

    private val _addTrackStatus = MutableLiveData<AddTrackStatus?>()
    val addTrackStatus: LiveData<AddTrackStatus> = _addTrackStatus as LiveData<AddTrackStatus>

    private val _shouldCloseBottomSheet = MutableLiveData<Boolean?>()
    val shouldCloseBottomSheet: LiveData<Boolean> = _shouldCloseBottomSheet as LiveData<Boolean>

    val playlists: LiveData<List<Playlist>> = getPlaylistsUseCase.execute().asLiveData()

    private var currentTrack: Track? = null
    private var isBound = false
    private var serviceConnection: ServiceConnection? = null

    private val updateStateCallback = { playerState: PlayerState ->
        _state.value = _state.value?.copy(
            isPlaying = playerState.isPlaying,
            currentPosition = playerState.currentPosition
        )
    }

    fun bindService(context: Context) {
        // Если уже привязаны, не привязываемся повторно
        if (isBound) return

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                audioPlayerService = (service as AudioPlayerService.AudioPlayerBinder).getService()
                isBound = true

                currentTrack?.let { track ->
                    audioPlayerService?.setTrack(
                        track,
                        track.artistsName ?: "",
                        track.trackName ?: ""
                    )

                    audioPlayerService?.getState()?.observeForever(updateStateCallback)

                    // Восстанавливаем состояние плеера
                    val serviceState = audioPlayerService?.getState()?.value
                    _state.value = _state.value?.copy(
                        isPlaying = serviceState?.isPlaying ?: false,
                        currentPosition = serviceState?.currentPosition ?: 0
                    )
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                audioPlayerService = null
                isBound = false
            }
        }

        val intent = Intent(context, AudioPlayerService::class.java).apply {
            putExtra("TRACK", currentTrack)
            putExtra("ARTIST_NAME", currentTrack?.artistsName ?: "")
            putExtra("TRACK_TITLE", currentTrack?.trackName ?: "")
        }
        context.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        if (isBound) {
            try {
                audioPlayerService?.getState()?.removeObserver(updateStateCallback)
                context.unbindService(serviceConnection!!)
            } catch (e: Exception) {
                // Игнорируем ошибку, если сервис уже не зарегистрирован
            } finally {
                audioPlayerService = null
                isBound = false
                serviceConnection = null
            }
        }
    }

    fun startForegroundMode() {
        audioPlayerService?.startForegroundMode()
    }

    fun stopForegroundMode() {
        audioPlayerService?.stopForegroundMode()
    }

    fun setTrack(track: Track) {
        currentTrack = track
        updateTrackState(track)

        if (isBound) {
            audioPlayerService?.setTrack(
                track,
                track.artistsName ?: "",
                track.trackName ?: ""
            )
        }
    }

    private fun updateTrackState(track: Track) {
        viewModelScope.launch {
            val isFav = track.trackId?.let { isFavoriteUseCase.execute(it) } ?: false
            _state.value = _state.value?.copy(
                track = track,
                isFavorite = isFav,
                currentPosition = audioPlayerService?.getState()?.value?.currentPosition ?: 0,
                isPlaying = audioPlayerService?.getState()?.value?.isPlaying ?: false
            )
        }
    }

    fun startPlayer() {
        audioPlayerService?.play()
    }

    fun pausePlayer() {
        audioPlayerService?.pause()
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

    fun resetAddTrackStatus() {
        _addTrackStatus.value = null
    }

    fun resetShouldCloseBottomSheet() {
        _shouldCloseBottomSheet.value = null
    }

    fun shouldStopOnExit(): Boolean {
        return _state.value?.isPlaying == true
    }

    fun releasePlayer() {
        audioPlayerService?.release()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerService = null
    }

    private var wasInBackground = false

    fun onAppBackgrounded() {
        wasInBackground = true
    }

    fun onAppForegrounded() {
        if (wasInBackground) {
            stopForegroundMode()
            wasInBackground = false
        }
    }
}