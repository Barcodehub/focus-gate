package io.github.barcodehub.focusanki.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.barcodehub.focusanki.domain.model.Task
import io.github.barcodehub.focusanki.domain.validation.Limits

@Composable
fun TasksScreen(viewModel: TasksViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar tarea")
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay tareas todavía.\nToca + para crear una.", textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        onComplete = { viewModel.complete(task) },
                        onReset = { viewModel.reset(task) },
                        onDelete = { viewModel.delete(task) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, points, description ->
                viewModel.createTask(title, points, description)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onComplete: () -> Unit,
    onReset: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Deshabilitado cuando ya está completada: no debe existir
            // forma de desmarcarla desde aquí.
            IconButton(onClick = onComplete, enabled = !task.completed) {
                Icon(
                    imageVector = if (task.completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (task.completed) "Completada" else "Marcar como completada",
                    tint = if (task.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(task.title, style = MaterialTheme.typography.titleSmall)
                task.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Text("+${task.points} pts", style = MaterialTheme.typography.labelSmall)
            }

            if (task.completed) {
                // Solo se puede reiniciar a partir del día siguiente (evita
                // farmear puntos completando y reiniciando en bucle).
                val canReset = task.canReset()
                TextButton(onClick = onReset, enabled = canReset) {
                    Text(if (canReset) "Reiniciar" else "Mañana")
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, points: Int, description: String?) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var pointsText by rememberSaveable { mutableStateOf("10") }
    var description by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva tarea") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(Limits.MAX_TITLE_LENGTH) },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pointsText,
                    onValueChange = { input -> pointsText = input.filter { it.isDigit() }.take(5) },
                    label = { Text("Puntos (1-${Limits.MAX_POINTS})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(Limits.MAX_DESCRIPTION_LENGTH) },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(title.trim(), pointsText.toInt(), description.trim().ifBlank { null })
                },
                enabled = title.isNotBlank() && pointsText.toIntOrNull() in 1..Limits.MAX_POINTS
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}