package com.practicum.playlistmaker.presentation.player

import android.app.*
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
import kotlinx.coroutines.*

private const val TAG = "AudioPlayerService"

interface AudioPlayerServiceInterface {
    fun setTrack(track: Track, artistName: String, trackTitle: String)
    fun play()
    fun pause()
    fun getState(): LiveData<PlayerState>
    fun startForegroundMode()
    fun stopForegroundMode()
    fun release()
    fun isPlaying(): Boolean
    fun removeStateObserver(observer: androidx.lifecycle.Observer<PlayerState>)
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
    private var _state = MutableLiveData(PlayerState())
    override fun getState(): LiveData<PlayerState> = _state

    private var updateProgressJob: Job? = null
    private var currentTrack: Track? = null
    private var artistName: String = ""
    private var trackTitle: String = ""

    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "playback_channel"

    private var isForeground = false
    private var isReleased = false

    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerServiceInterface = this@AudioPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )

        mediaPlayer.setOnPreparedListener {
            _state.value = _state.value?.copy(isPrepared = true)
        }

        mediaPlayer.setOnCompletionListener {
            stopUpdatingProgress()
            _state.value = _state.value?.copy(
                isPlaying = false,
                currentPosition = 0,
                isCompleted = true
            )
            stopForegroundMode()

            if (isForeground) {
                stopForeground(true)
                isForeground = false
            }
        }

        mediaPlayer.setOnErrorListener { mp, what, extra ->
            Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
            false
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        intent?.let {
            artistName = it.getStringExtra("ARTIST_NAME") ?: ""
            trackTitle = it.getStringExtra("TRACK_TITLE") ?: ""
            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelableExtra("TRACK", Track::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.getParcelableExtra("TRACK")
            }
            track?.let { setTrack(it, artistName, trackTitle) }
        }
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (_state.value?.isPlaying != true) {
            stopForegroundMode()
            stopSelf()
        }
        return true
    }

    override fun setTrack(track: Track, artistName: String, trackTitle: String) {
        this.artistName = artistName
        this.trackTitle = trackTitle
        this.currentTrack = track

        _state.value = _state.value?.copy(currentTrack = track)

        track.previewUrl?.let { previewUrl ->
            try {
                stopUpdatingProgress()
                if (!isReleased) {
                    mediaPlayer.reset()
                    _state.value = _state.value?.copy(isPrepared = false, isCompleted = false)

                    mediaPlayer.setDataSource(previewUrl)
                    mediaPlayer.prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting track", e)
                e.printStackTrace()
            }
        }
    }

    override fun play() {
        if (_state.value?.isPrepared == true && !mediaPlayer.isPlaying) {
            if (_state.value?.isCompleted == true) {
                mediaPlayer.seekTo(0)
                _state.value = _state.value?.copy(isCompleted = false)
            }
            mediaPlayer.start()
            _state.value = _state.value?.copy(isPlaying = true)
            startUpdatingProgress()
        } else if (!_state.value?.isPrepared!!) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(100)
                play()
            }
        }
    }

    override fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            _state.value = _state.value?.copy(isPlaying = false)
            stopUpdatingProgress()
        }
    }

    override fun isPlaying(): Boolean = _state.value?.isPlaying == true

    override fun startForegroundMode() {
        if (_state.value?.isPlaying == true && !isForeground && !isReleased) {
            try {
                val notification = createNotification()

                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                        startForeground(NOTIFICATION_ID, notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        startForeground(NOTIFICATION_ID, notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
                    }
                    else -> {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                }
                isForeground = true
            } catch (e: SecurityException) {
            } catch (e: Exception) {
            }
        } else {
            Log.d(TAG, "startForegroundMode skipped - already in foreground or not playing")
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

    override fun release() {
        if (isReleased) return

        isReleased = true
        stopUpdatingProgress()
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error releasing media player (already released)", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing media player", e)
        }
    }

    fun isTrackCompleted(): Boolean {
        return _state.value?.isCompleted == true
    }

    private fun createNotification(): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("$artistName - $trackTitle")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Воспроизведение музыки",
                NotificationManager.IMPORTANCE_LOW
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
                val position = try {
                    if (mediaPlayer.isPlaying) {
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

    override fun removeStateObserver(observer: androidx.lifecycle.Observer<PlayerState>) {
        _state.removeObserver(observer)
    }

    private fun stopUpdatingProgress() {
        updateProgressJob?.cancel()
        updateProgressJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }
}