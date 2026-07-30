package dev.fogmobile.vpn

import android.os.ParcelFileDescriptor
import dev.fogmobile.core.NetworkProfile
import kotlinx.coroutines.flow.StateFlow

enum class EngineState { STOPPED, STARTING, RUNNING, STOPPING, FAILED }

interface NetworkEngine {
    val state: StateFlow<EngineState>
    val canForwardPackets: Boolean
    fun start(tun: ParcelFileDescriptor, profile: NetworkProfile)
    fun stop()
    fun healthCheck(): EngineState
}
