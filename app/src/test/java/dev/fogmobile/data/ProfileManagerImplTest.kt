package dev.fogmobile.data

import dev.fogmobile.core.HealthProbe
import dev.fogmobile.core.HealthReport
import dev.fogmobile.core.NetworkSnapshot
import dev.fogmobile.core.ProbeKind
import dev.fogmobile.core.ProbeStatus
import dev.fogmobile.core.ProfileCatalog
import dev.fogmobile.core.ServiceHealth
import dev.fogmobile.core.ServiceId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ProfileManagerImplTest {
    private val manager = ProfileManagerImpl()

    @Test
    fun keepsCurrentProfileWhenHealthIsGood() = runTest {
        val decision = manager.chooseProfile(ProfileCatalog.auto, NetworkSnapshot(connected = true)) {
            healthyReport()
        }

        assertEquals(ProfileCatalog.auto, decision.profile)
        assertFalse(decision.changed)
    }

    @Test
    fun doesNotLoopForeverWhenAllProfilesFail() = runTest {
        var checks = 0
        val decision = manager.chooseProfile(ProfileCatalog.auto, NetworkSnapshot(connected = true)) {
            checks += 1
            failedReport()
        }

        assertEquals(ProfileCatalog.auto, decision.profile)
        assertEquals(ProfileCatalog.profiles.size, checks)
    }

    private fun healthyReport() = report(ProbeStatus.OK)
    private fun failedReport() = report(ProbeStatus.FAILED)

    private fun report(status: ProbeStatus) = HealthReport(
        instagram = ServiceHealth(ServiceId.INSTAGRAM, listOf(HealthProbe(ProbeKind.DNS, status))),
        youtube = ServiceHealth(ServiceId.YOUTUBE, listOf(HealthProbe(ProbeKind.DNS, status))),
        checkedAtMillis = 1L,
    )
}
