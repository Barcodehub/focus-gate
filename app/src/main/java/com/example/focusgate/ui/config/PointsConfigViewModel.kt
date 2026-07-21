package com.example.focusgate.ui.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusgate.domain.model.PointsConfig
import com.example.focusgate.domain.usecase.ObservePointsConfigUseCase
import com.example.focusgate.domain.usecase.UpdatePointsConfigUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PointsConfigViewModel @Inject constructor(
    observePointsConfigUseCase: ObservePointsConfigUseCase,
    private val updatePointsConfigUseCase: UpdatePointsConfigUseCase
) : ViewModel() {

    val config: StateFlow<PointsConfig> = observePointsConfigUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PointsConfig.DEFAULT)

    fun save(config: PointsConfig) {
        viewModelScope.launch { updatePointsConfigUseCase(config) }
    }
}
