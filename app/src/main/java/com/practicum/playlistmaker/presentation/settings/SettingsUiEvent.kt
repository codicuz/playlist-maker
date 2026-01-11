package com.practicum.playlistmaker.presentation.settings

sealed class SettingsUiEvent {
    object OpenPracticumOffer : SettingsUiEvent()
    object SendToHelpdesk : SettingsUiEvent()
    object ShareApp : SettingsUiEvent()
    data class ShowError(val message: String) : SettingsUiEvent()
}
