package com.practicum.playlistmaker.presentation.settings

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivitySettingsBinding
import com.practicum.playlistmaker.presentation.theme.ThemeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val themeViewModel: ThemeViewModel by viewModel()
    private var themeInitialized = false

    companion object {
        private const val MIME_TYPE_TEXT_PLAIN = "text/plain"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        themeViewModel.state.observe(this) { state ->
            if (!themeInitialized) {
                binding.themeSwitcher.isChecked = state.isDarkMode
                themeInitialized = true
            }
        }

        binding.themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            if (themeInitialized && isChecked != themeViewModel.state.value?.isDarkMode) {
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

        binding.settingsHeader.setOnClickListener { finish() }
        binding.practicumOffer.setOnClickListener { themeViewModel.onPracticumOfferClicked() }
        binding.sendToHelpdesk.setOnClickListener { themeViewModel.onSendToHelpdeskClicked() }
        binding.shareApp.setOnClickListener { themeViewModel.onShareAppClicked() }
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
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
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
