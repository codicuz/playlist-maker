package com.practicum.playlistmaker

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {
    fun AppCompatActivity.restartWithTheme() {
        val intent = intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        startActivity(intent, options.toBundle())

        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val buttonBack = findViewById<TextView>(R.id.settingsHeader)
        val switch = findViewById<SwitchCompat>(R.id.night_theme_switch)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

//        val nightMode = AppCompatDelegate.getDefaultNightMode()
//        switch.isChecked = nightMode == AppCompatDelegate.MODE_NIGHT_YES
//
//
//        switch.setOnCheckedChangeListener { _, isChecked ->
//            AppCompatDelegate.setDefaultNightMode(
//                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
//            )
//            restartWithTheme() // Перезапускаем Activity, чтобы применить тему
//        }

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
    }
}