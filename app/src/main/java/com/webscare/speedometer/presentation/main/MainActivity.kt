package com.webscare.speedometer.presentation.main

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.webscare.speedometer.R
import com.webscare.speedometer.presentation.history.HistoryFragment
import com.webscare.speedometer.presentation.home.HomeFragment
import com.webscare.speedometer.presentation.settings.SettingsFragment
import com.webscare.speedometer.util.DrawerDestination
import com.google.android.material.appbar.MaterialToolbar
import com.webscare.speedometer.Utils.addPressEffect
import com.webscare.speedometer.domain.usecase.CheckLocationPermissionUseCase
import com.webscare.speedometer.presentation.customview.TrackingState
import com.webscare.speedometer.presentation.location.LocationBottomSheet

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
        val drawerContainer = findViewById<FrameLayout>(R.id.drawer)
        val displayMetrics = resources.displayMetrics
        val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val percentage = if (isLandscape) 0.50 else 0.85
        val targetWidth = (displayMetrics.widthPixels * percentage).toInt()
        val params = drawerContainer.layoutParams
        params.width = targetWidth
        drawerContainer.layoutParams = params

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
            val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? HomeFragment

            fragment?.let { home ->
                if (home.isHudMode) {
                    home.disableHudMode()
                    setHudMode(false) // Ye function icons wapis layega, lekin humein check karna hai...
                } else {
                    home.stopTrackingWithoutSaving()
                    setHudMode(false)
                }
            }
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
        val drawerContainer = findViewById<FrameLayout>(R.id.drawer)

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

        val existingFragment = supportFragmentManager.findFragmentById(R.id.drawer)
        if (existingFragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(existingFragment)
                .commitNow()
        }
        drawerLayout.openDrawer(GravityCompat.END)
        drawerLayout.postDelayed({
            if (!isFinishing && !supportFragmentManager.isStateSaved) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.drawer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }, 280)
    }
    private fun setupUi() {
        historyIcon.addPressEffect {
            viewModel.onHistoryClicked()
        }

        settingsIcon.addPressEffect {
            viewModel.onSettingsClicked()
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
                val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? HomeFragment
                val hudActive = fragment?.isHudMode ?: false
                when (state) {
                    ToolbarState.NORMAL -> {
                        if (hudActive) {
                            setHudMode(true)
                        } else {
                            toolbarImage.visibility = View.VISIBLE
                            historyIcon.visibility = View.VISIBLE
                            settingsIcon.visibility = View.VISIBLE
                            backIcon.visibility = View.GONE
                        }
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
        val mainView = findViewById<View>(R.id.main)
        val appBar =
            findViewById<com.google.android.material.appbar.AppBarLayout>(R.id.appBarLayout)

        ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
            val systemBars =
                insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())

            // Orientation check karein
            val isLandscape =
                resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

            if (isLandscape) {
                // 1. Landscape mein padding 0 kar dein
                view.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)

                // 2. AppBar ko 12dp margin top dein
                val params = appBar.layoutParams as ViewGroup.MarginLayoutParams
                params.topMargin = (12 * resources.displayMetrics.density).toInt()
                appBar.layoutParams = params
            } else {
                view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )

                // Vertical mein agar margin nahi chahiye to zero kar dein
                val params = appBar.layoutParams as ViewGroup.MarginLayoutParams
                params.topMargin = 0
                appBar.layoutParams = params
            }

            insets
        }
    }

    fun setHudMode(enabled: Boolean) {
        toolbar.scaleX = 1f

        if (enabled) {
            toolbarImage.visibility = View.GONE
            historyIcon.visibility = View.GONE
            settingsIcon.visibility = View.GONE
            backIcon.visibility = View.VISIBLE
        } else {
            // HUD OFF: Ab check karo tracking chal rahi hai ya nahi
            val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? HomeFragment
            val isTracking = fragment?.getTrackingState() != TrackingState.IDLE

            if (isTracking) {
                toolbarImage.visibility = View.GONE
                historyIcon.visibility = View.GONE
                settingsIcon.visibility = View.GONE
                backIcon.visibility = View.VISIBLE
            } else {
                toolbarImage.visibility = View.VISIBLE
                historyIcon.visibility = View.VISIBLE
                settingsIcon.visibility = View.VISIBLE
                backIcon.visibility = View.GONE
            }
        }


    }
    private fun checkAndShowLocationSheet() {
        val checkPermission = CheckLocationPermissionUseCase(applicationContext)

        if (!checkPermission()) {
            val locationSheet = LocationBottomSheet()
            locationSheet.isCancelable = true
            locationSheet.show(supportFragmentManager, "LocationBottomSheet")
        }
    }
}
