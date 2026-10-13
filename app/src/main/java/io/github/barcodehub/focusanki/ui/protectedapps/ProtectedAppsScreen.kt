package io.github.barcodehub.focusanki.ui.protectedapps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import io.github.barcodehub.focusanki.domain.model.ProtectedApp
import io.github.barcodehub.focusanki.domain.validation.Limits
import io.github.barcodehub.focusanki.system.InstalledAppInfo

@Composable
fun ProtectedAppsScreen(viewModel: ProtectedAppsViewModel = hiltViewModel()) {
    val apps by viewModel.apps.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editingApp by remember { mutableStateOf<ProtectedApp?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar app protegida")
            }
        }
    ) { padding ->
        if (apps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay apps protegidas todavía.\nToca + para agregar una.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    ProtectedAppRow(
                        app = app,
                        onToggle = { enabled -> viewModel.setEnabled(app, enabled) },
                        onEditCost = { editingApp = app },
                        onDelete = { viewModel.remove(app) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddProtectedAppDialog(
            candidates = viewModel.installedApps(apps),
            onDismiss = { showAddDialog = false },
            onConfirm = { info, cost ->
                viewModel.addProtectedApp(info.packageName, info.label, cost)
                showAddDialog = false
            }
        )
    }

    editingApp?.let { app ->
        EditCostDialog(
            app = app,
            onDismiss = { editingApp = null },
            onConfirm = { newCost ->
                viewModel.updateCost(app, newCost)
                editingApp = null
            }
        )
    }
}

@Composable
private fun ProtectedAppRow(
    app: ProtectedApp,
    onToggle: (Boolean) -> Unit,
    onEditCost: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(app.appLabel.take(1).uppercase(), fontWeight = FontWeight.Bold)
            }

            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                Text(app.appLabel, style = MaterialTheme.typography.titleSmall)
                TextButton(onClick = onEditCost, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                    Text("Costo: ${app.costPoints} pts (tocar para editar)")
                }
            }

            Switch(checked = app.enabled, onCheckedChange = onToggle)
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

@Composable
private fun AddProtectedAppDialog(
    candidates: List<InstalledAppInfo>,
    onDismiss: () -> Unit,
    onConfirm: (InstalledAppInfo, Int) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<InstalledAppInfo?>(null) }
    var costText by rememberSaveable { mutableStateOf("20") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (selected == null) "Elegir app" else "Costo en puntos") },
        text = {
            if (selected == null) {
                Column {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Buscar") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    val filtered = candidates.filter { it.label.contains(query, ignoreCase = true) }
                    LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        items(filtered, key = { it.packageName }) { info ->
                            Text(
                                text = info.label,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .clickable { selected = info }
                            )
                        }
                    }
                }
            } else {
                Column {
                    Text("App: ${selected!!.label}")
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { input -> costText = input.filter { it.isDigit() }.take(5) },
                        label = { Text("Puntos requeridos (${Limits.MIN_COST}-${Limits.MAX_POINTS})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (selected != null) {
                val cost = costText.toIntOrNull()
                TextButton(
                    onClick = { onConfirm(selected!!, cost!!) },
                    enabled = cost != null && cost in Limits.MIN_COST..Limits.MAX_POINTS
                ) { Text("Agregar") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun EditCostDialog(
    app: ProtectedApp,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var costText by rememberSaveable(app.packageName) { mutableStateOf(app.costPoints.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar costo: ${app.appLabel}") },
        text = {
            OutlinedTextField(
                value = costText,
                onValueChange = { input -> costText = input.filter { it.isDigit() }.take(5) },
                label = { Text("Puntos requeridos (${Limits.MIN_COST}-${Limits.MAX_POINTS})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            val cost = costText.toIntOrNull()
            TextButton(
                onClick = { onConfirm(cost!!) },
                enabled = cost != null && cost in Limits.MIN_COST..Limits.MAX_POINTS
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
