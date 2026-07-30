package dev.fogmobile.vpn

import android.os.ParcelFileDescriptor
import dev.fogmobile.core.NetworkProfile
import java.io.FileInputStream
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BaselineLocalNetworkEngine @Inject constructor() : NetworkEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val running = AtomicBoolean(false)
    private val mutableState = MutableStateFlow(EngineState.STOPPED)
    private var currentTun: ParcelFileDescriptor? = null
    override val state: StateFlow<EngineState> = mutableState
    override val canForwardPackets: Boolean = false

    override fun start(tun: ParcelFileDescriptor, profile: NetworkProfile) {
        if (!running.compareAndSet(false, true)) return
        currentTun = tun
        mutableState.value = EngineState.STARTING
        scope.launch {
            mutableState.value = EngineState.RUNNING
            FileInputStream(tun.fileDescriptor).use { input ->
                val buffer = ByteArray(32767)
                while (running.get()) {
                    val readSucceeded = runCatching { input.read(buffer) >= 0 }.getOrDefault(false)
                    if (!readSucceeded) break
                }
            }
            mutableState.value = EngineState.STOPPED
            currentTun = null
            runCatching { tun.close() }
        }
    }

    override fun stop() {
        mutableState.value = EngineState.STOPPING
        running.set(false)
        runCatching { currentTun?.close() }
    }

    override fun healthCheck(): EngineState = mutableState.value

    fun shutdown() {
        stop()
        scope.cancel()
    }
}
