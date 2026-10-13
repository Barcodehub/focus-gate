package io.github.barcodehub.focusanki.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.barcodehub.focusanki.ui.navigation.FocusAnkiNavHost
import io.github.barcodehub.focusanki.ui.theme.FocusAnkiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusAnkiTheme {
                FocusAnkiNavHost()
            }
        }
    }
}
