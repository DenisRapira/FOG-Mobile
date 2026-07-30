package dev.fogmobile.domain

import dev.fogmobile.core.ConnectionUiState
import dev.fogmobile.core.HealthReport
import dev.fogmobile.core.NetworkKind
import dev.fogmobile.core.NetworkProfile
import dev.fogmobile.core.NetworkSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AppPreferences {
    val state: Flow<PreferencesState>
    suspend fun setOnboardingComplete(value: Boolean)
    suspend fun setAutoCheckAfterNetworkChange(value: Boolean)
    suspend fun setStartProtectionOnLaunch(value: Boolean)
    suspend fun rememberProfile(kind: NetworkKind, profileId: String)
}

data class PreferencesState(
    val onboardingComplete: Boolean = false,
    val autoCheckAfterNetworkChange: Boolean = true,
    val startProtectionOnLaunch: Boolean = false,
    val lastWifiProfileId: String? = null,
    val lastMobileProfileId: String? = null,
)

interface ConnectivityObserver {
    val network: StateFlow<NetworkSnapshot>
}

interface ServiceHealthChecker {
    suspend fun check(): HealthReport
}

interface ProfileManager {
    suspend fun chooseProfile(current: NetworkProfile, network: NetworkSnapshot, check: suspend () -> HealthReport): ProfileDecision
}

data class ProfileDecision(
    val profile: NetworkProfile,
    val report: HealthReport,
    val changed: Boolean,
)

interface ConnectionManager {
    val state: StateFlow<ConnectionUiState>
    suspend fun checkAndConfigure()
    suspend fun stop()
    suspend fun onVpnPermissionGranted()
    suspend fun markOnboardingComplete()
    suspend fun setAutoCheckAfterNetworkChange(value: Boolean)
    suspend fun setStartProtectionOnLaunch(value: Boolean)
}
