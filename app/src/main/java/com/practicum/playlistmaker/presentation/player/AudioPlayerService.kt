package com.practicum.playlistmaker.presentation.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.domain.track.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "AudioPlayerService"

interface AudioPlayerServiceInterface {
    fun setTrack(track: Track, artistName: String, trackTitle: String)
    fun play()
    fun pause()
    fun getState(): LiveData<PlayerState>
    fun startForegroundMode()
    fun stopForegroundMode()
    fun isPlaying(): Boolean
    fun getCurrentTrackId(): Int?
    fun isPrepared(): Boolean
    fun reset()
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0,
    val isPrepared: Boolean = false,
    val isCompleted: Boolean = false,
    val currentTrack: Track? = null
)

class AudioPlayerService : Service(), AudioPlayerServiceInterface {

    private val binder = AudioPlayerBinder()
    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private val _state = MutableLiveData(PlayerState())
    override fun getState(): LiveData<PlayerState> = _state

    private var updateProgressJob: Job? = null
    private var currentTrack: Track? = null
    private var artistName: String = ""
    private var trackTitle: String = ""
    private var isForeground = false

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "playback_channel"
    }

    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerServiceInterface = this@AudioPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaPlayer()
    }

    override fun reset() {
        try {
            if (_state.value?.isPrepared == true) {
                mediaPlayer.seekTo(0)
            }
            _state.value = _state.value?.copy(
                currentPosition = 0, isPlaying = false, isCompleted = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting player", e)
        }
    }

    private fun initMediaPlayer() {
        try {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build()
            )

            mediaPlayer.setOnPreparedListener {
                _state.postValue(_state.value?.copy(isPrepared = true))
            }

            mediaPlayer.setOnCompletionListener {
                stopUpdatingProgress()
                _state.postValue(_state.value?.copy(
                    isPlaying = false, currentPosition = 0, isCompleted = true
                ))
                stopForegroundMode()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MediaPlayer", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (_state.value?.isPlaying != true) {
            stopSelf()
        }
        return true
    }

    override fun setTrack(track: Track, artistName: String, trackTitle: String) {

        this.artistName = artistName
        this.trackTitle = trackTitle
        this.currentTrack = track

        _state.value = _state.value?.copy(
            currentTrack = track,
            isPlaying = false,
            currentPosition = 0,
            isPrepared = false,
            isCompleted = false
        )

        track.previewUrl?.let { previewUrl ->
            try {
                stopUpdatingProgress()
                mediaPlayer.reset()
                mediaPlayer.setDataSource(previewUrl)
                mediaPlayer.prepareAsync()
            } catch (e: Exception) {
                Log.e(TAG, "Error setting track", e)
            }
        }
    }

    override fun play() {
        try {
            if (_state.value?.isPrepared != true) {
                return
            }

            val isCurrentlyPlaying = try {
                mediaPlayer.isPlaying
            } catch (e: IllegalStateException) {
                false
            }

            if (!isCurrentlyPlaying) {
                if (_state.value?.isCompleted == true) {
                    mediaPlayer.seekTo(0)
                    _state.value = _state.value?.copy(isCompleted = false)
                }
                mediaPlayer.start()
                _state.value = _state.value?.copy(isPlaying = true)
                startUpdatingProgress()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in play()", e)
        }
    }

    override fun pause() {
        try {
            val isCurrentlyPlaying = try {
                mediaPlayer.isPlaying
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Error checking isPlaying", e)
                false
            }

            if (isCurrentlyPlaying) {
                mediaPlayer.pause()
                _state.value = _state.value?.copy(isPlaying = false)
                stopUpdatingProgress()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in pause()", e)
        }
    }

    override fun isPlaying(): Boolean {
        return try {
            if (_state.value?.isPrepared == true) {
                mediaPlayer.isPlaying
            } else false
        } catch (e: IllegalStateException) {
            false
        }
    }

    override fun getCurrentTrackId(): Int? = currentTrack?.trackId

    override fun isPrepared(): Boolean = _state.value?.isPrepared == true

    override fun startForegroundMode() {
        if (_state.value?.isPlaying == true && !isForeground) {
            try {
                val notification = createNotification()

                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                        startForeground(
                            NOTIFICATION_ID,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        )
                    }

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        startForeground(
                            NOTIFICATION_ID,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        )
                    }

                    else -> {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                }
                isForeground = true
            } catch (e: Exception) {
                Log.e(TAG, "Error starting foreground", e)
            }
        }
    }

    override fun stopForegroundMode() {
        if (isForeground) {
            try {
                stopForeground(true)
                isForeground = false
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping foreground", e)
            }
        }
    }

    private fun createNotification(): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("$artistName - $trackTitle")
            .setSmallIcon(android.R.drawable.ic_media_play).setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setOngoing(true).build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.playing_track), NotificationManager.IMPORTANCE_LOW
            ).apply {
                setSound(null, null)
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startUpdatingProgress() {
        stopUpdatingProgress()
        updateProgressJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                try {
                    if (_state.value?.isPlaying == true) {
                        val position = try {
                            mediaPlayer.currentPosition
                        } catch (e: IllegalStateException) {
                            _state.value?.currentPosition ?: 0
                        }
                        _state.value = _state.value?.copy(currentPosition = position)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating progress", e)
                }
                delay(300)
            }
        }
    }

    private fun stopUpdatingProgress() {
        updateProgressJob?.cancel()
        updateProgressJob = null
    }

    override fun onDestroy() {
        stopUpdatingProgress()
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing media player", e)
        }
        super.onDestroy()
    }
}