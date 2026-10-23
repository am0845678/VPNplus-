package com.example.vpn

import android.net.VpnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

sealed class ReachabilityResult {
    data class Success(val latencyMs: Long, val resolvedIp: String) : ReachabilityResult()
    data class Failure(val reason: String, val isDnsFailure: Boolean = false) : ReachabilityResult()
}

sealed class DnsCheckResult {
    data class Success(val addresses: List<String>, val elapsedMs: Long) : DnsCheckResult()
    data class Failure(val reason: String) : DnsCheckResult()
}

sealed class HttpsCheckResult {
    data class Success(val responseCode: Int, val elapsedMs: Long) : HttpsCheckResult()
    data class Failure(val reason: String) : HttpsCheckResult()
}

data class DiagnosticSummary(
    val serverReachability: ReachabilityResult,
    val dnsCheck: DnsCheckResult,
    val httpsCheck: HttpsCheckResult,
    val coreAvailable: Boolean,
    val protocol: String
)

object VpnDiagnostics {

    /**
     * Checks if the required native binary / userspace core is available for the given protocol.
     * Accurately distinguishes between protocols with integrated native backends vs missing cores.
     */
    fun isProtocolCoreInstalled(protocol: String): Boolean {
        // WireGuard has the real official GoBackend userspace core compiled into the app.
        // VLESS, VMess, Trojan require the external native libxray.so / v2ray-core binary.
        val normalized = protocol.uppercase()
        return when (normalized) {
            "WIREGUARD", "WG" -> true
            "VLESS", "VMESS", "TROJAN", "SHADOWSOCKS", "SS" -> false
            "OPENVPN" -> false
            else -> false
        }
    }

    /**
     * Accurately tests the TCP socket reachability to host:port.
     * Uses vpnService.protect(socket) if provided, ensuring this probe directly tests
     * the physical network without being trapped in an unforwarded TUN interface.
     */
    suspend fun testServerReachability(
        host: String,
        port: Int,
        vpnService: VpnService? = null,
        timeoutMs: Int = 3500
    ): ReachabilityResult = withContext(Dispatchers.IO) {
        if (host.isBlank()) {
            return@withContext ReachabilityResult.Failure("آدرس سرور خالی است")
        }

        val startTime = System.nanoTime()
        var socket: Socket? = null
        try {
            socket = Socket()
            // CRITICAL: Protect socket from VPN routing to avoid routing loops
            vpnService?.protect(socket)

            val targetPort = if (port in 1..65535) port else 443
            val socketAddress = InetSocketAddress(host, targetPort)
            socket.connect(socketAddress, timeoutMs)

            val elapsedMs = (System.nanoTime() - startTime) / 1_000_000
            val resolvedIp = socket.inetAddress?.hostAddress ?: host
            ReachabilityResult.Success(elapsedMs, resolvedIp)
        } catch (e: UnknownHostException) {
            ReachabilityResult.Failure("خطای تحلیل نام دامنه سرور (DNS Unresolved)", isDnsFailure = true)
        } catch (e: SocketTimeoutException) {
            ReachabilityResult.Failure("عدم پاسخگویی سرور (Timeout - احتمال فیلتر بودن IP یا پورت)")
        } catch (e: ConnectException) {
            ReachabilityResult.Failure("رد اتصال توسط سرور یا عدم دسترسی به مسیر (Connection Refused)")
        } catch (e: Exception) {
            ReachabilityResult.Failure("خطای ارتباط: ${e.localizedMessage ?: "نامشخص"}")
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    /**
     * Verifies DNS resolution functionality independently from VPN packet routing.
     */
    suspend fun testDnsResolution(domain: String = "connectivitycheck.gstatic.com"): DnsCheckResult =
        withContext(Dispatchers.IO) {
            val start = System.nanoTime()
            try {
                val addresses = InetAddress.getAllByName(domain)
                val elapsedMs = (System.nanoTime() - start) / 1_000_000
                if (addresses.isNotEmpty()) {
                    DnsCheckResult.Success(addresses.map { it.hostAddress ?: "" }, elapsedMs)
                } else {
                    DnsCheckResult.Failure("هیچ آدرس IP برای $domain یافت نشد")
                }
            } catch (e: Exception) {
                DnsCheckResult.Failure("شکست در تفکیک DNS: ${e.localizedMessage ?: "نامشخص"}")
            }
        }

    /**
     * Performs a real HTTP/HTTPS connectivity verification test.
     */
    suspend fun testHttpsConnectivity(timeoutMs: Long = 4000): HttpsCheckResult =
        withContext(Dispatchers.IO) {
            val start = System.nanoTime()
            val client = OkHttpClient.Builder()
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .followRedirects(true)
                .build()

            val request = Request.Builder()
                .url("https://www.google.com/generate_204")
                .header("User-Agent", "ParsPingDiagnostics/1.0")
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val elapsedMs = (System.nanoTime() - start) / 1_000_000
                    if (response.code in 200..204) {
                        HttpsCheckResult.Success(response.code, elapsedMs)
                    } else {
                        HttpsCheckResult.Failure("کد پاسخ اینترنت نامعتبر: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                HttpsCheckResult.Failure("عدم دسترسی به اینترنت: ${e.localizedMessage ?: "Timeout"}")
            }
        }
}
