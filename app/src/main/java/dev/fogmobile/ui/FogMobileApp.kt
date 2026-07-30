package dev.fogmobile.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.fogmobile.core.AppConnectionState
import dev.fogmobile.core.ConnectionUiState
import dev.fogmobile.core.HealthProbe
import dev.fogmobile.core.NetworkKind
import dev.fogmobile.core.ProbeStatus
import dev.fogmobile.core.ServiceHealth
import dev.fogmobile.ui.theme.FogBlack
import dev.fogmobile.ui.theme.FogError
import dev.fogmobile.ui.theme.FogMuted
import dev.fogmobile.ui.theme.FogPanel
import dev.fogmobile.ui.theme.FogPink
import dev.fogmobile.ui.theme.FogSuccess
import dev.fogmobile.ui.theme.FogWarning
import dev.fogmobile.ui.theme.FogWire

private enum class Screen(val route: String) {
    Splash("splash"),
    Onboarding("onboarding"),
    Dashboard("dashboard"),
    Check("check"),
    Diagnostics("diagnostics"),
    Settings("settings"),
    About("about"),
}

@Composable
fun FogMobileApp(viewModel: FogMobileViewModel) {
    val state by viewModel.state.collectAsState()
    val navController = rememberNavController()

    Scaffold(
        containerColor = FogBlack,
        bottomBar = { BottomNav(navController) },
    ) { padding ->
        NavHost(
            modifier = Modifier.padding(padding),
            navController = navController,
            startDestination = if (state.onboardingComplete) Screen.Dashboard.route else Screen.Onboarding.route,
        ) {
            composable(Screen.Splash.route) { SplashScreen() }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    state = state,
                    onPermission = viewModel::requestVpnPermission,
                    onCheck = viewModel::checkConnection,
                    onDone = {
                        viewModel.completeOnboarding()
                        navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
                    },
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    state = state,
                    onCheck = {
                        viewModel.checkConnection()
                        navController.navigate(Screen.Check.route)
                    },
                    onStop = viewModel::stopTunnel,
                    onDiagnostics = { navController.navigate(Screen.Diagnostics.route) },
                )
            }
            composable(Screen.Check.route) {
                ConnectionCheckScreen(state = state, onRetry = viewModel::checkConnection)
            }
            composable(Screen.Diagnostics.route) {
                DiagnosticsScreen(state = state, onRetry = viewModel::checkConnection)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    state = state,
                    onAutoChange = viewModel::setAutoCheckAfterNetworkChange,
                    onLaunchChange = viewModel::setStartProtectionOnLaunch,
                    onDiagnostics = { navController.navigate(Screen.Diagnostics.route) },
                    onAbout = { navController.navigate(Screen.About.route) },
                )
            }
            composable(Screen.About.route) { AboutScreen() }
        }
    }
}

@Composable
private fun BottomNav(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FogBlack)
            .border(1.dp, FogWire)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        NavButton("STATUS") { navController.navigate(Screen.Dashboard.route) }
        NavButton("CHECK") { navController.navigate(Screen.Check.route) }
        NavButton("DIAG") { navController.navigate(Screen.Diagnostics.route) }
        NavButton("SET") { navController.navigate(Screen.Settings.route) }
    }
}

@Composable
private fun NavButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, FogWire),
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
    ) {
        Mono(text, size = 11)
    }
}

@Composable
private fun SplashScreen() {
    FogSurface {
        Wordmark()
        Spacer(Modifier.height(20.dp))
        Mono("LOCAL CONNECTION UTILITY", color = FogPink)
    }
}

@Composable
private fun OnboardingScreen(
    state: ConnectionUiState,
    onPermission: () -> Unit,
    onCheck: () -> Unit,
    onDone: () -> Unit,
) {
    FogPage {
        Wordmark()
        SectionTitle("Первый запуск")
        StageRow("01", "Проверка устройства", "Сеть и системные возможности Android")
        StageRow("02", "Разрешение Android", "Системное VPN-разрешение нужно для локальной обработки соединения")
        StageRow("03", "Автоматическая настройка", "AUTO подбирает минимальный профиль")
        StageRow("04", "Instagram и YouTube", "Статус появится только после реальной проверки")
        StageRow("05", "Готово", "После этого откроется dashboard")
        PrimaryButton("РАЗРЕШИТЬ VPN") { onPermission() }
        SecondaryButton("ПРОВЕРИТЬ УСТРОЙСТВО") { onCheck() }
        PrimaryButton(if (state.appState == AppConnectionState.CONNECTED) "ГОТОВО" else "ПРОДОЛЖИТЬ") { onDone() }
    }
}

