package io.github.barcodehub.focusanki.ui.config

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
import io.github.barcodehub.focusanki.domain.model.PointsConfig
import io.github.barcodehub.focusanki.domain.validation.Limits
import kotlinx.coroutines.launch

@Composable
fun PointsConfigScreen(viewModel: PointsConfigViewModel = hiltViewModel()) {
    val config by viewModel.config.collectAsState()

    var again by remember(config) { mutableStateOf(config.again.toString()) }
    var hard by remember(config) { mutableStateOf(config.hard.toString()) }
    var good by remember(config) { mutableStateOf(config.good.toString()) }
    var easy by remember(config) { mutableStateOf(config.easy.toString()) }
    var unlockMinutes by remember(config) { mutableStateOf(config.unlockSessionMinutes.toString()) }

    fun inRange(text: String, range: IntRange) = text.toIntOrNull()?.let { it in range } == true
    val ankiRange = 0..Limits.MAX_ANKI_POINTS
    val valid = listOf(again, hard, good, easy).all { inRange(it, ankiRange) } &&
        inRange(unlockMinutes, Limits.MIN_UNLOCK_MINUTES..Limits.MAX_UNLOCK_MINUTES)

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

            if (!valid) {
                Text(
                    "Los puntos deben estar entre 0 y ${Limits.MAX_ANKI_POINTS} y los minutos entre " +
                        "${Limits.MIN_UNLOCK_MINUTES} y ${Limits.MAX_UNLOCK_MINUTES}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                enabled = valid,
                onClick = {
                    viewModel.save(
                        PointsConfig(
                            again = again.toInt(),
                            hard = hard.toInt(),
                            good = good.toInt(),
                            easy = easy.toInt(),
                            unlockSessionMinutes = unlockMinutes.toInt()
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
        onValueChange = { input -> onChange(input.filter { it.isDigit() }.take(4)) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}
