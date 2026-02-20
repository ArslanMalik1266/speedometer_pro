package com.example.speedometerpro.presentation.settings

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.example.speedometerpro.R
import com.example.speedometerpro.data.datastore.SettingsDataStore
import com.example.speedometerpro.data.local.AppDatabase
import com.example.speedometerpro.data.repository.SpeedHistoryRepositoryImpl
import com.example.speedometerpro.domain.model.SpeedUnit
import com.example.speedometerpro.domain.usecase.*
import com.example.speedometerpro.presentation.main.MainActivity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var speedUnitValue: TextView
    private lateinit var speedUnitTv: TextView
    private lateinit var speedLimitValueTv: TextView
    private lateinit var speedLimitTv: TextView
    private lateinit var unitRadioLayout: ConstraintLayout
    private lateinit var limitLayout: ConstraintLayout
    private lateinit var speed_limit_container: ConstraintLayout
    private lateinit var speed_unit_container: ConstraintLayout
    private lateinit var unitRadioGroup: RadioGroup
    private lateinit var unitKmh: RadioButton
    private lateinit var unitMph: RadioButton
    private lateinit var seekBar: SeekBar
    private lateinit var autoSaveSwitch: SwitchCompat
    private lateinit var speedAlertSwitch: SwitchCompat
    private lateinit var clearHistoryTv: TextView

    private var isUpdatingUI = false
    private var isUnitLayoutVisible = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyEdgePadding(view)
        initViews(view)
        initViewModel()
        setupListeners(view)
        observeViewModel()
        handleBackPress()
    }

    private fun initViews(view: View) {
        speedUnitValue = view.findViewById(R.id.speed_unit_value)
        speedUnitTv = view.findViewById(R.id.speed_unit_tv)
        speedLimitValueTv = view.findViewById(R.id.speed_limit_value)
        speedLimitTv = view.findViewById(R.id.speed_limit_tv)
        unitRadioLayout = view.findViewById(R.id.unit_radio_layout)
        limitLayout = view.findViewById(R.id.limit_layout)
        unitRadioGroup = view.findViewById(R.id.unit_radio_group)
        unitKmh = view.findViewById(R.id.unit_radio_kmh)
        unitMph = view.findViewById(R.id.unit_radio_mph)
        autoSaveSwitch = view.findViewById(R.id.auto_save_switch)
        clearHistoryTv = view.findViewById(R.id.clear_trip_history_tv)
        seekBar = view.findViewById(R.id.mySlider)
        speedAlertSwitch = view.findViewById<SwitchCompat>(R.id.speed_alert_switch)
        speed_limit_container = view.findViewById(R.id.speed_limit_container)
        speed_unit_container = view.findViewById(R.id.unit_selection_container)

        speed_unit_container.background = null
        speed_limit_container.background = null


        // Initial invisible states for layouts
        unitRadioLayout.apply { visibility = View.GONE; alpha = 0f; translationY = -50f }
        limitLayout.apply { visibility = View.GONE; alpha = 0f; translationY = -50f }
    }

    private fun initViewModel() {
        val dataStore = SettingsDataStore(requireContext())
        val repo = SpeedHistoryRepositoryImpl(AppDatabase.getInstance(requireContext()).speedHistoryDao())
        val factory = SettingsViewModelFactory(
            DeleteSpeedHistoryUseCase(repo), GetAutoSaveUseCase(dataStore),
            SetAutoSaveUseCase(dataStore), ObserveSpeedUnitUseCase(dataStore), SetSpeedUnitUseCase(dataStore), GetSpeedAlertEnabledUseCase(dataStore),
            SetSpeedAlertEnabledUseCase(dataStore),
            GetSpeedLimitUseCase(dataStore), SetSpeedLimitUseCase(dataStore)

        )
        viewModel = ViewModelProvider(this, factory)[SettingsViewModel::class.java]
    }

    private fun setupListeners(view: View) {
        view.findViewById<MaterialToolbar>(R.id.settings_toolbar).setNavigationOnClickListener { viewModel.onBackClicked() }

        speedUnitValue.setOnClickListener { toggleUnitLayout() }
        speedLimitValueTv.setOnClickListener { viewModel.toggleLimitLayout() }

        view.findViewById<ImageButton>(R.id.minus_image).setOnClickListener { viewModel.updateSpeedLimit(viewModel.speedLimitDisplay.value - 1) }
        view.findViewById<ImageButton>(R.id.plus_image).setOnClickListener { viewModel.updateSpeedLimit(viewModel.speedLimitDisplay.value + 1) }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) viewModel.updateSpeedLimit(progress)
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        speedAlertSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUI) {
                viewModel.setSpeedAlert(isChecked)
            }
        }

        unitRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (isUpdatingUI) return@setOnCheckedChangeListener
            val unit = if (checkedId == R.id.unit_radio_kmh) SpeedUnit.KMH else SpeedUnit.MPH
            viewModel.onUnitSelected(unit)
        }

        autoSaveSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingUI) viewModel.setAutoSave(isChecked)
        }

        clearHistoryTv.setOnClickListener { showClearHistoryDialog() }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Speed Limit Value & Units
                launch {
                    viewModel.speedLimitDisplay.collect { value ->
                        val unitStr = if (viewModel.currentUnit.value == SpeedUnit.KMH) "km/h" else "mph"
                        speedLimitValueTv.text = "$value $unitStr"
                        if (seekBar.progress != value) seekBar.progress = value
                    }
                }

                // Speed Limit Layout Visibility & Backgrounds
                launch {
                    viewModel.isLimitLayoutVisible.collect { isVisible ->
                        if (isVisible) expandLimitLayout() else collapseLimitLayout()
                    }
                }

                // Speed Unit
                launch {
                    viewModel.currentUnit.collect { unit ->
                        isUpdatingUI = true
                        if (unit == SpeedUnit.KMH) { unitKmh.isChecked = true; speedUnitValue.text = "km/h" }
                        else { unitMph.isChecked = true; speedUnitValue.text = "mph" }
                        isUpdatingUI = false
                    }
                }

                launch {
                    viewModel.speedAlertEnabled.collect { enabled ->
                        isUpdatingUI = true
                        speedAlertSwitch.isChecked = enabled
                        isUpdatingUI = false
                    }
                }

                launch {
                    viewModel.showAlertTrigger.collect { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        // You can also play a sound here!
                    }
                }

                // Auto Save & Navigation
                launch { viewModel.autoSave.collect { isUpdatingUI = true; autoSaveSwitch.isChecked = it; isUpdatingUI = false } }
                launch { viewModel.navigateBack.collect { (requireActivity() as MainActivity).closeDrawerScreen() } }
            }
        }
    }

    private fun expandLimitLayout() {
        limitLayout.visibility = View.VISIBLE
        limitLayout.animate().alpha(1f).translationY(0f).setDuration(250).start()
        speed_limit_container.setBackgroundResource(R.drawable.container_rounded_bg)
        speedLimitValueTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0)
    }

    private fun collapseLimitLayout() {
        limitLayout.animate().alpha(0f).translationY(-50f).setDuration(250).withEndAction {
            limitLayout.visibility = View.GONE
            speed_limit_container.background = null
            speedLimitValueTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.spinner_icon, 0)
        }.start()
    }

    private fun toggleUnitLayout() { if (isUnitLayoutVisible) collapseUnitLayout() else expandUnitLayout() }

    private fun expandUnitLayout() {
        isUnitLayoutVisible = true
        unitRadioLayout.visibility = View.VISIBLE
        unitRadioLayout.animate().alpha(1f).translationY(0f).setDuration(250).start()
        speed_unit_container.setBackgroundResource(R.drawable.container_rounded_bg)
        speedUnitValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0)
    }

    private fun collapseUnitLayout() {
        isUnitLayoutVisible = false
        unitRadioLayout.animate().alpha(0f).translationY(-50f).setDuration(250).withEndAction {
            unitRadioLayout.visibility = View.GONE
            speed_unit_container.background = null
            speedUnitValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.spinner_icon, 0)
        }.start()
    }

    private fun handleBackPress() { requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { viewModel.onBackClicked() } }

    private fun showClearHistoryDialog() {
        Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_clear_history)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            findViewById<View>(R.id.cancel_tv).setOnClickListener { dismiss() }
            findViewById<View>(R.id.ok_tv).setOnClickListener { viewModel.clearAllHistory(); dismiss() }
            show()
        }
    }
    private fun applyEdgePadding(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->

            val systemBars = insets.getInsetsIgnoringVisibility(
                WindowInsetsCompat.Type.systemBars()
            )
            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                0
            )
            insets
        }
    }
}