@Composable
internal fun DashboardScreen(state: ConnectionUiState, onCheck: () -> Unit, onStop: () -> Unit, onDiagnostics: () -> Unit) {
    FogPage {
        Wordmark()
        SectionTitle("Статус соединения")
        StatusIndicator(state.appState)
        ServiceStatus("Instagram", state.healthReport?.instagram?.isHealthy)
        ServiceStatus("YouTube", state.healthReport?.youtube?.isHealthy)
        InfoLine("Профиль", state.selectedProfile.title)
        InfoLine("Текущая сеть", state.network.kind.label())
        PrimaryButton(if (state.tunnelActive) "ОТКЛЮЧИТЬ" else "ПРОВЕРИТЬ СОЕДИНЕНИЕ") {
            if (state.tunnelActive) onStop() else onCheck()
        }
        SecondaryButton("ДИАГНОСТИКА") { onDiagnostics() }
    }
}

@Composable
private fun ConnectionCheckScreen(state: ConnectionUiState, onRetry: () -> Unit) {
    FogPage {
        Wordmark()
        SectionTitle("Проверка")
        state.checkSteps.forEachIndexed { index, step ->
            StageRow((index + 1).toString().padStart(2, '0'), step.title, step.status.name)
        }
        AnimatedContent(targetState = state.appState, label = "state") { appState ->
            Text(appState.userText(), color = appState.color(), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        PrimaryButton("ПОВТОРИТЬ") { onRetry() }
    }
}

@Composable
private fun DiagnosticsScreen(state: ConnectionUiState, onRetry: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    FogPage {
        Wordmark()
        SectionTitle("Diagnostics")
        state.healthReport?.let {
            DiagnosticService("Instagram", it.instagram)
            DiagnosticService("YouTube", it.youtube)
        } ?: Text("Диагностика ещё не запускалась.", color = FogMuted)
        InfoLine("Network", state.network.kind.label())
        InfoLine("Profile", state.selectedProfile.title)
        InfoLine("Tunnel", if (state.tunnelActive) "RUNNING" else "STOPPED")
        state.technicalMessage?.let { message ->
            Text(message, color = FogWarning, fontSize = 12.sp)
        }
        PrimaryButton("ПОВТОРИТЬ ДИАГНОСТИКУ") { onRetry() }
        SecondaryButton("ЭКСПОРТ ЛОГА") {
            clipboard.setText(AnnotatedString(state.toDiagnosticLog()))
        }
    }
}

@Composable
private fun SettingsScreen(
    state: ConnectionUiState,
    onAutoChange: (Boolean) -> Unit,
    onLaunchChange: (Boolean) -> Unit,
    onDiagnostics: () -> Unit,
    onAbout: () -> Unit,
) {
    FogPage {
        Wordmark()
        SectionTitle("Settings")
        InfoLine("Mode", "AUTO")
        InfoLine("Apps", "Instagram / YouTube")
        ToggleLine("Auto check after network change", state.autoCheckAfterNetworkChange, onAutoChange)
        ToggleLine("Start protection on app launch", state.startProtectionOnLaunch, onLaunchChange)
        SecondaryButton("OPEN DIAGNOSTICS") { onDiagnostics() }
        SecondaryButton("ABOUT") { onAbout() }
    }
}

@Composable
private fun AboutScreen() {
    FogPage {
        Wordmark()
        SectionTitle("About")
        Text("FOG Mobile 0.1.0", color = Color.White, fontSize = 18.sp)
        Text("Локальная Android-реализация, вдохновлённая FOG Prime. Без удалённого VPN backend, без MITM TLS, без аналитики.", color = FogMuted)
        Text("Source reference: github.com/DenisRapira/FOG-Prime", color = FogMuted)
    }
}

@Composable
private fun FogPage(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FogBlack)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
private fun FogSurface(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(FogBlack).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

@Composable
private fun Wordmark() {
    Column {
        Mono("FOG", size = 42, weight = FontWeight.Black)
        Mono("Mobile", size = 22, color = FogPink, weight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Mono(text.uppercase(), color = FogPink, size = 12)
}

@Composable
private fun StageRow(index: String, title: String, note: String) {
    Row(
        modifier = Modifier.fillMaxWidth().border(1.dp, FogWire).background(FogPanel),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(48.dp).height(68.dp).border(1.dp, FogWire), contentAlignment = Alignment.Center) {
            Mono(index, size = 12, color = FogPink)
        }
        Column(Modifier.weight(1f).padding(14.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(note, color = FogMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun StatusIndicator(state: AppConnectionState) {
    Box(
        modifier = Modifier.fillMaxWidth().height(140.dp).border(1.dp, state.color()).background(FogPanel),
        contentAlignment = Alignment.Center,
    ) {
        Text(state.userText(), color = state.color(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ServiceStatus(name: String, healthy: Boolean?) {
    val color = when (healthy) {
        true -> FogSuccess
        false -> FogError
        null -> FogMuted
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(name, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(if (healthy == true) "Работает" else if (healthy == false) "Требует проверки" else "Не проверено", color = color)
    }
    HorizontalDivider(color = FogWire)
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Mono(label.uppercase(), size = 11, color = FogMuted)
        Mono(value, size = 12, color = Color.White)
    }
}

@Composable
private fun ToggleLine(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun DiagnosticService(name: String, service: ServiceHealth) {
    Text(name, color = Color.White, fontWeight = FontWeight.Bold)
    service.probes.forEach { probe -> DiagnosticProbe(probe) }
}

@Composable
private fun DiagnosticProbe(probe: HealthProbe) {
    InfoLine(probe.kind.name, probe.status.name)
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = FogPink, contentColor = Color.White),
    ) {
        Mono(text, size = 12, weight = FontWeight.Bold)
    }
}

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth().height(52.dp),
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(1.dp, FogWire),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
    ) {
        Mono(text, size = 12, weight = FontWeight.Bold)
    }
}

@Composable
private fun Mono(text: String, size: Int = 14, color: Color = Color.White, weight: FontWeight = FontWeight.SemiBold) {
    Text(text, color = color, fontSize = size.sp, fontFamily = FontFamily.Monospace, fontWeight = weight)
}

private fun AppConnectionState.userText(): String = when (this) {
    AppConnectionState.IDLE -> "Готов к проверке"
    AppConnectionState.CHECKING -> "Проверяем соединение"
    AppConnectionState.CONFIGURING -> "Настраиваем соединение"
    AppConnectionState.CONNECTED -> "Соединение готово"
    AppConnectionState.PARTIAL -> "Частичный доступ"
    AppConnectionState.FAILED -> "Не удалось установить стабильное соединение"
    AppConnectionState.VPN_PERMISSION_REQUIRED -> "Нужно разрешение Android"
    AppConnectionState.VPN_REVOKED -> "VPN отключён системой"
    AppConnectionState.NO_NETWORK -> "Нет подключения к сети"
}

private fun AppConnectionState.color(): Color = when (this) {
    AppConnectionState.CONNECTED -> FogSuccess
    AppConnectionState.PARTIAL, AppConnectionState.VPN_PERMISSION_REQUIRED -> FogWarning
    AppConnectionState.FAILED, AppConnectionState.VPN_REVOKED, AppConnectionState.NO_NETWORK -> FogError
    else -> FogPink
}

private fun NetworkKind.label(): String = when (this) {
    NetworkKind.WIFI -> "Wi-Fi"
    NetworkKind.MOBILE -> "Mobile"
    NetworkKind.OTHER -> "Other"
    NetworkKind.NONE -> "No network"
}

private fun ConnectionUiState.toDiagnosticLog(): String {
    val lines = mutableListOf<String>()
    lines += "FOG Mobile diagnostic log"
    lines += "state=${appState.name}"
    lines += "network=${network.kind.name}"
    lines += "profile=${selectedProfile.id}"
    lines += "tunnel=${if (tunnelActive) "running" else "stopped"}"
    technicalMessage?.let { lines += "engine=$it" }
    healthReport?.let { report ->
        listOf(report.instagram, report.youtube).forEach { service ->
            lines += "service=${service.service.name}"
            service.probes.forEach { probe ->
                lines += "${probe.kind.name}=${probe.status.name}; reason=${probe.technicalReason?.take(120) ?: ""}"
            }
        }
    }
    return lines.joinToString(separator = "\n")
}

@Preview
@Composable
private fun DashboardPreview() {
    dev.fogmobile.ui.theme.FogTheme {
        DashboardScreen(ConnectionUiState(onboardingComplete = true), {}, {}, {})
    }
}
