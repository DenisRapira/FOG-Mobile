package dev.fogmobile.vpn

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class ResolvedApp(val label: String, val packageName: String, val installed: Boolean)

class AppResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val candidates = mapOf(
        "Instagram" to listOf("com.instagram.android"),
        "YouTube" to listOf("com.google.android.youtube"),
    )

    fun resolveOfficialApps(): List<ResolvedApp> {
        val packageManager = context.packageManager
        return candidates.mapNotNull { (label, packages) ->
            val packageName = packages.firstOrNull { it.isInstalled(packageManager) }
            ResolvedApp(label, packageName ?: packages.first(), packageName != null)
        }
    }

    fun vpnAllowlist(): List<String> = resolveOfficialApps().filter { it.installed }.map { it.packageName }

    private fun String.isInstalled(packageManager: PackageManager): Boolean {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(this, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(this, 0)
            }
        }.isSuccess
    }
}
