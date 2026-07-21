package com.example.focusgate.system

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class InstalledAppInfo(val packageName: String, val label: String)

/**
 * Enumera las apps "abribles" (con Activity launcher) usando el mecanismo
 * de <queries> declarado en el Manifest, sin requerir el permiso sensible
 * QUERY_ALL_PACKAGES.
 */
class InstalledAppsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getLaunchableApps(): List<InstalledAppInfo> {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfos = packageManager.queryIntentActivities(intent, 0)

        return resolveInfos
            .map { resolveInfo ->
                InstalledAppInfo(
                    packageName = resolveInfo.activityInfo.packageName,
                    label = resolveInfo.loadLabel(packageManager).toString()
                )
            }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}
