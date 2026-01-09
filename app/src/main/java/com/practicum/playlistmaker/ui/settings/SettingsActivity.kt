package com.practicum.playlistmaker.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.net.toUri
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.presentation.settings.SettingsUiEvent
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeViewModel: ThemeViewModel
    private var themeInitialized = false

    companion object {
        private const val MIME_TYPE_TEXT_PLAIN = "text/plain"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        themeViewModel = Creator.provideThemeViewModel(application)

        val themeSwitcher = findViewById<SwitchCompat>(R.id.themeSwitcher)
        val buttonBack = findViewById<TextView>(R.id.settingsHeader)
        val practicumOfferBtn = findViewById<TextView>(R.id.practicumOffer)
        val sendToHelpdeskBtn = findViewById<TextView>(R.id.sendToHelpdesk)
        val shareAppBtn = findViewById<TextView>(R.id.shareApp)


        themeViewModel.isDarkModeLiveData.observe(this) { isDark ->
            if (!themeInitialized) {
                themeSwitcher.isChecked = isDark
                themeInitialized = true
            }
        }

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            if (themeInitialized && isChecked != themeViewModel.isDarkModeLiveData.value) {
                themeViewModel.switchTheme(isChecked)
                restartWithTheme()
            }
        }

        themeViewModel.uiEvent.observe(this) { event ->
            when (event) {
                is SettingsUiEvent.OpenPracticumOffer -> openPracticumOffer()
                is SettingsUiEvent.SendToHelpdesk -> openHelpdeskEmail()
                is SettingsUiEvent.ShareApp -> shareApp()
                is SettingsUiEvent.ShowError -> Toast.makeText(
                    this, event.message, Toast.LENGTH_LONG
                ).show()
            }
        }

        buttonBack.setOnClickListener { finish() }
        practicumOfferBtn.setOnClickListener { themeViewModel.onPracticumOfferClicked() }
        sendToHelpdeskBtn.setOnClickListener { themeViewModel.onSendToHelpdeskClicked() }
        shareAppBtn.setOnClickListener { themeViewModel.onShareAppClicked() }
    }

    private fun openPracticumOffer() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = getString(R.string.practicum_license).toUri()
        }
        startSafe(intent)
    }

    private fun openHelpdeskEmail() {
        val email = getString(R.string.email)
        val subject = getString(R.string.email_subject)
        val body = getString(R.string.email_text)
        val uri = "mailto:$email?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
        val intent = Intent(Intent.ACTION_SENDTO, uri.toUri())
        startSafe(intent)
    }

    private fun restartWithTheme() {
        val intent = intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        val options = android.app.ActivityOptions.makeCustomAnimation(this, 0, 0)
        startActivity(intent, options.toBundle())
        finish()
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE_TEXT_PLAIN
            putExtra(
                Intent.EXTRA_TEXT, getString(R.string.https_practicum_yandex_ru_android_developer)
            )
        }
        startSafe(Intent.createChooser(intent, null))
    }

    private fun startSafe(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.no_intent_handle), Toast.LENGTH_LONG).show()
        }
    }
}
