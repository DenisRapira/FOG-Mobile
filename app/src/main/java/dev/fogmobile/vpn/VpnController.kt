package dev.fogmobile.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.fogmobile.core.NetworkProfile
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withTimeoutOrNull

class VpnController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val serviceState: StateFlow<EngineState> = FogVpnService.engineState

    fun needsPermission(): Boolean = VpnService.prepare(context) != null

    suspend fun start(profile: NetworkProfile): Boolean {
        FogVpnService.markStarting()
        val intent = Intent(context, FogVpnService::class.java)
            .setAction(FogVpnService.ACTION_START)
            .putExtra(FogVpnService.EXTRA_PROFILE_ID, profile.id)
        ContextCompat.startForegroundService(context, intent)
        return withTimeoutOrNull(5_000) {
            serviceState.first { it == EngineState.RUNNING || it == EngineState.FAILED }
        } == EngineState.RUNNING
    }

    fun stop() {
        context.startService(Intent(context, FogVpnService::class.java).setAction(FogVpnService.ACTION_STOP))
    }
}
