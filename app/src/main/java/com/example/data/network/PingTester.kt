package com.example.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

sealed class PingResult {
    data class Success(val latencyMs: Long) : PingResult()
    data class Timeout(val message: String = "تایم‌اوت (پاسخی دریافت نشد)") : PingResult()
    data class ConnectionRefused(val message: String = "ارتباط توسط پورت سرور رد شد") : PingResult()
    data class DnsError(val message: String = "خطای وضوح نام دامنه (DNS)") : PingResult()
    data class Error(val message: String) : PingResult()

    val latencyOrNull: Long?
        get() = if (this is Success) latencyMs else null
}

object PingTester {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3500, TimeUnit.MILLISECONDS)
        .readTimeout(3500, TimeUnit.MILLISECONDS)
        .callTimeout(4000, TimeUnit.MILLISECONDS)
        .followRedirects(false)
        .build()

    /**
     * Accurately tests the TCP round-trip latency to the target host and port.
     * Accurately distinguishes between Timeout, Connection Refused, and DNS failure.
     * Never generates fake or estimated numbers.
     */
    suspend fun testTcpEndpoint(host: String, port: Int, timeoutMs: Int = 2500): PingResult {
        return withContext(Dispatchers.IO) {
            val startTime = System.nanoTime()
            var socket: Socket? = null
            try {
                socket = Socket()
                val targetPort = if (port in 1..65535) port else 443
                val socketAddress = InetSocketAddress(host.trim(), targetPort)
                socket.connect(socketAddress, timeoutMs)
                val elapsedMs = (System.nanoTime() - startTime) / 1_000_000
                PingResult.Success(elapsedMs)
            } catch (e: UnknownHostException) {
                PingResult.DnsError("خطای DNS: آدرس $host قابل شناسایی نیست")
            } catch (e: SocketTimeoutException) {
                PingResult.Timeout("تایم‌اوت: سرور پس از ${timeoutMs}ms پاسخ نداد")
            } catch (e: ConnectException) {
                PingResult.ConnectionRefused("پورت $port روی سرور $host باز نیست یا مسدود شده است")
            } catch (e: Exception) {
                PingResult.Error(e.localizedMessage ?: "عدم دسترسی به سرور")
            } finally {
                try {
                    socket?.close()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Backward-compatible simple latency getter (ms or null).
     */
    suspend fun measureTcpLatency(host: String, port: Int, timeoutMs: Int = 2500): Long? {
        val res = testTcpEndpoint(host, port, timeoutMs)
        return res.latencyOrNull
    }

    /**
     * Measures real HTTPS tunnel latency through the active VPN network.
     * Uses Cloudflare trace / Google 204 to test true end-to-end internet connectivity.
     */
    suspend fun measureTunnelHttpsLatency(
        probeUrl: String = "https://www.google.com/generate_204"
    ): Long? {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(probeUrl)
                .header("User-Agent", "ParsPing/1.0")
                .get()
                .build()

            val start = System.nanoTime()
            try {
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful || response.code == 204) {
                        (System.nanoTime() - start) / 1_000_000
                    } else {
                        null
                    }
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
