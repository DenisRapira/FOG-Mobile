package dev.fogmobile.data

import dev.fogmobile.core.HealthReport
import dev.fogmobile.core.NetworkProfile
import dev.fogmobile.core.NetworkSnapshot
import dev.fogmobile.core.ProfileCatalog
import dev.fogmobile.domain.ProfileDecision
import dev.fogmobile.domain.ProfileManager
import javax.inject.Inject

class ProfileManagerImpl @Inject constructor() : ProfileManager {
    override suspend fun chooseProfile(
        current: NetworkProfile,
        network: NetworkSnapshot,
        check: suspend () -> HealthReport,
    ): ProfileDecision {
        val currentReport = check()
        if (currentReport.allHealthy) {
            return ProfileDecision(current, currentReport, changed = false)
        }

        for (profile in ProfileCatalog.profiles.filterNot { it.id == current.id }.sortedBy { it.priority }) {
            val report = check()
            if (report.allHealthy) {
                return ProfileDecision(profile, report, changed = true)
            }
        }

        return ProfileDecision(current, currentReport, changed = false)
    }
}
