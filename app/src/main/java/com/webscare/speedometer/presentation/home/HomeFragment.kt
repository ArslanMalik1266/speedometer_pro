package com.webscare.speedometer.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.webscare.speedometer.R
import com.webscare.speedometer.Utils.addPressEffect
import com.webscare.speedometer.data.datastore.SettingsDataStore
import com.webscare.speedometer.data.local.AppDatabase
import com.webscare.speedometer.data.repository.LocationRepositoryImpl
import com.webscare.speedometer.data.repository.SpeedHistoryRepositoryImpl
import com.webscare.speedometer.domain.model.SpeedHistory
import com.webscare.speedometer.domain.model.SpeedUnit
import com.webscare.speedometer.domain.usecase.CalculateSessionUseCase
import com.webscare.speedometer.domain.usecase.CheckLocationPermissionUseCase
import com.webscare.speedometer.domain.usecase.DeleteSpeedHistoryUseCase
import com.webscare.speedometer.domain.usecase.GetAutoSaveUseCase
import com.webscare.speedometer.domain.usecase.GetLocationUpdatesUseCase
import com.webscare.speedometer.domain.usecase.GetSpeedAlertEnabledUseCase
import com.webscare.speedometer.domain.usecase.GetSpeedLimitUseCase
import com.webscare.speedometer.domain.usecase.ObserveSpeedUnitUseCase
import com.webscare.speedometer.domain.usecase.SetAutoSaveUseCase
import com.webscare.speedometer.domain.usecase.SetSpeedAlertEnabledUseCase
import com.webscare.speedometer.domain.usecase.SetSpeedLimitUseCase
import com.webscare.speedometer.domain.usecase.SetSpeedUnitUseCase
import com.webscare.speedometer.domain.usecase.formatSpeedUseCase
import com.webscare.speedometer.presentation.customview.SpeedometerMeterView
import com.webscare.speedometer.presentation.customview.SpeedometerViewModel
import com.webscare.speedometer.presentation.customview.TrackingState
import com.webscare.speedometer.presentation.history.SpeedHistoryViewModel
import com.webscare.speedometer.presentation.history.SpeedHistoryViewModelFactory
import com.webscare.speedometer.presentation.main.MainActivity
import com.webscare.speedometer.presentation.main.MainViewModel
import com.webscare.speedometer.presentation.settings.SettingsViewModel
import com.webscare.speedometer.presentation.settings.SettingsViewModelFactory
import com.webscare.speedometer.util.LocationPermissionHandler
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var speedometerMeter: SpeedometerMeterView
    private lateinit var speedValueTv: TextView
    private lateinit var timerValueTv: TextView
    private lateinit var distanceValueTv: TextView
    private lateinit var maxSpeedValueTv: TextView
    private lateinit var avgSpeedValueTv: TextView
    private lateinit var startStopBtn: TextView
    private lateinit var continueBtn: ImageView
    private lateinit var avgKmPerHourTv: TextView
    private lateinit var maxKmPerHourTv: TextView
    private lateinit var speedUnitTv: TextView
    private lateinit var distanceKmPerHourTv: TextView
    private lateinit var loadingView: View
    private lateinit var mainContent: View
    private lateinit var avgSpeedTv: TextView
    private lateinit var maxSpeedTv: TextView
    private lateinit var distanceTv: TextView

    private lateinit var viewModel: SpeedometerViewModel
    private lateinit var historyViewModel: SpeedHistoryViewModel
    private lateinit var settingsViewModel: SettingsViewModel


    private lateinit var permissionHandler: LocationPermissionHandler

    private var needleAnimator: android.animation.ValueAnimator? = null
    private var textAnimator: android.animation.ValueAnimator? = null
    private var lastReportedSpeed = 0f
    private var hudIcon: ImageView? = null
    var isHudMode = false
    private lateinit var hudStopBtn: ImageView

    private val mainViewModel: MainViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = requireContext()
        val locationRepository = LocationRepositoryImpl(requireActivity().application)
        val settingsDataStore = SettingsDataStore(context)
        val speedHistoryDao = AppDatabase.getInstance(context.applicationContext).speedHistoryDao()
        val historyRepository = SpeedHistoryRepositoryImpl(speedHistoryDao)

        // Initializing the permission handler
        permissionHandler = LocationPermissionHandler(
            fragment = this,
            onPermissionGranted = {
                handleStartStopAction()
            }
        )

        // Initialize ViewModels
        viewModel = SpeedometerViewModel(
            calculateSessionUseCase = CalculateSessionUseCase(),
            speedUnitFlow = ObserveSpeedUnitUseCase(settingsDataStore)(),
            getLocationUpdatesUseCase = GetLocationUpdatesUseCase(locationRepository),
            locationRepository = locationRepository
        )

        settingsViewModel = ViewModelProvider(
            requireActivity(), SettingsViewModelFactory(
                clearHistoryUseCase = DeleteSpeedHistoryUseCase(historyRepository),
                getAutoSaveUseCase = GetAutoSaveUseCase(settingsDataStore),
                setAutoSaveUseCase = SetAutoSaveUseCase(settingsDataStore),
                observeSpeedUnitUseCase = ObserveSpeedUnitUseCase(settingsDataStore),
                setSpeedUnitUseCase = SetSpeedUnitUseCase(settingsDataStore),
                getSpeedAlertUseCase = GetSpeedAlertEnabledUseCase(settingsDataStore),
                setSpeedAlertUseCase = SetSpeedAlertEnabledUseCase(settingsDataStore),
                getSpeedLimitUseCase = GetSpeedLimitUseCase(settingsDataStore),
                setSpeedLimitUseCase = SetSpeedLimitUseCase(settingsDataStore)
            )
        )[SettingsViewModel::class.java]

        historyViewModel = ViewModelProvider(
            this,
            SpeedHistoryViewModelFactory(historyRepository, GetAutoSaveUseCase(settingsDataStore))
        )[SpeedHistoryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupObservers()
        setupClickListeners()

        hudIcon?.addPressEffect {
            val guideCentre =
                view.findViewById<androidx.constraintlayout.widget.Guideline>(R.id.guidecentre)
            if (isHudMode) return@addPressEffect
            isHudMode = !isHudMode
            if (isHudMode) {
                avgSpeedTv.visibility = View.INVISIBLE
                avgSpeedValueTv.visibility = View.INVISIBLE
                avgKmPerHourTv.visibility = View.INVISIBLE
                maxSpeedTv.visibility = View.INVISIBLE
                maxSpeedValueTv.visibility = View.INVISIBLE
                maxKmPerHourTv.visibility = View.INVISIBLE
                distanceTv.visibility = View.INVISIBLE
                distanceValueTv.visibility = View.INVISIBLE
                distanceKmPerHourTv.visibility = View.INVISIBLE
                val params =
                    guideCentre.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                params.guidePercent = 0.9f
                guideCentre.layoutParams = params

                mainContent.scaleX = -1f
//                hudIcon.scaleX = -1f
                hudIcon?.setImageResource(R.drawable.hud_icon_on)

                if (viewModel.trackingState.value != TrackingState.IDLE) {
                    startStopBtn.visibility = View.GONE
                    hudStopBtn.visibility = View.VISIBLE
                    val size = (60 * resources.displayMetrics.density).toInt()

                    continueBtn.layoutParams.width = size
                    continueBtn.layoutParams.height = size
                    hudStopBtn.requestLayout()
                    continueBtn.requestLayout()
                }

            }
            (requireActivity() as MainActivity).setHudMode(isHudMode)
        }
    }

    fun disableHudMode() {
        val guideCentre =
            view?.findViewById<androidx.constraintlayout.widget.Guideline>(R.id.guidecentre)
                ?: return
        isHudMode = false

        // Saare views ko VISIBLE karein
        val viewsToInvert = listOf(
            avgSpeedTv,
            avgSpeedValueTv,
            avgKmPerHourTv,
            maxSpeedTv,
            maxSpeedValueTv,
            maxKmPerHourTv,
            distanceTv,
            distanceValueTv,
            distanceKmPerHourTv
        )
        viewsToInvert.forEach { it.visibility = View.VISIBLE }

        mainContent.scaleX = 1f
        hudIcon?.scaleX = 1f
        hudIcon?.setImageResource(R.drawable.hud_icon_off)

        val params =
            guideCentre.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        params.guidePercent = 0.7f
        guideCentre.layoutParams = params

        val isLandscape =
            resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val size60 = (60 * resources.displayMetrics.density).toInt()
        val size50 = (50 * resources.displayMetrics.density).toInt()

        if (!isLandscape) {
            // Return to standard portrait layout
            continueBtn.layoutParams.width = size50
            continueBtn.layoutParams.height = size50

            startStopBtn.visibility =
                if (viewModel.trackingState.value == TrackingState.IDLE) View.VISIBLE else View.VISIBLE

            hudStopBtn.visibility =
                if (viewModel.trackingState.value == TrackingState.TRACKING) View.GONE else View.VISIBLE
            hudStopBtn.visibility = if (viewModel.trackingState.value == TrackingState.IDLE) View.GONE else View.VISIBLE
            hudStopBtn.visibility = if (viewModel.trackingState.value == TrackingState.PAUSED) View.GONE else View.GONE



            startStopBtn.layoutParams.width = 0 // ConstraintLayout match_constraint
            startStopBtn.layoutParams.height = size50
        } else {
            // Keep landscape specific sizing
            continueBtn.layoutParams.width = size60
            continueBtn.layoutParams.height = size60
        }

        continueBtn.requestLayout()
        hudStopBtn.requestLayout()
        (requireActivity() as MainActivity).setHudMode(false)
    }

    private fun bindViews(view: View) {
        speedometerMeter = view.findViewById(R.id.speedometerMeter)
        speedValueTv = view.findViewById(R.id.speed_value)
        timerValueTv = view.findViewById(R.id.timer_value)
        distanceValueTv = view.findViewById(R.id.distance_value)
        maxSpeedValueTv = view.findViewById(R.id.max_speed_value)
        avgSpeedValueTv = view.findViewById(R.id.avg_speed_value)
        startStopBtn = view.findViewById(R.id.btn_home)
        continueBtn = view.findViewById(R.id.continue_btn)
        avgKmPerHourTv = view.findViewById(R.id.avg_km_per_hour_tv)
        maxKmPerHourTv = view.findViewById(R.id.max_km_per_hour_tv)
        speedUnitTv = view.findViewById(R.id.speed_unit)
        distanceKmPerHourTv = view.findViewById(R.id.distance_km_per_hour_tv)
        mainContent = view.findViewById(R.id.fragment_home)
        hudIcon = view.findViewById(R.id.hud_icon) ?: ImageView(requireContext())
        hudStopBtn = view.findViewById(R.id.hud_stop_btn)
        avgSpeedTv = view.findViewById(R.id.avg_speed_tv)
        maxSpeedTv = view.findViewById(R.id.max_speed_tv)
        distanceTv = view.findViewById(R.id.distance_tv)
    }

    private fun setupObservers() {

        // Formatted speed
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.formattedSpeed.collect { formatted ->
                    val numericSpeed = formatted.toFloatOrNull() ?: 0f
                    settingsViewModel.checkSpeed(numericSpeed)

                    if (viewModel.trackingState.value != TrackingState.IDLE) {
                        animateNeedle(numericSpeed)
                        animateTextSpeed(lastReportedSpeed, numericSpeed)
                        lastReportedSpeed = numericSpeed
                    }

                    speedValueTv.text = formatted
                }
            }
        }


        // Speed alerts
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.showAlertTrigger.collect { message ->
                    android.widget.Toast.makeText(
                        requireContext(), message, android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Speed & distance units
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.speedUnitFlow.collect { unit ->
                    val speedText = if (unit == SpeedUnit.KMH) "km/h" else "mph"
                    val distText = if (unit == SpeedUnit.KMH) "km" else "mi"

                    speedUnitTv.text = speedText
                    maxKmPerHourTv.text = speedText
                    avgKmPerHourTv.text = speedText
                    distanceKmPerHourTv.text = distText
                }
            }
        }

        // Max & Avg speed
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.maxSpeed, viewModel.speedUnitFlow) { max, unit ->
                    formatSpeedUseCase(max.toDouble(), unit)
                }.collect { maxSpeedValueTv.text = it }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.formattedAvgSpeed, viewModel.speedUnitFlow) { avg, unit ->
                    formatSpeedUseCase(avg.toDouble(), unit)
                }.collect { avgSpeedValueTv.text = it }
            }
        }

        // Timer
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trackingTime.collect { time ->
                    timerValueTv.text = "%02d:%02d".format(time / 60, time % 60)
                }
            }
        }

        // Distance
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.distanceInSelectedUnit.collect { dist ->
                    distanceValueTv.text = when {
                        dist == 0f -> "0"
                        dist < 1f -> "%.2f".format(dist)
                        else -> "%.1f".format(dist)
                    }
                }
            }
        }

        // Tracking state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trackingState.collect { state ->
                    val isLandscape =
                        resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                    val density = resources.displayMetrics.density
                    val commonSize = (60 * density).toInt()
                    val standardSize = (50 * density).toInt()

                    // Reset layout params and padding first to prevent "sticky" HUD styles
                    if (!isHudMode && !isLandscape) {
                        continueBtn.layoutParams.width = standardSize
                        continueBtn.layoutParams.height = standardSize
                    } else {
                        continueBtn.layoutParams.width = commonSize
                        continueBtn.layoutParams.height = commonSize
                    }

                    when (state) {
                        TrackingState.TRACKING -> {
                            if (isHudMode || isLandscape) {
                                hudStopBtn.visibility = View.VISIBLE
                                startStopBtn.visibility = View.GONE
                                hudStopBtn.setBackgroundResource(R.drawable.circle_grey)
                                hudStopBtn.setImageResource(R.drawable.stop_icon)
                            } else {
                                startStopBtn.visibility = View.VISIBLE
                                hudStopBtn.visibility = View.GONE
                                startStopBtn.text = "Stop"
                            }
                            continueBtn.visibility = View.VISIBLE
                            continueBtn.setImageResource(R.drawable.continue_icon)
                            continueBtn.setBackgroundResource(R.drawable.circle_grey)
                            updateColors(R.color.orange)
                        }

                        TrackingState.PAUSED -> {
                            if (isHudMode || isLandscape) {
                                hudStopBtn.visibility = View.VISIBLE
                                startStopBtn.visibility = View.GONE
                            } else {
                                hudStopBtn.visibility = View.GONE
                                startStopBtn.visibility = View.VISIBLE
                                startStopBtn.text = "Save & Reset"
                            }

                            continueBtn.visibility = View.VISIBLE
                            continueBtn.setImageResource(R.drawable.pause_icon)
                            continueBtn.setBackgroundResource(R.drawable.circle_orange)
                            updateColors(R.color.orange)
                        }

                        TrackingState.IDLE -> {
                            continueBtn.visibility = View.GONE
                            hudStopBtn.visibility = View.GONE
                            startStopBtn.visibility = View.VISIBLE
                            startStopBtn.text = "Start"
                            updateColors(R.color.grey)
                        }
                    }
                    continueBtn.requestLayout()
                    (requireActivity() as MainActivity).setHudMode(isHudMode)
                }
            }
        }
    }

    // Chota sa helper function colors ke liye (optional but clean)
    private fun updateColors(colorRes: Int) {
        val color = ContextCompat.getColor(requireContext(), colorRes)
        avgSpeedValueTv.setTextColor(color)
        maxSpeedValueTv.setTextColor(color)
        distanceValueTv.setTextColor(color)
    }

    private fun handleStartStopAction() {
        when (viewModel.trackingState.value) {
            TrackingState.TRACKING, TrackingState.PAUSED -> {
                val session = viewModel.saveCurrentSession()
                viewModel.stopTracking()
                saveToHistory(session)
                resetSpeedometerUI()
                mainViewModel.onTrackingStopped()
            }

            TrackingState.IDLE -> {
                viewModel.resetMetrics()
                resetSpeedometerUI()
                viewModel.startTracking()
                mainViewModel.onTrackingStarted()
            }
        }
    }

    private fun setupClickListeners() {

        startStopBtn.addPressEffect {
            val isAllowed = CheckLocationPermissionUseCase(requireContext())
            if (!isAllowed()) {
                permissionHandler.requestPermission()
            } else {
                handleStartStopAction()
            }

        }

        continueBtn.addPressEffect {
            when (viewModel.trackingState.value) {
                TrackingState.TRACKING -> viewModel.pauseTracking()
                TrackingState.PAUSED -> viewModel.resumeTracking()
                else -> {
                    viewModel.startTracking()
                    mainViewModel.onTrackingStarted()
                }

            }
        }
        hudStopBtn.addPressEffect {
            val session = viewModel.saveCurrentSession()
            viewModel.stopTracking()
            saveToHistory(session)
            resetSpeedometerUI()
            mainViewModel.onTrackingStopped()
            hudStopBtn.visibility = View.GONE
            startStopBtn.visibility = View.VISIBLE
        }
    }

    private fun saveToHistory(speedItem: SpeedHistory) {
        historyViewModel.saveTrip(speedItem)
    }


    private fun animateNeedle(targetSpeedKmh: Float) {
        needleAnimator?.cancel()
        val targetPos = (targetSpeedKmh / 240f) * 13
        val currentPos = speedometerMeter.speed
        needleAnimator = android.animation.ValueAnimator.ofFloat(currentPos, targetPos).apply {
            duration = 600
            interpolator = android.view.animation.LinearInterpolator()
            addUpdateListener { speedometerMeter.speed = it.animatedValue as Float }
            start()
        }
    }

    private fun animateTextSpeed(oldSpeed: Float, newSpeed: Float) {
        textAnimator?.cancel()
        textAnimator = android.animation.ValueAnimator.ofFloat(oldSpeed, newSpeed).apply {
            duration = 900
            addUpdateListener {
                speedValueTv.text = "%d".format((it.animatedValue as Float).toInt())
            }
            start()
        }
    }

    private fun resetSpeedometerUI() {
        animateNeedle(0f)
        animateTextSpeed(lastReportedSpeed, 0f)
        lastReportedSpeed = 0f

        timerValueTv.text = "00:00"
        distanceValueTv.text = "0"
        maxSpeedValueTv.text = "0.0"
        avgSpeedValueTv.text = "0.0"
    }

    fun stopTrackingWithoutSaving() {
        viewModel.stopTracking()
        resetSpeedometerUI()
        mainViewModel.onTrackingStopped()
    }

    fun getTrackingState(): TrackingState {
        return viewModel.trackingState.value
    }
}
