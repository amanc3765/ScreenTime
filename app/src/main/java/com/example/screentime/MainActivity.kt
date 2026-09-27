package com.example.screentime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.screentime.ui.DashboardScreen
import com.example.screentime.ui.ScreenTimeViewModel
import com.example.screentime.ui.theme.ScreenTimeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ScreenTimeViewModel by viewModels {
        ScreenTimeViewModel.Factory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScreenTimeTheme {
                DashboardScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}