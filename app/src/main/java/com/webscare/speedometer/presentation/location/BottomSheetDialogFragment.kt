package com.webscare.speedometer.presentation.location

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.webscare.speedometer.R
import com.webscare.speedometer.Utils.addPressEffect
import com.webscare.speedometer.domain.usecase.CheckLocationPermissionUseCase
import com.webscare.speedometer.presentation.main.MainActivity
import kotlinx.coroutines.launch

class LocationBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: LocationPermissionViewModel
    private lateinit var checkPermissionUseCase: CheckLocationPermissionUseCase

    // single source of truth for user intent
    private var userSelectedNotNow = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

            if (isGranted) {
                userSelectedNotNow = false
                dismiss()
            } else {
                val shouldShowRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                viewModel.handlePermissionResult(isGranted, shouldShowRationale)
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dlg ->
            val bottomSheetDialog = dlg as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        }

        // 👇 KEY FIX: outside tap behaves like Not Now
        dialog.setOnCancelListener {
            userSelectedNotNow = true
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(
            view.findViewById(R.id.bottom_sheet)
        ) { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissionUseCase =
            CheckLocationPermissionUseCase(requireContext().applicationContext)

        val factory = LocationPermissionViewModelFactory(checkPermissionUseCase)
        viewModel = ViewModelProvider(this, factory)[LocationPermissionViewModel::class.java]

        isCancelable = true
        dialog?.setCanceledOnTouchOutside(true)
        val descTextView = view.findViewById<TextView>(R.id.descbottomsheet_two)
        val fullText = getString(R.string.location_disclosure_body_2)
        val highlightText = "even when the app is closed or not in use."

        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf(highlightText)

        if (startIndex != -1) {
            val endIndex = startIndex + highlightText.length
            // Set Color to White
            spannable.setSpan(ForegroundColorSpan(Color.WHITE), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            descTextView.text = spannable
        } else {
            descTextView.text = fullText
        }

        observeViewModel()

        view.findViewById<TextView>(R.id.btn_enable).addPressEffect {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        view.findViewById<TextView>(R.id.btn_notNow).addPressEffect {
            userSelectedNotNow = true
            dismiss()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.navigateToSettings.collect {
                    openAppSettings()
                }
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireActivity().packageName, null)
        }
        startActivity(intent)
    }


}