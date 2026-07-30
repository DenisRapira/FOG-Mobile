package dev.fogmobile.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.fogmobile.core.NetworkKind
import dev.fogmobile.core.NetworkSnapshot
import dev.fogmobile.domain.ConnectivityObserver
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AndroidConnectivityObserver @Inject constructor(
    @ApplicationContext context: Context,
) : ConnectivityObserver {
    private val connectivityManager = context.getSystemService<ConnectivityManager>()
    private val mutableNetwork = MutableStateFlow(readCurrentNetwork())
    override val network: StateFlow<NetworkSnapshot> = mutableNetwork

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            mutableNetwork.value = readCurrentNetwork()
        }

        override fun onLost(network: Network) {
            mutableNetwork.value = readCurrentNetwork()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            mutableNetwork.value = readCurrentNetwork()
        }
    }

    init {
        connectivityManager?.registerDefaultNetworkCallback(callback)
    }

    private fun readCurrentNetwork(): NetworkSnapshot {
        val manager = connectivityManager ?: return NetworkSnapshot()
        val network = manager.activeNetwork ?: return NetworkSnapshot()
        val capabilities = manager.getNetworkCapabilities(network) ?: return NetworkSnapshot()
        val kind = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkKind.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkKind.MOBILE
            else -> NetworkKind.OTHER
        }
        return NetworkSnapshot(
            kind = kind,
            metered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED),
            connected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
        )
    }
}
