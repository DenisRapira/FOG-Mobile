package dev.fogmobile.data

import dev.fogmobile.core.AppConnectionState
import dev.fogmobile.core.CheckStep
import dev.fogmobile.core.ConnectionUiState
import dev.fogmobile.core.ProbeStatus
import dev.fogmobile.domain.AppPreferences
import dev.fogmobile.domain.ConnectionManager
import dev.fogmobile.domain.ConnectivityObserver
import dev.fogmobile.domain.ProfileManager
import dev.fogmobile.domain.ServiceHealthChecker
import dev.fogmobile.vpn.EngineState
import dev.fogmobile.vpn.VpnController
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ConnectionManagerImpl @Inject constructor(
    private val preferences: AppPreferences,
    private val connectivityObserver: ConnectivityObserver,
    private val healthChecker: ServiceHealthChecker,
    private val profileManager: ProfileManager,
    private val vpnController: VpnController,
) : ConnectionManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutableState = MutableStateFlow(ConnectionUiState())
    override val state: StateFlow<ConnectionUiState> = mutableState

    init {
        scope.launch {
            combine(preferences.state, connectivityObserver.network, vpnController.serviceState) { prefs, network, engine ->
                mutableState.value.copy(
                    onboardingComplete = prefs.onboardingComplete,
                    autoCheckAfterNetworkChange = prefs.autoCheckAfterNetworkChange,
                    startProtectionOnLaunch = prefs.startProtectionOnLaunch,
                    network = network,
                    tunnelActive = engine == EngineState.RUNNING,
                )
            }.collect { mutableState.value = it }
        }
    }

    override suspend fun checkAndConfigure() {
        val network = connectivityObserver.network.value
        if (!network.connected) {
            mutableState.value = mutableState.value.copy(appState = AppConnectionState.NO_NETWORK)
            return
        }

        updateStep(0, ProbeStatus.RUNNING, AppConnectionState.CHECKING)
        val firstReport = healthChecker.check()
        renderReport(firstReport, configuring = false, final = false)

        if (firstReport.allHealthy) {
            mutableState.value = mutableState.value.copy(appState = AppConnectionState.CONNECTED)
            return
        }

        if (vpnController.needsPermission()) {
            mutableState.value = mutableState.value.copy(appState = AppConnectionState.VPN_PERMISSION_REQUIRED)
            return
        }

        configureAfterPermission()
    }

    override suspend fun onVpnPermissionGranted() {
        configureAfterPermission()
    }

    private suspend fun configureAfterPermission() {
        updateStep(3, ProbeStatus.RUNNING, AppConnectionState.CONFIGURING)
        val decision = profileManager.chooseProfile(mutableState.value.selectedProfile, connectivityObserver.network.value) {
            healthChecker.check()
        }
        preferences.rememberProfile(connectivityObserver.network.value.kind, decision.profile.id)
        val tunnelStarted = vpnController.start(decision.profile)
        if (!tunnelStarted) {
            mutableState.value = mutableState.value.copy(
                selectedProfile = decision.profile,
                appState = AppConnectionState.FAILED,
                checkSteps = mutableState.value.checkSteps.mapIndexed { index, step ->
                    if (index >= 3) step.copy(status = ProbeStatus.FAILED) else step
                },
                technicalMessage = "No operational local packet-forwarding engine is installed.",
            )
            return
        }
        val finalReport = healthChecker.check()
        renderReport(finalReport, configuring = true, final = true)
        mutableState.value = mutableState.value.copy(
            selectedProfile = decision.profile,
            appState = when {
                finalReport.allHealthy -> AppConnectionState.CONNECTED
                finalReport.instagram.isHealthy || finalReport.youtube.isHealthy -> AppConnectionState.PARTIAL
                else -> AppConnectionState.FAILED
            },
        )
    }

    override suspend fun stop() {
        vpnController.stop()
        mutableState.value = mutableState.value.copy(appState = AppConnectionState.IDLE)
    }

    override suspend fun markOnboardingComplete() {
        preferences.setOnboardingComplete(true)
    }

    override suspend fun setAutoCheckAfterNetworkChange(value: Boolean) {
        preferences.setAutoCheckAfterNetworkChange(value)
    }

    override suspend fun setStartProtectionOnLaunch(value: Boolean) {
        preferences.setStartProtectionOnLaunch(value)
    }

    private fun updateStep(index: Int, status: ProbeStatus, appState: AppConnectionState) {
        val steps = mutableState.value.checkSteps.mapIndexed { stepIndex, step ->
            if (stepIndex == index) step.copy(status = status) else step
        }
        mutableState.value = mutableState.value.copy(checkSteps = steps, appState = appState)
    }

    private fun renderReport(report: dev.fogmobile.core.HealthReport, configuring: Boolean, final: Boolean) {
        mutableState.value = mutableState.value.copy(
            healthReport = report,
            checkSteps = listOf(
                CheckStep("Проверяем сеть", ProbeStatus.OK),
                CheckStep("Instagram", if (report.instagram.isHealthy) ProbeStatus.OK else ProbeStatus.FAILED),
                CheckStep("YouTube", if (report.youtube.isHealthy) ProbeStatus.OK else ProbeStatus.FAILED),
                CheckStep("Настраиваем соединение", if (configuring) ProbeStatus.OK else ProbeStatus.WAITING),
                CheckStep("Финальная проверка", if (!final) ProbeStatus.WAITING else if (report.allHealthy) ProbeStatus.OK else ProbeStatus.FAILED),
            ),
        )
    }
}
