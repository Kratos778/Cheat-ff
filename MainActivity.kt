package com.kratos.fflauncher

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private const val TARGET_PACKAGE = "com.dts.freefireth"
private const val TARGET_VERSION = "1.132.1"
private val Neon = Color(0xFF58E6C5)
private val Muted = Color(0xFF9DB0C8)
private val Panel = Color(0xFF111A29)

data class GameStatus(val installed: Boolean, val versionName: String? = null, val versionCode: Long? = null, val message: String)

class MainActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("fflauncher", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Neon, secondary = Color(0xFF83A9FF), background = Color(0xFF080D16), surface = Panel, onPrimary = Color(0xFF05130F), onBackground = Color(0xFFEAF1FF), onSurface = Color(0xFFEAF1FF))) {
                var status by remember { mutableStateOf(readGameStatus()) }
                var page by rememberSaveable { mutableStateOf(0) }
                var autoLaunch by rememberSaveable { mutableStateOf(prefs.getBoolean("autoLaunch", false)) }
                var showLines by rememberSaveable { mutableStateOf(prefs.getBoolean("showLines", true)) }
                var showBoxes by rememberSaveable { mutableStateOf(prefs.getBoolean("showBoxes", true)) }
                var showObstacles by rememberSaveable { mutableStateOf(prefs.getBoolean("showObstacles", true)) }
                var overlayOn by remember { mutableStateOf(OverlayService.isRunning) }
                var message by remember { mutableStateOf("Pronto.") }
                var autoHandled by rememberSaveable { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    status = readGameStatus()
                    if (autoLaunch && !autoHandled && status.installed) { autoHandled = true; launchGame { message = it } }
                }
                Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF101E31), Color(0xFF080D16))))) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).background(Color(0xFF173D43), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Android, null, tint = Neon, modifier = Modifier.size(29.dp)) }
                            Column(Modifier.padding(start = 12.dp)) { Text("FF LAUNCHER", fontSize = 23.sp, fontWeight = FontWeight.Black, letterSpacing = 1.1.sp); Text("GAME STARTER • HUD LAB", color = Muted, fontSize = 11.sp, letterSpacing = 1.sp) }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Launcher", "HUD Lab", "Definições").forEachIndexed { i, title ->
                                FilterChip(selected = page == i, onClick = { page = i }, label = { Text(title) })
                            }
                        }
                        when (page) {
                            0 -> LauncherPage(status, message, autoLaunch,
                                onAuto = { autoLaunch = it; prefs.edit().putBoolean("autoLaunch", it).apply() },
                                onRefresh = { status = readGameStatus(); message = "Verificação concluída." },
                                onLaunch = { status = readGameStatus(); launchGame { message = it } },
                                onInfo = { openAppInfo() })
                            1 -> HudLabPage(showLines, showBoxes, showObstacles, overlayOn,
                                onLines = { showLines = it; prefs.edit().putBoolean("showLines", it).apply() },
                                onBoxes = { showBoxes = it; prefs.edit().putBoolean("showBoxes", it).apply() },
                                onObstacles = { showObstacles = it; prefs.edit().putBoolean("showObstacles", it).apply() },
                                onOverlay = {
                                    if (overlayOn) { stopService(Intent(this@MainActivity, OverlayService::class.java)); overlayOn = false; message = "HUD flutuante desligado." }
                                    else if (!Settings.canDrawOverlays(this@MainActivity)) {
                                        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                                        message = "Autoriza a sobreposição e volta a tocar em Ativar HUD."
                                    } else { prefs.edit().putBoolean("showLines", showLines).putBoolean("showBoxes", showBoxes).putBoolean("showObstacles", showObstacles).apply(); ContextCompat.startForegroundService(this@MainActivity, Intent(this@MainActivity, OverlayService::class.java)); overlayOn = true; message = "HUD de demonstração ativo." }
                                }, message = message)
                            else -> SettingsPage(autoLaunch, showLines, showBoxes, showObstacles, overlayOn,
                                onAuto = { autoLaunch = it; prefs.edit().putBoolean("autoLaunch", it).apply() },
                                onLines = { showLines = it; prefs.edit().putBoolean("showLines", it).apply() },
                                onBoxes = { showBoxes = it; prefs.edit().putBoolean("showBoxes", it).apply() },
                                onObstacles = { showObstacles = it; prefs.edit().putBoolean("showObstacles", it).apply() })
                        }
                        Text("Os alvos e indicadores são simulados. O HUD não lê dados internos do Free Fire nem localiza jogadores reais.", color = Muted, fontSize = 12.sp)
                    }
                    NavigationBar(containerColor = Color(0xFF0C1421)) {
                        NavigationBarItem(selected = page == 0, onClick = { page = 0 }, icon = { Icon(Icons.Filled.OpenInNew, null) }, label = { Text("Jogo") })
                        NavigationBarItem(selected = page == 1, onClick = { page = 1 }, icon = { Icon(Icons.Filled.CheckCircle, null) }, label = { Text("HUD Lab") })
                        NavigationBarItem(selected = page == 2, onClick = { page = 2 }, icon = { Icon(Icons.Filled.Refresh, null) }, label = { Text("Definições") })
                    }
                }
            }
        }
    }

    private fun readGameStatus(): GameStatus = try {
        val info = getPackageInfoCompat(TARGET_PACKAGE)
        val name = info.versionName ?: "desconhecida"
        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode else @Suppress("DEPRECATION") info.versionCode.toLong()
        GameStatus(true, name, code, if (name == TARGET_VERSION) "Versão de referência encontrada." else "Instalado; versão diferente ou não confirmada.")
    } catch (_: PackageManager.NameNotFoundException) { GameStatus(false, message = "Free Fire não encontrado neste dispositivo.") }
      catch (e: Exception) { GameStatus(false, message = "Falha na verificação: ${e.message ?: "erro"}") }

    private fun getPackageInfoCompat(name: String): PackageInfo {
        return if (Build.VERSION.SDK_INT >= 33) {
            packageManager.getPackageInfo(name, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(name, 0)
        }
    }
    private fun launchGame(onResult: (String) -> Unit) {
        val intent = packageManager.getLaunchIntentForPackage(TARGET_PACKAGE)
        if (intent == null) { onResult("Instala o Free Fire ou confirma o pacote com.dts.freefireth."); return }
        try { intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(intent); onResult("Pedido de abertura enviado ao Android.") }
        catch (_: ActivityNotFoundException) { onResult("O Android não encontrou a atividade do jogo.") }
        catch (e: Exception) { onResult("Não foi possível abrir: ${e.message ?: "erro"}") }
    }
    private fun openAppInfo() { try { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$TARGET_PACKAGE"))) } catch (_: Exception) {} }
}

@Composable private fun LauncherPage(status: GameStatus, message: String, auto: Boolean, onAuto: (Boolean) -> Unit, onRefresh: () -> Unit, onLaunch: () -> Unit, onInfo: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("JOGO-ALVO", color = Muted, fontSize = 12.sp, letterSpacing = 1.4.sp); Text("Free Fire", fontSize = 28.sp, fontWeight = FontWeight.Bold); Text(TARGET_PACKAGE, color = Muted, fontSize = 13.sp); HorizontalDivider(color = Color(0xFF27364B))
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (status.installed) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline, null, tint = if (status.installed) Neon else Color(0xFFFFB86B)); Column(Modifier.padding(start = 10.dp).weight(1f)) { Text(if (status.installed) "INSTALADO" else "NÃO DETETADO", fontWeight = FontWeight.Bold); Text(status.message, color = Muted, fontSize = 12.sp) } }
            if (status.installed) { InfoRow("Versão instalada", status.versionName ?: "Desconhecida"); InfoRow("Version code", status.versionCode?.toString() ?: "Desconhecido"); InfoRow("Versão de referência", TARGET_VERSION) }
            Button(onClick = onLaunch, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = status.installed) { Icon(Icons.Filled.OpenInNew, null); Text("  ABRIR FREE FIRE") }
            OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Filled.Refresh, null); Text("  Verificar novamente") }
            Text(message, color = Muted, fontSize = 12.sp)
            HorizontalDivider(color = Color(0xFF27364B))
            SettingSwitch("Abrir jogo ao iniciar", "Desativado por defeito; podes ativar aqui.", auto, onAuto)
            OutlinedButton(onClick = onInfo, modifier = Modifier.fillMaxWidth(), enabled = status.installed) { Text("Informações da aplicação") }
        }
    }
}

