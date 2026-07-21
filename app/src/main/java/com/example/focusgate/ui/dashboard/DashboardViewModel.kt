package com.example.focusgate.ui.dashboard

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusgate.domain.usecase.ObservePointsBalanceUseCase
import com.example.focusgate.system.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class PermissionsState(
    val accessibilityEnabled: Boolean = false,
    val overlayEnabled: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observePointsBalanceUseCase: ObservePointsBalanceUseCase,
    private val permissionChecker: PermissionChecker
) : ViewModel() {

    val balance: StateFlow<Int> = observePointsBalanceUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    var permissionsState by mutableStateOf(PermissionsState())
        private set

    /** Los permisos del sistema no son observables por Flow; se refrescan
     * explícitamente (p. ej. cada vez que la pantalla vuelve a primer
     * plano) desde la UI. */
    fun refreshPermissions(context: Context) {
        permissionsState = PermissionsState(
            accessibilityEnabled = permissionChecker.isAccessibilityServiceEnabled(context),
            overlayEnabled = permissionChecker.canDrawOverlays(context)
        )
    }
}
