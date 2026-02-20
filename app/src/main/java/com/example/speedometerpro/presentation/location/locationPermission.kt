package com.example.speedometerpro.presentation.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.speedometerpro.R
import com.example.speedometerpro.domain.usecase.CheckLocationPermissionUseCase
import com.example.speedometerpro.presentation.main.MainActivity
import kotlinx.coroutines.launch

class locationPermission : AppCompatActivity() {

    private lateinit var viewModel: LocationPermissionViewModel
    private lateinit var checkPermissionUseCase: CheckLocationPermissionUseCase

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            navigateToMain()
        } else {
            // shouldShowRationale is true if they just denied once.
            // It is false if they denied permanently.
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            )
            viewModel.handlePermissionResult(isGranted, shouldShowRationale)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableFullscreen()

        // 1. Initial Logic Check: If already granted, skip this screen
        checkPermissionUseCase = CheckLocationPermissionUseCase(applicationContext)
        if (checkPermissionUseCase()) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_location_permission)

        // 2. Initialize ViewModel
        viewModel = LocationPermissionViewModel(checkPermissionUseCase)

        observeViewModel()

        // 3. Button Click Logic
        findViewById<TextView>(R.id.enable_location_btn).setOnClickListener {
            if (checkPermissionUseCase()) {
                navigateToMain()
            } else {
                // This triggers the system popup.
                // If the user has already denied twice, this popup won't appear,
                // and the launcher callback above will trigger the settings redirect.
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableFullscreen()
        // Check again in case they returned from System Settings
        if (checkPermissionUseCase()) {
            navigateToMain()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.navigateToSettings.collect {
                Toast.makeText(this@locationPermission, "Please enable location in Settings", Toast.LENGTH_LONG).show()
                openAppSettings()
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close this activity so user can't go back to it
    }
    private fun enableFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)

        controller.hide(
            WindowInsetsCompat.Type.statusBars() or
                    WindowInsetsCompat.Type.navigationBars()
        )

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}