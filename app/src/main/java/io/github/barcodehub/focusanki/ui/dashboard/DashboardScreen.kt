package io.github.barcodehub.focusanki.ui.dashboard

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.github.barcodehub.focusanki.BuildConfig
import io.github.barcodehub.focusanki.accessibility.DiagnosticsBus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val balance by viewModel.balance.collectAsState()
    val permissions = viewModel.permissionsState

    // Los permisos se conceden en pantallas externas (Ajustes), así que
    // se refrescan cada vez que esta pantalla vuelve a primer plano.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        viewModel.refreshPermissions(context)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Tu saldo", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "$balance",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("puntos", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text("Permisos requeridos", style = MaterialTheme.typography.titleMedium)

            PermissionStatusCard(
                title = "Servicio de accesibilidad",
                description = "Necesario para detectar tus respuestas en AnkiDroid y las apps que abres.",
                granted = permissions.accessibilityEnabled,
                onFix = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
            )

            PermissionStatusCard(
                title = "Mostrar sobre otras apps",
                description = "Necesario para mostrar la pantalla de bloqueo cuando no tienes puntos suficientes.",
                granted = permissions.overlayEnabled,
                onFix = {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                    )
                }
            )

            // Panel de depuración: solo en builds debug (muestra qué apps abres).
            if (BuildConfig.DEBUG) DiagnosticsCard()

            AboutFooter()
        }
    }
}

@Composable
private fun DiagnosticsCard() {
    val diagnostics by DiagnosticsBus.state.collectAsState()
    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Diagnóstico en vivo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))

            DiagnosticLine(
                "Servicio conectado",
                if (diagnostics.serviceConnected)
                    "Sí, desde ${diagnostics.serviceConnectedAt?.let { timeFormatter.format(Date(it)) }}"
                else
                    "NO — revisa el permiso de Accesibilidad arriba"
            )
            DiagnosticLine("Eventos totales vistos", diagnostics.totalEventsSeen.toString())
            DiagnosticLine(
                "Último evento",
                diagnostics.lastEventPackage?.let {
                    "$it (${diagnostics.lastEventType}) a las ${diagnostics.lastEventAt?.let { t -> timeFormatter.format(Date(t)) }}"
                } ?: "ninguno todavía"
            )
            DiagnosticLine(
                "Apps protegidas cacheadas",
                "${diagnostics.protectedAppsCached} -> ${diagnostics.protectedPackagesCached}"
            )
            DiagnosticLine("Último clic en Anki (crudo)", diagnostics.lastAnkiRawText ?: "ninguno todavía")
            DiagnosticLine("Botón mapeado", diagnostics.lastAnkiMappedButton ?: "-")
            DiagnosticLine("Último chequeo de desbloqueo", diagnostics.lastUnlockCheck ?: "ninguno todavía")
        }
    }
}

@Composable
private fun DiagnosticLine(label: String, value: String) {
    Column(modifier = Modifier.padding(top = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PermissionStatusCard(
    title: String,
    description: String,
    granted: Boolean,
    onFix: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (granted)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (granted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (!granted) {
                TextButton(onClick = onFix, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Activar ahora")
                }
            }
        }
    }
}

@Composable
private fun AboutFooter() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("FocusAnki v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelMedium)
        Text(
            "Hecho por Brayan Barco · Licencia MIT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
