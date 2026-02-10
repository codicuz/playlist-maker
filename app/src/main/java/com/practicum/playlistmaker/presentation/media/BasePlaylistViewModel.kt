package com.practicum.playlistmaker.presentation.media

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BasePlaylistScreenState(
    val title: String = "",
    val description: String = "",
    val coverUri: Uri? = null,
    val originalCoverUri: String? = null,
    val isCreateEnabled: Boolean = false,
    val isCreating: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

abstract class BasePlaylistViewModel : ViewModel() {
    protected val _state = MutableStateFlow(BasePlaylistScreenState())
    val state: StateFlow<BasePlaylistScreenState> = _state.asStateFlow()

    fun onTitleChanged(title: String) {
        _state.value = _state.value.copy(
            title = title, isCreateEnabled = title.isNotBlank()
        )
    }

    fun onDescriptionChanged(description: String) {
        _state.value = _state.value.copy(description = description)
    }

    fun onCoverSelected(uri: Uri) {
        _state.value = _state.value.copy(coverUri = uri)
    }

    abstract fun save()
}