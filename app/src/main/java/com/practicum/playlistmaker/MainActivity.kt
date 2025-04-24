package com.practicum.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonSearch = findViewById<Button>(R.id.buttonSearch)
        val buttonMedia = findViewById<Button>(R.id.buttonMedia)
        val buttonSettings = findViewById<Button>(R.id.buttonSettings)

        val buttonSearchClickListener: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {
                val searchActivityIntent = Intent(this@MainActivity, SearchActivity::class.java)
                startActivity(searchActivityIntent)
            }
        }

        buttonSearch.setOnClickListener(buttonSearchClickListener)

        buttonMedia.setOnClickListener {
            val mediaActivityIntent = Intent(this, MediaActivity::class.java)
            startActivity(mediaActivityIntent)
        }

        buttonSettings.setOnClickListener {
            val displaySettingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(displaySettingsIntent)
        }
    }
}
