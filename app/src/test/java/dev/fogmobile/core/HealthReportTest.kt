package dev.fogmobile.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthReportTest {
    @Test
    fun allHealthyRequiresEveryRequiredProbe() {
        val report = HealthReport(
            instagram = ServiceHealth(ServiceId.INSTAGRAM, listOf(HealthProbe(ProbeKind.DNS, ProbeStatus.OK))),
            youtube = ServiceHealth(ServiceId.YOUTUBE, listOf(HealthProbe(ProbeKind.TLS, ProbeStatus.FAILED))),
            checkedAtMillis = 1L,
        )

        assertFalse(report.allHealthy)
    }

    @Test
    fun skippedProbeDoesNotFailService() {
        val service = ServiceHealth(
            ServiceId.YOUTUBE,
            listOf(
                HealthProbe(ProbeKind.TLS, ProbeStatus.OK),
                HealthProbe(ProbeKind.QUIC, ProbeStatus.SKIPPED),
            ),
        )

        assertTrue(service.isHealthy)
    }
}
