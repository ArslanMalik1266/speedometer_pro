package com.example.speedometerpro.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.speedometerpro.R
import com.example.speedometerpro.domain.usecase.CheckLocationPermissionUseCase
import com.example.speedometerpro.presentation.history.HistoryFragment
import com.example.speedometerpro.presentation.home.HomeFragment
import com.example.speedometerpro.presentation.location.LocationBottomSheet
import com.example.speedometerpro.presentation.settings.SettingsFragment
import com.example.speedometerpro.util.DrawerDestination
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var toolbar: MaterialToolbar
    private lateinit var toolbarImage: ImageView
    private lateinit var historyIcon: ImageView
    private lateinit var settingsIcon: ImageView

    private lateinit var customToolbar: View

    override fun onResume() {
        super.onResume()
        enableFullscreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableFullscreen()
        applyEdgePadding()

        checkAndShowLocationSheet()

        toolbar = findViewById(R.id.toolbar)
        toolbarImage = findViewById(R.id.toolbarImage)
        historyIcon = findViewById(R.id.historyIcon)
        settingsIcon = findViewById(R.id.settingsIcon)
        customToolbar = findViewById(R.id.constraint_toolbar)

        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, HomeFragment())
            .commit()

        setupUi()
        observeViewModel()

        setSupportActionBar(toolbar)


    }

    fun openRightDrawer(fragment: androidx.fragment.app.Fragment) {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val drawer = findViewById<FrameLayout>(R.id.drawer)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END)


        // Set drawer width to 80% of screen
        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.9).toInt()
        drawer.layoutParams.width = width
        drawer.requestLayout()

        // Replace content with the desired fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.drawer, fragment)
            .addToBackStack(null)
            .commit()

        // Open drawer
        drawerLayout.openDrawer(GravityCompat.END)
    }

    private fun setupUi() {
        historyIcon.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                viewModel.onHistoryClicked()
            }
        }

        settingsIcon.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                viewModel.onSettingsClicked()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.drawerDestination.collect { destination ->
                when (destination) {
                    DrawerDestination.History -> openRightDrawer(HistoryFragment())
                    DrawerDestination.Settings -> openRightDrawer(SettingsFragment())
                }
            }
        }
    }

    fun closeDrawerScreen() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        // Pop fragment inside drawer container
        supportFragmentManager.popBackStack()

        // Close drawer
        drawerLayout.closeDrawer(GravityCompat.END)
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

    private fun applyEdgePadding() {

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars = insets.getInsetsIgnoringVisibility(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                0
            )

            insets
        }
    }

    private fun checkAndShowLocationSheet() {
        val checkPermission = CheckLocationPermissionUseCase(applicationContext)

        if (!checkPermission()) {
            val locationSheet = LocationBottomSheet()
            locationSheet.isCancelable = false
            locationSheet.show(supportFragmentManager, "LocationBottomSheet")
        }
    }






}
