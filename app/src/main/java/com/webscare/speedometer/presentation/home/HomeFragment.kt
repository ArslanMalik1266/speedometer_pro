package com.webscare.speedometer.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.webscare.speedometer.R
import com.webscare.speedometer.Utils.addPressEffect
import com.webscare.speedometer.data.datastore.SettingsDataStore
import com.webscare.speedometer.data.local.AppDatabase
import com.webscare.speedometer.data.repository.LocationRepositoryImpl
import com.webscare.speedometer.data.repository.SpeedHistoryRepositoryImpl
import com.webscare.speedometer.domain.model.SpeedHistory
import com.webscare.speedometer.domain.model.SpeedUnit
import com.webscare.speedometer.domain.usecase.*
import com.webscare.speedometer.presentation.customview.LocationPermissionState
import com.webscare.speedometer.presentation.customview.SpeedometerMeterView
import com.webscare.speedometer.presentation.customview.SpeedometerViewModel
import com.webscare.speedometer.presentation.customview.TrackingState
import com.webscare.speedometer.presentation.history.SpeedHistoryViewModel
import com.webscare.speedometer.presentation.history.SpeedHistoryViewModelFactory
import com.webscare.speedometer.presentation.main.MainActivity
import com.webscare.speedometer.presentation.main.MainViewModel
import com.webscare.speedometer.presentation.settings.SettingsViewModel
import com.webscare.speedometer.presentation.settings.SettingsViewModelFactory
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

    private var needleAnimator: android.animation.ValueAnimator? = null
    private var textAnimator: android.animation.ValueAnimator? = null
    private var lastReportedSpeed = 0f
    private lateinit var hudIcon: ImageView
    private var isHudMode = false
    private lateinit var hudStopBtn: ImageView

    private val mainViewModel: MainViewModel by activityViewModels()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = requireContext()
        val locationRepository = LocationRepositoryImpl(requireActivity().application)
        val settingsDataStore = SettingsDataStore(context)
        val speedHistoryDao = AppDatabase.getInstance(context.applicationContext).speedHistoryDao()
        val historyRepository = SpeedHistoryRepositoryImpl(speedHistoryDao)

        // Initialize ViewModels
        viewModel = SpeedometerViewModel(
            calculateSessionUseCase = CalculateSessionUseCase(),
            speedUnitFlow = ObserveSpeedUnitUseCase(settingsDataStore)(),
            getLocationUpdatesUseCase = GetLocationUpdatesUseCase(locationRepository),
            locationRepository = locationRepository
        )

        settingsViewModel = ViewModelProvider(
            requireActivity(),
            SettingsViewModelFactory(
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setupObservers()
        setupClickListeners()
//TODO
        hudIcon.addPressEffect {
            val guideCentre = view.findViewById<androidx.constraintlayout.widget.Guideline>(R.id.guidecentre)
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
                val params = guideCentre.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                params.guidePercent = 0.9f
                guideCentre.layoutParams = params

                mainContent.scaleX = -1f
//                hudIcon.scaleX = -1f
                hudIcon.setImageResource(R.drawable.hud_icon_on)

                if (viewModel.trackingState.value != TrackingState.IDLE) {
                    startStopBtn.visibility = View.GONE
                    hudStopBtn.visibility = View.VISIBLE
                    val paddingPy = (20 * resources.displayMetrics.density).toInt()
                    hudStopBtn.setPadding(0, paddingPy, 0, paddingPy)
                    continueBtn.setPadding(0, paddingPy, 0, paddingPy)
                    val size = (60 * resources.displayMetrics.density).toInt()
                    hudStopBtn.layoutParams.width = size
                    hudStopBtn.layoutParams.height = size
                    continueBtn.layoutParams.width = size
                    continueBtn.layoutParams.height = size
                    hudStopBtn.requestLayout()
                    continueBtn.requestLayout()
                }

            } else {
                avgSpeedTv.visibility = View.VISIBLE
                avgSpeedValueTv.visibility = View.VISIBLE
                avgKmPerHourTv.visibility = View.VISIBLE
                maxSpeedTv.visibility = View.VISIBLE
                maxSpeedValueTv.visibility = View.VISIBLE
                maxKmPerHourTv.visibility = View.VISIBLE
                distanceTv.visibility = View.VISIBLE
                distanceValueTv.visibility = View.VISIBLE
                distanceKmPerHourTv.visibility = View.VISIBLE
                mainContent.scaleX = 1f
                hudIcon.scaleX = 1f
                hudIcon.setImageResource(R.drawable.hud_icon_off)
                val params = guideCentre.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                params.guidePercent = 0.7f
                guideCentre.layoutParams = params

                startStopBtn.visibility = View.VISIBLE
                hudStopBtn.visibility = View.GONE
                val paddingPy = (14 * resources.displayMetrics.density).toInt()
                hudStopBtn.setPadding(0, paddingPy, 0, paddingPy)
                continueBtn.setPadding(0, paddingPy, 0, paddingPy)
                val size = (50 * resources.displayMetrics.density).toInt()
                hudStopBtn.layoutParams.width = size
                hudStopBtn.layoutParams.height = size
                continueBtn.layoutParams.width = size
                continueBtn.layoutParams.height = size
                hudStopBtn.requestLayout()
                continueBtn.requestLayout()
            }
            (requireActivity() as MainActivity).setHudMode(isHudMode)
        }


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
        hudIcon = view.findViewById(R.id.hud_icon)
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
                    android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show()
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
                    when (state) {
                        TrackingState.TRACKING -> {
                            continueBtn.visibility = View.VISIBLE
                            startStopBtn.text = "Stop"
                            startStopBtn.isSelected = true
                            startStopBtn.isActivated = false
                            val paddingPy = (14 * resources.displayMetrics.density).toInt()
                            continueBtn.setPadding(0, paddingPy, 0, paddingPy)
                            continueBtn.setImageResource(R.drawable.continue_icon)
                            continueBtn.setBackgroundResource(R.drawable.circle_grey)


                            if (isHudMode) {
                                startStopBtn.visibility = View.GONE
                                hudStopBtn.visibility = View.VISIBLE
                                val paddingPy = (20 * resources.displayMetrics.density).toInt()
                                hudStopBtn.setPadding(0, paddingPy, 0, paddingPy)
                                continueBtn.setPadding(0, paddingPy, 0, paddingPy)

                                val size = (60 * resources.displayMetrics.density).toInt()
                                hudStopBtn.layoutParams.width = size
                                hudStopBtn.layoutParams.height = size
                                continueBtn.layoutParams.width = size
                                continueBtn.layoutParams.height = size
                                hudStopBtn.requestLayout()
                                continueBtn.requestLayout()
                            }

                            avgSpeedValueTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                            maxSpeedValueTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                            distanceValueTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                        }
                        TrackingState.PAUSED -> {
                            continueBtn.visibility = View.VISIBLE
                            startStopBtn.text = "Save & Reset"
                            startStopBtn.isSelected = false
                            startStopBtn.isActivated = true

                            val paddingPx = (4 * resources.displayMetrics.density).toInt()
                            val paddingPy = (14 * resources.displayMetrics.density).toInt()
                            continueBtn.setPadding(paddingPx, paddingPy, 0, paddingPy)
                            continueBtn.setImageResource(R.drawable.pause_icon)
                            continueBtn.setBackgroundResource(R.drawable.circle_orange)

                            if (isHudMode) {
                                startStopBtn.visibility = View.GONE
                                hudStopBtn.visibility = View.VISIBLE
                                val paddingPy = (20 * resources.displayMetrics.density).toInt()
                                hudStopBtn.setPadding(0, paddingPy, 0, paddingPy)
                                continueBtn.setPadding(paddingPx, paddingPy, 0, paddingPy)
                                val size = (60 * resources.displayMetrics.density).toInt()
                                hudStopBtn.layoutParams.width = size
                                hudStopBtn.layoutParams.height = size
                                continueBtn.layoutParams.width = size
                                continueBtn.layoutParams.height = size
                                hudStopBtn.requestLayout()
                                continueBtn.requestLayout()
                            }

                            avgSpeedValueTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                            maxSpeedValueTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                            distanceValueTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.orange))
                        }
                        TrackingState.IDLE -> {
                            continueBtn.visibility = View.GONE
                            startStopBtn.text = "Start"
                            startStopBtn.isSelected = false
                            startStopBtn.isActivated = false

                            val paddingPx = (4 * resources.displayMetrics.density).toInt()
                            val paddingPy = (14 * resources.displayMetrics.density).toInt()
                            continueBtn.setPadding(paddingPx, paddingPy, 0, paddingPy)
                            continueBtn.setImageResource(R.drawable.pause_icon)
                            continueBtn.setBackgroundResource(R.drawable.circle_orange)

                            if (isHudMode) {
                                hudStopBtn.visibility = View.GONE
                                startStopBtn.visibility = View.VISIBLE
                                val size = (50 * resources.displayMetrics.density).toInt()
                                hudStopBtn.layoutParams.width = size
                                hudStopBtn.layoutParams.height = size
                                continueBtn.layoutParams.width = size
                                continueBtn.layoutParams.height = size
                                hudStopBtn.requestLayout()
                                continueBtn.requestLayout()
                            }

                            avgSpeedValueTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                            maxSpeedValueTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                            distanceValueTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
                        }
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {

        startStopBtn.addPressEffect {
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
            addUpdateListener { speedValueTv.text = "%d".format((it.animatedValue as Float).toInt()) }
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
}
