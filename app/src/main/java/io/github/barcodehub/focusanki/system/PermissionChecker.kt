package io.github.barcodehub.focusanki.system

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import io.github.barcodehub.focusanki.accessibility.GateAccessibilityService
import javax.inject.Inject

/**
 * Comprobaciones de permisos reutilizadas tanto por la UI (para mostrar el
 * estado y guiar al usuario) como, si hiciera falta, por el propio
 * servicio.
 */
class PermissionChecker @Inject constructor() {

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expected = ComponentName(context, GateAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            val componentName = ComponentName.unflattenFromString(splitter.next())
            if (componentName == expected) return true
        }
        return false
    }

    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)
}
