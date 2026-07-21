package com.example.focusgate.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusgate.domain.model.PointsTransaction
import com.example.focusgate.domain.usecase.ObservePointsHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observePointsHistoryUseCase: ObservePointsHistoryUseCase
) : ViewModel() {

    // Este ViewModel es intencionalmente la única pieza que necesitará
    // "Estadísticas" en el futuro: ya expone el historial completo de
    // movimientos (con su origen). Un futuro StatsViewModel puede
    // reutilizar ObservePointsHistoryUseCase para calcular rachas,
    // promedios diarios, etc. sin tocar esta pantalla.
    val transactions: StateFlow<List<PointsTransaction>> = observePointsHistoryUseCase(limit = 100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
