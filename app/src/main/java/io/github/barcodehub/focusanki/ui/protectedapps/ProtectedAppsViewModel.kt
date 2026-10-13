package io.github.barcodehub.focusanki.ui.protectedapps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.barcodehub.focusanki.domain.model.ProtectedApp
import io.github.barcodehub.focusanki.domain.usecase.DeleteProtectedAppUseCase
import io.github.barcodehub.focusanki.domain.usecase.ObserveProtectedAppsUseCase
import io.github.barcodehub.focusanki.domain.usecase.UpsertProtectedAppUseCase
import io.github.barcodehub.focusanki.system.InstalledAppInfo
import io.github.barcodehub.focusanki.system.InstalledAppsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProtectedAppsViewModel @Inject constructor(
    observeProtectedAppsUseCase: ObserveProtectedAppsUseCase,
    private val upsertProtectedAppUseCase: UpsertProtectedAppUseCase,
    private val deleteProtectedAppUseCase: DeleteProtectedAppUseCase,
    private val installedAppsProvider: InstalledAppsProvider
) : ViewModel() {

    val apps: StateFlow<List<ProtectedApp>> = observeProtectedAppsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun installedApps(alreadyProtected: List<ProtectedApp>): List<InstalledAppInfo> {
        val protectedPackages = alreadyProtected.map { it.packageName }.toSet()
        return installedAppsProvider.getLaunchableApps().filterNot { it.packageName in protectedPackages }
    }

    fun addProtectedApp(packageName: String, label: String, cost: Int) {
        viewModelScope.launch {
            upsertProtectedAppUseCase(
                ProtectedApp(packageName = packageName, appLabel = label, costPoints = cost, enabled = true)
            )
        }
    }

    fun setEnabled(app: ProtectedApp, enabled: Boolean) {
        viewModelScope.launch { upsertProtectedAppUseCase(app.copy(enabled = enabled)) }
    }

    fun updateCost(app: ProtectedApp, newCost: Int) {
        viewModelScope.launch { upsertProtectedAppUseCase(app.copy(costPoints = newCost)) }
    }

    fun remove(app: ProtectedApp) {
        viewModelScope.launch { deleteProtectedAppUseCase(app.packageName) }
    }
}
