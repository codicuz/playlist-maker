package com.practicum.playlistmaker.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.theme.GetThemeUseCase
import com.practicum.playlistmaker.domain.theme.SwitchThemeUseCase

class ThemeViewModel(
    private val switchThemeUseCase: SwitchThemeUseCase,
    private val getThemeUseCase: GetThemeUseCase
) : ViewModel() {

    private val _isDarkMode = MutableLiveData<Boolean>()
    val isDarkModeLiveData: LiveData<Boolean> = _isDarkMode

    private val _uiEvent = MutableLiveData<SettingsUiEvent>()
    val uiEvent: LiveData<SettingsUiEvent> = _uiEvent

    init {
        _isDarkMode.value = getThemeUseCase.execute()
    }

    fun switchTheme(darkMode: Boolean) {
        switchThemeUseCase.execute(darkMode)
        _isDarkMode.value = darkMode
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
}

