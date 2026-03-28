package com.webscare.speedometer.presentation.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.webscare.speedometer.R
import com.webscare.speedometer.data.datastore.SettingsDataStore
import com.webscare.speedometer.data.local.AppDatabase
import com.webscare.speedometer.data.repository.SpeedHistoryRepositoryImpl
import com.webscare.speedometer.domain.repository.SpeedHistoryRepository
import com.webscare.speedometer.domain.usecase.DeleteSpeedHistoryUseCase
import com.webscare.speedometer.domain.usecase.GetAutoSaveUseCase
import com.webscare.speedometer.domain.usecase.GetSpeedAlertEnabledUseCase
import com.webscare.speedometer.domain.usecase.GetSpeedLimitUseCase
import com.webscare.speedometer.domain.usecase.ObserveSpeedUnitUseCase
import com.webscare.speedometer.domain.usecase.SetAutoSaveUseCase
import com.webscare.speedometer.domain.usecase.SetSpeedAlertEnabledUseCase
import com.webscare.speedometer.domain.usecase.SetSpeedLimitUseCase
import com.webscare.speedometer.domain.usecase.SetSpeedUnitUseCase
import com.webscare.speedometer.presentation.main.MainActivity
import com.webscare.speedometer.presentation.settings.SettingsViewModel
import com.webscare.speedometer.presentation.settings.SettingsViewModelFactory
import com.google.android.material.appbar.MaterialToolbar


class HistoryFragment : Fragment() {
    private lateinit var viewModel: SpeedHistoryViewModel


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var toolbar: MaterialToolbar

    private val settingsViewModel: SettingsViewModel by lazy {
        val dao = AppDatabase.getInstance(requireContext()).speedHistoryDao()
        val repository = SpeedHistoryRepositoryImpl(dao)

        val settingsFactory = SettingsViewModelFactory(
            clearHistoryUseCase = DeleteSpeedHistoryUseCase(repository),
            getAutoSaveUseCase = GetAutoSaveUseCase(SettingsDataStore(requireContext())),
            setAutoSaveUseCase = SetAutoSaveUseCase(SettingsDataStore(requireContext())),
            observeSpeedUnitUseCase = ObserveSpeedUnitUseCase(SettingsDataStore(requireContext())),
            setSpeedUnitUseCase = SetSpeedUnitUseCase(SettingsDataStore(requireContext())),
            getSpeedAlertUseCase = GetSpeedAlertEnabledUseCase(SettingsDataStore(requireContext())),
            setSpeedAlertUseCase = SetSpeedAlertEnabledUseCase(SettingsDataStore(requireContext())),
            getSpeedLimitUseCase = GetSpeedLimitUseCase(SettingsDataStore(requireContext())),
            setSpeedLimitUseCase = SetSpeedLimitUseCase(SettingsDataStore(requireContext()))
        )

        ViewModelProvider(requireActivity(), settingsFactory)[SettingsViewModel::class.java]
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dao = AppDatabase.getInstance(requireContext().applicationContext).speedHistoryDao()
        val repository = SpeedHistoryRepositoryImpl(dao)
        val factory = SpeedHistoryViewModelFactory(repository, GetAutoSaveUseCase(SettingsDataStore(requireContext())))

        applyEdgePadding(view)


        viewModel = ViewModelProvider(this, factory)[SpeedHistoryViewModel::class.java]
        recyclerView = view.findViewById(R.id.history_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        toolbar = view.findViewById(R.id.history_toolbar)



        adapter = HistoryAdapter()
        recyclerView.adapter = adapter

        viewModel.historyItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
        setupToolbar()
        observeViewModel()



    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            viewModel.onBackClicked()
        }
    }
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.closeScreen.collect {
                (requireActivity() as MainActivity).closeDrawerScreen()
            }
        }

        lifecycleScope.launchWhenStarted {
            settingsViewModel.currentUnit.collect { unit ->
                adapter.updateUnit(unit)
            }
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