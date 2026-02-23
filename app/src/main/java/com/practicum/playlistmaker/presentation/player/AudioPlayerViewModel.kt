package com.practicum.playlistmaker.presentation.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.favorites.AddToFavoritesUseCase
import com.practicum.playlistmaker.domain.favorites.IsFavoriteUseCase
import com.practicum.playlistmaker.domain.favorites.RemoveFromFavoritesUseCase
import com.practicum.playlistmaker.domain.history.GetSearchHistoryUseCase
import com.practicum.playlistmaker.domain.playlist.AddTrackResult
import com.practicum.playlistmaker.domain.playlist.AddTrackToPlaylistUseCase
import com.practicum.playlistmaker.domain.playlist.GetPlaylistsUseCase
import com.practicum.playlistmaker.domain.playlist.Playlist
import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "AudioPlayerViewModel"

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
    private val addTrackToPlaylistUseCase: AddTrackToPlaylistUseCase,
    private val getSearchHistoryUseCase: GetSearchHistoryUseCase
) : ViewModel() {

    private var audioPlayerService: AudioPlayerServiceInterface? = null

    private val _state = MutableStateFlow(AudioPlayerScreenState())
    val state: StateFlow<AudioPlayerScreenState> = _state.asStateFlow()

    private val _addTrackStatus = MutableSharedFlow<AddTrackStatus?>()
    val addTrackStatus: SharedFlow<AddTrackStatus?> = _addTrackStatus.asSharedFlow()

    private val _shouldCloseBottomSheet = MutableStateFlow<Boolean?>(null)
    val shouldCloseBottomSheet: StateFlow<Boolean?> = _shouldCloseBottomSheet.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private var currentTrack: Track? = null
    private var isBound = false
    private var serviceConnection: ServiceConnection? = null
    private var wasInBackground = false
    private var pendingStartPlayer = false
    private var pollJob: Job? = null

    private var isConfigurationChange = false

    init {
        loadPlaylists()
    }

    fun onConfigurationChange() {
        isConfigurationChange = true
    }

    private fun loadPlaylists() {
        viewModelScope.launch {
            getPlaylistsUseCase.execute().collect { list ->
                _playlists.value = list
            }
        }
    }

    fun loadTrack(trackId: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading track with ID: $trackId")

                val history = getSearchHistoryUseCase.execute()
                val trackFromHistory = history.find { it.trackId == trackId }

                if (trackFromHistory != null) {
                    Log.d(TAG, "Track found in history: ${trackFromHistory.trackName}")
                    setTrack(trackFromHistory)
                } else {
                    Log.d(TAG, "Track not found in history, creating test track")
                    val testTrack = Track(
                        id = trackId.toLong(),
                        trackId = trackId,
                        trackName = "Тестовый трек",
                        artistsName = "Тестовый исполнитель",
                        trackTimeMillis = 180000,
                        artworkUrl100 = "",
                        previewUrl = null,
                        collectionName = "Тестовый альбом",
                        releaseDate = "2023-01-01T00:00:00Z",
                        primaryGenreName = "Test",
                        country = "Test"
                    )
                    setTrack(testTrack)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading track", e)
            }
        }
    }

    fun bindService(context: Context) {
        if (isBound && audioPlayerService != null) {
            updateStateFromService()
            if (pendingStartPlayer) {
                pendingStartPlayer = false
                startPlayer()
            }
            return
        }

        if (serviceConnection != null) {
            return
        }

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                audioPlayerService = (service as AudioPlayerService.AudioPlayerBinder).getService()
                isBound = true

                currentTrack?.let { track ->
                    if (audioPlayerService?.getCurrentTrackId() != track.trackId) {
                        audioPlayerService?.setTrack(
                            track, track.artistsName ?: "", track.trackName ?: ""
                        )
                    }
                }

                updateStateFromService()
                startPolling()

                if (pendingStartPlayer) {
                    pendingStartPlayer = false
                    startPlayer()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                audioPlayerService = null
                isBound = false
                stopPolling()
            }
        }

        val intent = Intent(context, AudioPlayerService::class.java)
        context.bindService(intent, serviceConnection!!, Context.BIND_AUTO_CREATE)
    }

    fun unbindService(context: Context) {
        stopPolling()
        if (isBound && serviceConnection != null) {
            try {
                context.unbindService(serviceConnection!!)
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "Service already unregistered")
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service", e)
            } finally {
                audioPlayerService = null
                isBound = false
                serviceConnection = null
                pendingStartPlayer = false
            }
        }
    }

    fun stopAndUnbindService(context: Context) {
        audioPlayerService?.pause()
        audioPlayerService?.reset()
        audioPlayerService?.stopForegroundMode()
        unbindService(context)
    }

    private fun updateStateFromService() {
        audioPlayerService?.let { service ->
            try {
                val serviceState = service.getState().value
                serviceState?.let { state ->
                    _state.update { currentState ->
                        currentState.copy(
                            isPlaying = state.isPlaying, currentPosition = state.currentPosition
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating state from service", e)
            }
        }
    }

    private fun startPolling() {
        stopPolling()
        pollJob = viewModelScope.launch {
            while (true) {
                delay(300)
                if (isBound) {
                    updateStateFromService()
                } else {
                    break
                }
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun setTrack(track: Track) {
        Log.d(TAG, "Setting track: ${track.trackName}")
        currentTrack = track

        _state.update {
            AudioPlayerScreenState(
                track = track, isPlaying = false, currentPosition = 0, isFavorite = false
            )
        }

        viewModelScope.launch {
            val isFav = track.trackId?.let { isFavoriteUseCase.execute(it) } ?: false
            _state.update { it.copy(isFavorite = isFav) }
        }

        if (isBound && audioPlayerService != null) {
            audioPlayerService?.setTrack(
                track, track.artistsName ?: "", track.trackName ?: ""
            )
        } else {
            Log.d(TAG, "Service not ready, track will be set when connected")
        }
    }

    private fun updateTrackState(track: Track) {
        viewModelScope.launch {
            val isFav = track.trackId?.let { isFavoriteUseCase.execute(it) } ?: false
            _state.update { it.copy(isFavorite = isFav) }
        }
    }

    fun updateTrackIfNeeded(track: Track) {
        if (currentTrack?.trackId != track.trackId) {
            currentTrack = track

            viewModelScope.launch {
                val isFav = track.trackId?.let { isFavoriteUseCase.execute(it) } ?: false
                _state.update {
                    it.copy(
                        track = track, isFavorite = isFav
                    )
                }
            }

            if (isBound && audioPlayerService != null) {
                audioPlayerService?.setTrack(
                    track, track.artistsName ?: "", track.trackName ?: ""
                )
            }
        } else {
            updateStateFromService()
        }
    }

    fun startPlayer() {
        if (!isServiceReady()) {
            Log.e(TAG, "Cannot start player - service is null")
            pendingStartPlayer = true
            return
        }

        try {
            audioPlayerService?.play()
            pendingStartPlayer = false

            updateStateFromService()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting player", e)
            pendingStartPlayer = true
        }
    }

    fun pausePlayer() {
        if (!isServiceReady()) {
            Log.e(TAG, "Cannot pause player - service is null")
            return
        }

        try {
            audioPlayerService?.pause()
            pendingStartPlayer = false

            updateStateFromService()
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing player", e)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentFav = _state.value.isFavorite
            currentTrack?.trackId?.let { id ->
                if (currentFav) {
                    removeFromFavoritesUseCase.execute(id)
                } else {
                    currentTrack?.let { addToFavoritesUseCase.execute(it) }
                }
                _state.update { it.copy(isFavorite = !currentFav) }
            }
        }
    }

    fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        if (playlist.trackIds.contains(track.trackId)) {
            viewModelScope.launch {
                _addTrackStatus.emit(AddTrackStatus.AlreadyExists(playlist.title))
            }
            _shouldCloseBottomSheet.value = false
            return
        }

        viewModelScope.launch {
            when (val result = addTrackToPlaylistUseCase.execute(playlist.id, track)) {
                is AddTrackResult.Success -> {
                    _addTrackStatus.emit(AddTrackStatus.Success(result.playlistName))
                    _shouldCloseBottomSheet.value = true
                    loadPlaylists()
                }

                is AddTrackResult.AlreadyExists -> {
                    _addTrackStatus.emit(AddTrackStatus.AlreadyExists(result.playlistName))
                    _shouldCloseBottomSheet.value = false
                }

                is AddTrackResult.Error -> {
                    _addTrackStatus.emit(AddTrackStatus.Error(result.message))
                    _shouldCloseBottomSheet.value = true
                }
            }
        }
    }

    fun resetShouldCloseBottomSheet() {
        _shouldCloseBottomSheet.value = null
    }

    fun resetAddTrackStatus() {
        viewModelScope.launch {
            _addTrackStatus.emit(null)
        }
    }

    fun startForegroundMode() {
        try {
            audioPlayerService?.startForegroundMode()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground", e)
        }
    }

    fun stopForegroundMode() {
        try {
            audioPlayerService?.stopForegroundMode()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping foreground", e)
        }
    }

    fun cleanup() {
        stopPolling()
        audioPlayerService = null
        isBound = false
        serviceConnection = null
        pendingStartPlayer = false
    }

    fun onAppBackgrounded() {
        wasInBackground = true
        if (_state.value.isPlaying && !isConfigurationChange) {
            startForegroundMode()
        }
    }

    fun onConfigurationChangeFinished() {
        isConfigurationChange = false
    }


    fun onAppForegrounded() {
        if (wasInBackground) {
            stopForegroundMode()
            wasInBackground = false
        }
    }

    fun isServiceReady(): Boolean {
        return isBound && audioPlayerService != null
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}