@Composable private fun HudLabPage(lines: Boolean, boxes: Boolean, obstacles: Boolean, overlay: Boolean, onLines: (Boolean) -> Unit, onBoxes: (Boolean) -> Unit, onObstacles: (Boolean) -> Unit, onOverlay: () -> Unit, message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("HUD LAB • SIMULAÇÃO", fontWeight = FontWeight.Bold, letterSpacing = 1.sp); Text("Pré-visualização com alvos artificiais e parede de teste.", color = Muted, fontSize = 12.sp)
            HudPreview(lines, boxes, obstacles, Modifier.fillMaxWidth().height(245.dp))
            SettingSwitch("Linhas de referência", "Liga os traços dos alvos simulados.", lines, onLines)
            SettingSwitch("Caixas e marcadores", "Mostra caixas e pontos de referência fictícios.", boxes, onBoxes)
            SettingSwitch("Simular obstáculo", "A parede muda o estado visual de um alvo de teste.", obstacles, onObstacles)
            Button(onClick = onOverlay, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(if (overlay) "DESLIGAR HUD FLUTUANTE" else "ATIVAR HUD FLUTUANTE") }
            Text(if (overlay) "HUD flutuante ativo ou a aguardar confirmação do Android." else "Para sobrepor ao jogo, o Android exige autorização especial de sobreposição." , color = Muted, fontSize = 12.sp)
            Text(message, color = Neon, fontSize = 12.sp)
        }
    }
}

