package com.practicum.playlistmaker.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.domain.theme.GetThemeUseCase
import com.practicum.playlistmaker.domain.theme.SwitchThemeUseCase

class MainViewModel(
    private val getThemeUseCase: GetThemeUseCase, private val switchThemeUseCase: SwitchThemeUseCase
) : ViewModel() {

    private val _isDarkMode = MutableLiveData<Boolean>()

    init {
        _isDarkMode.value = getThemeUseCase.execute()
    }
}
