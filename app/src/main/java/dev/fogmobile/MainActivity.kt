package dev.fogmobile

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.fogmobile.ui.FogMobileApp
import dev.fogmobile.ui.FogMobileViewModel
import dev.fogmobile.ui.theme.FogTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FogTheme {
                val viewModel: FogMobileViewModel = hiltViewModel()
                val vpnPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.onVpnPermissionResult(it.resultCode == RESULT_OK)
                }

                LaunchedEffect(Unit) {
                    viewModel.permissionRequests.collect {
                        val intent: Intent? = VpnService.prepare(this@MainActivity)
                        if (intent == null) {
                            viewModel.onVpnPermissionResult(true)
                        } else {
                            vpnPermissionLauncher.launch(intent)
                        }
                    }
                }

                FogMobileApp(viewModel = viewModel)
            }
        }
    }
}
