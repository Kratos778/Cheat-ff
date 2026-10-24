package com.kratos.fflauncher.ui.views

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kratos.fflauncher.OverlayService
import com.kratos.fflauncher.ui.viewmodel.MainViewModel

@Composable
fun FFLauncherScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF101E31), Color(0xFF080D16))
                )
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .background(Color(0xFF173D43), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Android,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text("FF LAUNCHER", fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
                Text("GAME STARTER • HUD LAB", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Launcher", "HUD Lab", "Definições").forEachIndexed { index, title ->
                FilterChip(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    label = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> LauncherTab(uiState = uiState, onRefresh = { viewModel.refreshGameStatus() })
            1 -> HudLabTab(
                uiState = uiState,
                onToggleOverlay = {
                    val enabled = !uiState.overlayEnabled
                    if (enabled) {
                        if (Settings.canDrawOverlays(context)) {
                            ContextCompat.startForegroundService(
                                context,
                                Intent(context, OverlayService::class.java)
                            )
                            viewModel.setOverlayEnabled(true)
                            viewModel.setMessage("HUD visual ativado.")
                        } else {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                            )
                            viewModel.setMessage("Autorize sobreposição e retorne.")
                        }
                    } else {
                        context.stopService(Intent(context, OverlayService::class.java))
                        viewModel.setOverlayEnabled(false)
                        viewModel.setMessage("HUD visual desligado.")
                    }
                },
                onToggleLines = { viewModel.updateLines(it) },
                onToggleBoxes = { viewModel.updateBoxes(it) },
                onToggleObstacles = { viewModel.updateObstacles(it) }
            )
            else -> SettingsTab(
                uiState = uiState,
                onToggleAutoLaunch = { viewModel.updateAutoLaunch(it) },
                onToggleLines = { viewModel.updateLines(it) },
                onToggleBoxes = { viewModel.updateBoxes(it) },
                onToggleObstacles = { viewModel.updateObstacles(it) }
            )
        }

        Text(
            text = uiState.message,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun LauncherTab(
    uiState: com.kratos.fflauncher.ui.viewmodel.MainUiState,
    onRefresh: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("JOGO-ALVO", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text("Free Fire", fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Text("com.dts.freefireth", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (uiState.gameStatus.installed) Icons.Filled.CheckCircle else Icons.Filled.Info,
                    contentDescription = null,
                    tint = if (uiState.gameStatus.installed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (uiState.gameStatus.installed) "Instalado" else "Não encontrado",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Text(uiState.gameStatus.message)

            Button(
                onClick = onRefresh,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text("  Verificar novamente")
            }
        }
    }
}

@Composable
private fun HudLabTab(
    uiState: com.kratos.fflauncher.ui.viewmodel.MainUiState,
    onToggleOverlay: () -> Unit,
    onToggleLines: (Boolean) -> Unit,
    onToggleBoxes: (Boolean) -> Unit,
    onToggleObstacles: (Boolean) -> Unit
) {
    val panel = MaterialTheme.colorScheme.surface
    Card(
        colors = CardDefaults.cardColors(containerColor = panel),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("HUD LAB • SIMULAÇÃO", fontWeight = FontWeight.Bold)
            Text(
                text = "Visualização de referência para testar um overlay independente do jogo.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            SettingRow(
                title = "Linhas de referência",
                checked = uiState.showLines,
                onCheckedChange = onToggleLines
            )
            SettingRow(
                title = "Caixas e marcadores",
                checked = uiState.showBoxes,
                onCheckedChange = onToggleBoxes
            )
            SettingRow(
                title = "Simular obstáculo",
                checked = uiState.showObstacles,
                onCheckedChange = onToggleObstacles
            )

            Button(
                onClick = onToggleOverlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (uiState.overlayEnabled) "Desligar overlay" else "Ativar overlay")
            }
        }
    }
}

@Composable
private fun SettingsTab(
    uiState: com.kratos.fflauncher.ui.viewmodel.MainUiState,
    onToggleAutoLaunch: (Boolean) -> Unit,
    onToggleLines: (Boolean) -> Unit,
    onToggleBoxes: (Boolean) -> Unit,
    onToggleObstacles: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("DEFINIÇÕES", fontWeight = FontWeight.Bold)
            SettingRow(
                title = "Abrir jogo ao iniciar",
                checked = uiState.autoLaunch,
                onCheckedChange = onToggleAutoLaunch
            )
            SettingRow(
                title = "Linhas do HUD",
                checked = uiState.showLines,
                onCheckedChange = onToggleLines
            )
            SettingRow(
                title = "Caixas e marcadores",
                checked = uiState.showBoxes,
                onCheckedChange = onToggleBoxes
            )
            SettingRow(
                title = "Obstáculos",
                checked = uiState.showObstacles,
                onCheckedChange = onToggleObstacles
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
