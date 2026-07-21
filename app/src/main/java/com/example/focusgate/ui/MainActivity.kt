package com.example.focusgate.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.focusgate.ui.navigation.FocusGateNavHost
import com.example.focusgate.ui.theme.FocusGateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusGateTheme {
                FocusGateNavHost()
            }
        }
    }
}
