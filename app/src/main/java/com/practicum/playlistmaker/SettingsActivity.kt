package com.practicum.playlistmaker

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {
    private fun AppCompatActivity.restartWithTheme() {
        val intent = intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        startActivity(intent, options.toBundle())
        finish()
    }

    private fun canHandleIntent(intent: Intent): Boolean {
        val activities = packageManager.queryIntentActivities(intent, 0)
        return activities.isNotEmpty()
    }

    private fun intentHandleError() {
        Toast.makeText(this, getString(R.string.no_intent_handle), Toast.LENGTH_LONG
        ).show()
    }

    private fun callPracticumOfferIntent() {
        val practicumOffer = getString(R.string.practicum_license)
        val agreement = Intent(Intent.ACTION_VIEW)
        agreement.data = Uri.parse(practicumOffer)

        if (canHandleIntent(agreement)) startActivity(agreement) else intentHandleError()
    }

    private fun callSendToHelpdesk() {
        val sendToHeldesk = Intent(Intent.ACTION_SENDTO)
        sendToHeldesk.data = Uri.parse(getString(R.string.send_to))
        sendToHeldesk.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
        sendToHeldesk.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
        sendToHeldesk.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_text))

//        if (canHandleIntent(sendToHeldesk)) startActivity(sendToHeldesk) else intentHandleError()
        startActivity(sendToHeldesk)
    }

    private fun callShareApp() {
        val shareApp = Intent(Intent.ACTION_SEND)
        shareApp.type = getString(R.string.text_plain)
        shareApp.putExtra(Intent.EXTRA_TEXT,
            getString(R.string.https_practicum_yandex_ru_android_developer))

        if (canHandleIntent(shareApp)) startActivity(shareApp) else intentHandleError()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val buttonBack = findViewById<TextView>(R.id.settingsHeader)
        val switch = findViewById<SwitchCompat>(R.id.night_theme_switch)
        val practicumOfferBtn = findViewById<TextView>(R.id.practicumOffer)
        val sendToHelpdesk = findViewById<TextView>(R.id.sendToHelpdesk)
        val shareApp = findViewById<TextView>(R.id.shareApp)

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        val isDark = prefs.getBoolean("dark_mode", false)
        switch.isChecked = isDark
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            restartWithTheme()
        }

        buttonBack.setOnClickListener {
            finish()
        }

        practicumOfferBtn.setOnClickListener {
            callPracticumOfferIntent()
        }

        sendToHelpdesk.setOnClickListener {
            callSendToHelpdesk()
        }

        shareApp.setOnClickListener{
            callShareApp()
        }
    }
}