@Composable private fun HudPreview(lines: Boolean, boxes: Boolean, obstacles: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier.background(Color(0xFF07101B), RoundedCornerShape(16.dp))) {
        val w = size.width; val h = size.height
        for (i in 1..5) drawLine(Color(0xFF142C3A), Offset(0f, h * i / 6f), Offset(w, h * i / 6f), 1f)
        for (i in 1..7) drawLine(Color(0xFF142C3A), Offset(w * i / 8f, 0f), Offset(w * i / 8f, h), 1f)
        val targets = listOf(Offset(w * .28f, h * .36f), Offset(w * .72f, h * .43f), Offset(w * .53f, h * .27f))
        if (lines) targets.forEach { drawLine(Neon.copy(alpha = .7f), Offset(w / 2f, h), it, 2f) }
        if (obstacles) { drawRect(Color(0xFF5A6678).copy(alpha = .55f), Offset(w * .43f, h * .42f), Size(w * .15f, h * .3f)); drawLine(Color(0xFFFFB86B), Offset(w * .42f, h * .42f), Offset(w * .59f, h * .42f), 3f) }
        targets.forEachIndexed { i, p ->
            val blocked = obstacles && i == 1
            val c = if (blocked) Color(0xFFFFB86B) else Neon
            drawCircle(c, 5f, p)
            if (boxes) { drawRect(c, Offset(p.x - 22f, p.y - 38f), Size(44f, 76f), style = androidx.compose.ui.graphics.drawscope.Stroke(2f)); drawLine(c, Offset(p.x - 9f, p.y - 48f), Offset(p.x + 9f, p.y - 48f), 2f) }
        }
    }
}

@Composable private fun SettingsPage(auto: Boolean, lines: Boolean, boxes: Boolean, obstacles: Boolean, overlay: Boolean, onAuto: (Boolean) -> Unit, onLines: (Boolean) -> Unit, onBoxes: (Boolean) -> Unit, onObstacles: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("DEFINIÇÕES", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            SettingSwitch("Abrir jogo ao iniciar", "O lançamento automático pode ser limitado pelo Android.", auto, onAuto)
            SettingSwitch("Linhas do HUD", "Estado inicial do laboratório.", lines, onLines)
            SettingSwitch("Caixas e marcadores", "Estado inicial dos marcadores simulados.", boxes, onBoxes)
            SettingSwitch("Obstáculos", "Ativa a parede fictícia na simulação.", obstacles, onObstacles)
            InfoRow("HUD flutuante", if (overlay) "Ativo" else "Desligado")
            Text("O overlay é uma camada visual independente. Não acompanha nem extrai coordenadas do jogo.", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable private fun SettingSwitch(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Text(description, color = Muted, fontSize = 12.sp) }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable private fun InfoRow(label: String, value: String) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Muted, fontSize = 12.sp); Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) } }
