package com.example.focusgate.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String, val label: String, val icon: ImageVector) {
    data object Dashboard : Destination("dashboard", "Inicio", Icons.Filled.Home)
    data object ProtectedApps : Destination("protected_apps", "Apps", Icons.Filled.Apps)
    data object Config : Destination("config", "Config", Icons.Filled.Tune)
    data object History : Destination("history", "Historial", Icons.Filled.History)

    companion object {
        val bottomBarItems = listOf(Dashboard, ProtectedApps, Config, History)
    }
}
