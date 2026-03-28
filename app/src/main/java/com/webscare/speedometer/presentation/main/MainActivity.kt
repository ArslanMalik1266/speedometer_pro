package com.webscare.speedometer.presentation.main

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
import com.webscare.speedometer.R
import com.webscare.speedometer.domain.usecase.CheckLocationPermissionUseCase
import com.webscare.speedometer.presentation.history.HistoryFragment
import com.webscare.speedometer.presentation.home.HomeFragment
import com.webscare.speedometer.presentation.location.LocationBottomSheet
import com.webscare.speedometer.presentation.settings.SettingsFragment
import com.webscare.speedometer.util.DrawerDestination
import com.google.android.material.appbar.MaterialToolbar
import com.webscare.speedometer.Utils.addPressEffect

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var toolbar: MaterialToolbar
    private lateinit var toolbarImage: ImageView
    private lateinit var historyIcon: ImageView
    private lateinit var settingsIcon: ImageView
    private lateinit var backIcon: ImageView

    private lateinit var customToolbar: View


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
        backIcon = findViewById(R.id.backIcon)

        backIcon.addPressEffect {
            val homeFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? HomeFragment
            homeFragment?.stopTrackingWithoutSaving()
        }
        observeViewModel()

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
        drawerLayout.setDrawerLockMode( DrawerLayout.LOCK_MODE_UNLOCKED)


        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.9).toInt()
        drawer.layoutParams.width = width
        drawer.requestLayout()

        supportFragmentManager.beginTransaction()
            .replace(R.id.drawer, fragment)
            .addToBackStack(null)
            .commit()

        drawerLayout.openDrawer(GravityCompat.END)
    }

    private fun setupUi() {
        historyIcon.addPressEffect {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                viewModel.onHistoryClicked()
            }
        }

        settingsIcon.addPressEffect {
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
        lifecycleScope.launchWhenStarted {
            viewModel.toolbarState.collect { state ->
                when (state) {
                    ToolbarState.NORMAL -> {
                        toolbarImage.visibility = View.VISIBLE
                        historyIcon.visibility = View.VISIBLE
                        settingsIcon.visibility = View.VISIBLE
                        backIcon.visibility = View.GONE
                    }
                    ToolbarState.TRACKING -> {
                        toolbarImage.visibility = View.GONE
                        historyIcon.visibility = View.GONE
                        settingsIcon.visibility = View.GONE
                        backIcon.visibility = View.VISIBLE
                    }
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
                systemBars.bottom
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


    fun setHudMode(enabled: Boolean) {
        val scale = if (enabled) -1f else 1f
        toolbar.scaleX = scale
    }




}
