package dev.fogmobile.core

enum class AppConnectionState {
    IDLE,
    CHECKING,
    CONFIGURING,
    CONNECTED,
    PARTIAL,
    FAILED,
    VPN_PERMISSION_REQUIRED,
    VPN_REVOKED,
    NO_NETWORK
}

enum class ServiceId { INSTAGRAM, YOUTUBE }

enum class ProbeKind { DNS, TLS, MEDIA_CDN, VIDEO_CDN, QUIC }

enum class ProbeStatus { WAITING, RUNNING, OK, FAILED, SKIPPED }

enum class NetworkKind { WIFI, MOBILE, OTHER, NONE }

data class NetworkSnapshot(
    val kind: NetworkKind = NetworkKind.NONE,
    val metered: Boolean = false,
    val connected: Boolean = false,
    val interfaceName: String? = null,
)

data class HealthProbe(
    val kind: ProbeKind,
    val status: ProbeStatus,
    val technicalReason: String? = null,
)

data class ServiceHealth(
    val service: ServiceId,
    val probes: List<HealthProbe>,
) {
    val isHealthy: Boolean = probes.all { it.status == ProbeStatus.OK || it.status == ProbeStatus.SKIPPED }
}

data class HealthReport(
    val instagram: ServiceHealth,
    val youtube: ServiceHealth,
    val checkedAtMillis: Long,
) {
    val allHealthy: Boolean = instagram.isHealthy && youtube.isHealthy
}

data class NetworkProfile(
    val id: String,
    val title: String,
    val priority: Int,
    val blockUdp443ForFallback: Boolean,
    val tlsClientHelloMode: String,
)

data class ConnectionUiState(
    val appState: AppConnectionState = AppConnectionState.IDLE,
    val selectedProfile: NetworkProfile = ProfileCatalog.auto,
    val network: NetworkSnapshot = NetworkSnapshot(),
    val healthReport: HealthReport? = null,
    val checkSteps: List<CheckStep> = CheckStep.defaultSteps,
    val tunnelActive: Boolean = false,
    val onboardingComplete: Boolean = false,
    val autoCheckAfterNetworkChange: Boolean = true,
    val startProtectionOnLaunch: Boolean = false,
    val technicalMessage: String? = null,
)

data class CheckStep(
    val title: String,
    val status: ProbeStatus,
) {
    companion object {
        val defaultSteps = listOf(
            CheckStep("Проверяем сеть", ProbeStatus.WAITING),
            CheckStep("Instagram", ProbeStatus.WAITING),
            CheckStep("YouTube", ProbeStatus.WAITING),
            CheckStep("Настраиваем соединение", ProbeStatus.WAITING),
            CheckStep("Финальная проверка", ProbeStatus.WAITING),
        )
    }
}

object ProfileCatalog {
    val auto = NetworkProfile("auto", "AUTO", 0, blockUdp443ForFallback = true, tlsClientHelloMode = "adaptive")
    val profiles = listOf(
        auto,
        NetworkProfile("profile-a", "Profile A", 10, blockUdp443ForFallback = true, tlsClientHelloMode = "baseline"),
        NetworkProfile("profile-b", "Profile B", 20, blockUdp443ForFallback = true, tlsClientHelloMode = "split-safe"),
        NetworkProfile("profile-c", "Profile C", 30, blockUdp443ForFallback = false, tlsClientHelloMode = "quic-observe"),
    )
}
