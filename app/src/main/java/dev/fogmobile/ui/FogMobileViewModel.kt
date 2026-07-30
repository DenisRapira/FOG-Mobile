package dev.fogmobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.fogmobile.domain.ConnectionManager
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FogMobileViewModel @Inject constructor(
    private val connectionManager: ConnectionManager,
) : ViewModel() {
    val state = connectionManager.state
    private val permissionChannel = Channel<Unit>(Channel.BUFFERED)
    val permissionRequests = permissionChannel.receiveAsFlow()

    fun checkConnection() {
        viewModelScope.launch { connectionManager.checkAndConfigure() }
    }

    fun stopTunnel() {
        viewModelScope.launch { connectionManager.stop() }
    }

    fun requestVpnPermission() {
        permissionChannel.trySend(Unit)
    }

    fun onVpnPermissionResult(granted: Boolean) {
        if (granted) {
            viewModelScope.launch { connectionManager.onVpnPermissionGranted() }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { connectionManager.markOnboardingComplete() }
    }

    fun setAutoCheckAfterNetworkChange(value: Boolean) {
        viewModelScope.launch { connectionManager.setAutoCheckAfterNetworkChange(value) }
    }

    fun setStartProtectionOnLaunch(value: Boolean) {
        viewModelScope.launch { connectionManager.setStartProtectionOnLaunch(value) }
    }
}
