package com.example.focusgate.ui.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.focusgate.domain.model.PointsConfig
import kotlinx.coroutines.launch

@Composable
fun PointsConfigScreen(viewModel: PointsConfigViewModel = hiltViewModel()) {
    val config by viewModel.config.collectAsState()

    var again by remember(config) { mutableStateOf(config.again.toString()) }
    var hard by remember(config) { mutableStateOf(config.hard.toString()) }
    var good by remember(config) { mutableStateOf(config.good.toString()) }
    var easy by remember(config) { mutableStateOf(config.easy.toString()) }
    var unlockMinutes by remember(config) { mutableStateOf(config.unlockSessionMinutes.toString()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Puntos por respuesta en Anki", style = MaterialTheme.typography.titleMedium)

            NumberField(label = "Again", value = again, onChange = { again = it })
            NumberField(label = "Hard", value = hard, onChange = { hard = it })
            NumberField(label = "Good", value = good, onChange = { good = it })
            NumberField(label = "Easy", value = easy, onChange = { easy = it })

            Text(
                "Sesión de desbloqueo",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Minutos durante los cuales no se te vuelve a cobrar al reabrir la misma app.",
                style = MaterialTheme.typography.bodySmall
            )
            NumberField(label = "Minutos", value = unlockMinutes, onChange = { unlockMinutes = it })

            Button(
                onClick = {
                    viewModel.save(
                        PointsConfig(
                            again = again.toIntOrNull() ?: 0,
                            hard = hard.toIntOrNull() ?: 0,
                            good = good.toIntOrNull() ?: 0,
                            easy = easy.toIntOrNull() ?: 0,
                            unlockSessionMinutes = unlockMinutes.toIntOrNull() ?: 2
                        )
                    )
                    scope.launch { snackbarHostState.showSnackbar("Configuración guardada") }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Guardar")
            }
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onChange(input.filter { it.isDigit() }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}
