package com.practicum.playlistmaker.presentation.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.theme.GetThemeUseCase
import com.practicum.playlistmaker.domain.theme.SwitchThemeUseCase
import com.practicum.playlistmaker.presentation.settings.SettingsUiEvent
import com.practicum.playlistmaker.presentation.settings.SingleLiveEvent

class ThemeViewModel(
    private val switchThemeUseCase: SwitchThemeUseCase,
    private val getThemeUseCase: GetThemeUseCase
) : ViewModel() {

    private val _state = MutableLiveData(ThemeScreenState())
    val state: LiveData<ThemeScreenState> = _state

    private val _uiEvent = SingleLiveEvent<SettingsUiEvent?>()
    val uiEvent: LiveData<SettingsUiEvent?> = _uiEvent

    init {
        _state.value = ThemeScreenState(isDarkMode = getThemeUseCase.execute())
    }

    fun switchTheme(darkMode: Boolean) {
        switchThemeUseCase.execute(darkMode)
        _state.value = _state.value?.copy(isDarkMode = darkMode)
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