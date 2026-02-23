package com.practicum.playlistmaker.presentation.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.domain.theme.GetThemeUseCase
import com.practicum.playlistmaker.domain.theme.SwitchThemeUseCase
import com.practicum.playlistmaker.presentation.settings.SettingsUiEvent
import com.practicum.playlistmaker.presentation.settings.SingleLiveEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val switchThemeUseCase: SwitchThemeUseCase,
    private val getThemeUseCase: GetThemeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ThemeScreenState(isDarkMode = getThemeUseCase.execute()))
    val state: StateFlow<ThemeScreenState> = _state.asStateFlow()

    private val _uiEvent = SingleLiveEvent<SettingsUiEvent?>()
    val uiEvent: LiveData<SettingsUiEvent?> = _uiEvent

    fun switchTheme(darkMode: Boolean) {
        viewModelScope.launch {
            switchThemeUseCase.execute(darkMode)
            _state.value = _state.value.copy(isDarkMode = darkMode)
        }
    }

    fun onPracticumOfferClicked() {
        _uiEvent.value = SettingsUiEvent.OpenPracticumOffer
    }

    fun onSendToHelpdeskClicked() {
        _uiEvent.value = SettingsUiEvent.SendToHelpdesk
    }

    fun onShareAppClicked() {
        _uiEvent.value = SettingsUiEvent.ShareApp
    }

    fun resetUiEvent() {
        _uiEvent.value = null
    }
}