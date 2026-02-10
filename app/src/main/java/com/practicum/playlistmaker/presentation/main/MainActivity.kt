package com.practicum.playlistmaker.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPermissionLauncher()
        // Убираем автоматическую проверку разрешений при запуске

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container_view) as? NavHostFragment
        val navController = navHostFragment?.navController

        navController?.let {
            binding.bottomNavigationView.setupWithNavController(it)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.audioPlayerFragment,
                    R.id.newPlaylistFragment,
                    R.id.playlistFragment -> hideBottomNav()
                    else -> showBottomNav()
                }
            }
        }
    }

    private fun initPermissionLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    Log.d("PERMISSION", "Photo access granted")
                } else {
                    Log.d("PERMISSION", "Photo access denied")
                }
            }
    }

    fun requestGalleryPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        val granted = ContextCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            permissionLauncher.launch(permission)
            return false
        }
        return true
    }

    fun hideBottomNav() {
        binding.bottomNavigationView.isVisible = false

    }

    fun showBottomNav() {
        binding.bottomNavigationView.isVisible = true
    }
}