package dev.fogmobile.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import androidx.core.app.NotificationCompat
import dev.fogmobile.core.ProfileCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FogVpnService : VpnService() {
    private lateinit var appResolver: AppResolver
    private lateinit var engine: BaselineLocalNetworkEngine

    override fun onCreate() {
        super.onCreate()
        appResolver = AppResolver(this)
        engine = BaselineLocalNetworkEngine()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopTunnel()
            else -> startTunnel(intent?.getStringExtra(EXTRA_PROFILE_ID) ?: ProfileCatalog.auto.id)
        }
        return START_STICKY
    }

    override fun onRevoke() {
        stopTunnel()
        mutableEngineState.value = EngineState.FAILED
        super.onRevoke()
    }

    override fun onDestroy() {
        engine.stop()
        if (mutableEngineState.value != EngineState.FAILED) {
            mutableEngineState.value = EngineState.STOPPED
        }
        super.onDestroy()
    }

    private fun startTunnel(profileId: String) {
        val profile = ProfileCatalog.profiles.firstOrNull { it.id == profileId } ?: ProfileCatalog.auto
        startForeground(NOTIFICATION_ID, notification("FOG Mobile обрабатывает выбранные приложения локально"))
        if (!engine.canForwardPackets) {
            mutableEngineState.value = EngineState.FAILED
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }
        val builder = Builder()
            .setSession("FOG Mobile")
            .setMtu(1500)
            .addAddress("10.82.0.2", 32)
            .addDnsServer("1.1.1.1")
            .addRoute("0.0.0.0", 0)
            .setBlocking(true)

        val allowlist = appResolver.vpnAllowlist()
        if (allowlist.isEmpty()) {
            mutableEngineState.value = EngineState.FAILED
            stopSelf()
            return
        }

        allowlist.forEach { packageName ->
            runCatching { builder.addAllowedApplication(packageName) }
        }

        val tun = builder.establish()
        if (tun == null) {
            mutableEngineState.value = EngineState.FAILED
            stopSelf()
            return
        }
        engine.start(tun, profile)
        mutableEngineState.value = EngineState.RUNNING
    }

    private fun stopTunnel() {
        engine.stop()
        mutableEngineState.value = EngineState.STOPPED
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "FOG Mobile", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun notification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FOG Mobile")
            .setContentText(text)
            .setSmallIcon(dev.fogmobile.R.drawable.ic_vpn_notification)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START = "dev.fogmobile.START"
        const val ACTION_STOP = "dev.fogmobile.STOP"
        const val EXTRA_PROFILE_ID = "profile_id"
        private const val CHANNEL_ID = "fog_mobile_vpn"
        private const val NOTIFICATION_ID = 82
        private val mutableEngineState = MutableStateFlow(EngineState.STOPPED)
        val engineState: StateFlow<EngineState> = mutableEngineState

        fun markStarting() {
            mutableEngineState.value = EngineState.STARTING
        }
    }
}
