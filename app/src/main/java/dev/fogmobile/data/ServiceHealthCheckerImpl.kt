package dev.fogmobile.data

import dev.fogmobile.core.HealthProbe
import dev.fogmobile.core.HealthReport
import dev.fogmobile.core.ProbeKind
import dev.fogmobile.core.ProbeStatus
import dev.fogmobile.core.ServiceHealth
import dev.fogmobile.core.ServiceId
import dev.fogmobile.domain.ServiceHealthChecker
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import javax.inject.Inject
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class ServiceHealthCheckerImpl @Inject constructor() : ServiceHealthChecker {
    override suspend fun check(): HealthReport = withContext(Dispatchers.IO) {
        HealthReport(
            instagram = checkInstagram(),
            youtube = checkYouTube(),
            checkedAtMillis = System.currentTimeMillis(),
        )
    }

    private suspend fun checkInstagram(): ServiceHealth {
        return ServiceHealth(
            ServiceId.INSTAGRAM,
            listOf(
                dns("instagram.com", ProbeKind.DNS),
                tls("www.instagram.com", ProbeKind.TLS),
                tls("scontent.cdninstagram.com", ProbeKind.MEDIA_CDN),
            ),
        )
    }

    private suspend fun checkYouTube(): ServiceHealth {
        return ServiceHealth(
            ServiceId.YOUTUBE,
            listOf(
                dns("youtube.com", ProbeKind.DNS),
                tls("www.youtube.com", ProbeKind.TLS),
                tls("i.ytimg.com", ProbeKind.VIDEO_CDN),
                udp443("www.youtube.com", ProbeKind.QUIC),
            ),
        )
    }

    private suspend fun dns(host: String, kind: ProbeKind): HealthProbe {
        val result = withTimeoutOrNull(3_000) {
            runCatching { InetAddress.getAllByName(host).isNotEmpty() }
        }
        return when {
            result?.getOrNull() == true -> HealthProbe(kind, ProbeStatus.OK)
            else -> HealthProbe(kind, ProbeStatus.FAILED, result?.exceptionOrNull()?.message ?: "DNS lookup timed out for $host")
        }
    }

    private suspend fun tls(host: String, kind: ProbeKind): HealthProbe {
        val result = withTimeoutOrNull(4_000) {
            runCatching {
                (SSLSocketFactory.getDefault().createSocket() as SSLSocket).use { socket ->
                    socket.connect(InetSocketAddress(host, 443), 3_500)
                    socket.soTimeout = 3_500
                    socket.sslParameters = socket.sslParameters.apply {
                        endpointIdentificationAlgorithm = "HTTPS"
                    }
                    socket.startHandshake()
                    true
                }
            }
        }
        return when {
            result?.getOrNull() == true -> HealthProbe(kind, ProbeStatus.OK)
            else -> HealthProbe(kind, ProbeStatus.FAILED, result?.exceptionOrNull()?.message ?: "TLS socket timed out for $host")
        }
    }

    private suspend fun udp443(host: String, kind: ProbeKind): HealthProbe {
        val result = withTimeoutOrNull(2_500) {
            runCatching {
                DatagramSocket().use { socket ->
                    socket.soTimeout = 1_500
                    socket.connect(InetSocketAddress(host, 443))
                    socket.send(DatagramPacket(ByteArray(1), 1))
                    true
                }
            }
        }
        return when {
            result?.getOrNull() == true -> HealthProbe(
                kind,
                ProbeStatus.SKIPPED,
                "UDP route is available; a full QUIC handshake is not performed.",
            )
            else -> HealthProbe(kind, ProbeStatus.FAILED, result?.exceptionOrNull()?.message ?: "UDP/443 probe timed out for $host")
        }
    }
}
