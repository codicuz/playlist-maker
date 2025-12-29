package com.practicum.playlistmaker.ui

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.util.ThemeSwitcher

class SettingsActivity : AppCompatActivity() {
    companion object {
        const val MAIL_TO = "mailto:"
        const val MIME_TYPE_TEXT_PLAIN = "text/plain"
    }

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
        Toast.makeText(
            this, getString(R.string.no_intent_handle), Toast.LENGTH_LONG
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
        sendToHeldesk.data = Uri.parse(MAIL_TO)
        sendToHeldesk.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
        sendToHeldesk.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
        sendToHeldesk.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_text))

//        if (canHandleIntent(sendToHeldesk)) startActivity(sendToHeldesk) else intentHandleError()
        startActivity(sendToHeldesk)
    }

    private fun callShareApp() {
        val shareApp = Intent(Intent.ACTION_SEND)
        shareApp.type = MIME_TYPE_TEXT_PLAIN
        shareApp.putExtra(
            Intent.EXTRA_TEXT,
            getString(R.string.https_practicum_yandex_ru_android_developer)
        )

        if (canHandleIntent(shareApp)) startActivity(shareApp) else intentHandleError()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val buttonBack = findViewById<TextView>(R.id.settingsHeader)
        val practicumOfferBtn = findViewById<TextView>(R.id.practicumOffer)
        val sendToHelpdesk = findViewById<TextView>(R.id.sendToHelpdesk)
        val shareApp = findViewById<TextView>(R.id.shareApp)

        val themeSwitcher = findViewById<SwitchCompat>(R.id.themeSwitcher)
        themeSwitcher.isChecked = (applicationContext as ThemeSwitcher).isDarkMode()

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            (applicationContext as ThemeSwitcher).switchTheme(isChecked)
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

        shareApp.setOnClickListener {
            callShareApp()
        }
    }
}