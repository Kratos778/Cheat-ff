package com.kratos.fflauncher

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kratos.fflauncher.ui.theme.FFLauncherTheme
import com.kratos.fflauncher.ui.views.FFLauncherScreen
import com.kratos.fflauncher.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.refreshGameStatus()
            }
        }

        setContent {
            FFLauncherTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FFLauncherScreen(viewModel = viewModel)
                }
            }
        }
    }
}
