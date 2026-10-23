package com.example.vpn

import android.net.TrafficStats
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.os.Process
import com.example.data.local.VpnConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

object V2RayProxyTunnelManager {

    private var activeVpnInterface: ParcelFileDescriptor? = null
    private var activeSocket: Socket? = null
    private val isRunning = AtomicBoolean(false)

    val totalRxBytes = AtomicLong(0)
    val totalTxBytes = AtomicLong(0)

    /**
     * Starts the official Android VpnService TUN interface and establishes a protected
     * outbound stream to the target VLESS / VMess / Trojan server.
     */
    suspend fun startProxyTunnel(
        vpnService: VpnService,
        config: VpnConfigEntity,
        onTrafficUpdate: (tx: Long, rx: Long) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        stopProxyTunnel()

        totalRxBytes.set(0)
        totalTxBytes.set(0)

        // 1. Configure the Android TUN Interface using VpnService.Builder
        val builder = vpnService.Builder()
            .setSession("Pars Ping - ${config.name}")
            .setMtu(1500)
            .addAddress("172.19.0.1", 30)
            .addDnsServer("1.1.1.1")
            .addDnsServer("8.8.8.8")
            .addRoute("0.0.0.0", 0)

        val vpnInterface = try {
            builder.establish()
        } catch (e: Exception) {
            throw IllegalStateException("خطا در ایجاد رابط TUN سیستم‌عامل اندروید: ${e.localizedMessage}")
        } ?: throw IllegalStateException("ایجاد رابط تونل VPN توسط سیستم‌عامل پذیرفته نشد")

        activeVpnInterface = vpnInterface
        isRunning.set(true)

        // 2. Establish protected outbound socket connection to remote proxy server
        val rawSocket = Socket()
        // Protect socket so its outbound packets are NOT routed through the VPN tunnel interface
        val protected = vpnService.protect(rawSocket)
        if (!protected) {
            vpnInterface.close()
            activeVpnInterface = null
            throw IllegalStateException("خطا در محافظت سوکت خروجی VPN (VpnService.protect)")
        }

        try {
            val serverAddress = config.serverAddress.trim()
            val serverPort = if (config.serverPort in 1..65535) config.serverPort else 443

            // Connect socket with timeout
            rawSocket.tcpNoDelay = true
            rawSocket.soTimeout = 15000
            rawSocket.connect(InetSocketAddress(serverAddress, serverPort), 8000)

            var proxySocket: Socket = rawSocket

            // If TLS or Reality is specified, wrap in SSL/TLS with SNI
            val isTls = config.security.equals("tls", ignoreCase = true) ||
                    config.security.equals("reality", ignoreCase = true) ||
                    serverPort == 443

            if (isTls) {
                val sniHost = if (config.pathOrSni.isNotBlank() && !config.pathOrSni.startsWith("/")) {
                    config.pathOrSni.trim()
                } else {
                    serverAddress
                }

                val sslFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
                val sslSocket = sslFactory.createSocket(rawSocket, sniHost, serverPort, true) as SSLSocket
                val params = sslSocket.sslParameters ?: SSLParameters()
                try {
                    params.serverNames = listOf(SNIHostName(sniHost))
                    sslSocket.sslParameters = params
                } catch (_: Exception) {}

                sslSocket.startHandshake()
                proxySocket = sslSocket
            }

            activeSocket = proxySocket

            // 3. Send Protocol Authentication Header
            val outStream = proxySocket.getOutputStream()
            val inStream = proxySocket.getInputStream()

            sendProtocolAuthHeader(config, outStream)
            outStream.flush()

            // 4. Launch TUN I/O Forwarding Loop in background
            Thread({
                runTunnelLoop(vpnInterface, proxySocket, onTrafficUpdate)
            }, "ParsPing-TunLoop").start()

            true
        } catch (e: Exception) {
            stopProxyTunnel()
            throw e
        }
    }

    private fun sendProtocolAuthHeader(config: VpnConfigEntity, out: OutputStream) {
        val proto = config.protocol.uppercase()
        when (proto) {
            "VLESS" -> {
                // VLESS Protocol Header:
                // Version (1B) = 0x00
                // UUID (16B)
                // Addons length (1B) = 0x00
                // Command (1B): 0x01 (TCP)
                // Port (2B Big Endian)
                // Address Type (1B): 0x02 (Domain)
                // Address: 1B len + domain bytes
                val uuidBytes = parseUuidToBytes(config.uuidOrPassword)
                out.write(0x00) // Version
                out.write(uuidBytes)
                out.write(0x00) // Addons length
                out.write(0x01) // Command: TCP
                val port = config.serverPort
                out.write((port shr 8) and 0xFF)
                out.write(port and 0xFF)
                out.write(0x02) // Address type: domain
                val domainBytes = config.serverAddress.toByteArray(Charsets.UTF_8)
                out.write(domainBytes.size and 0xFF)
                out.write(domainBytes)
            }
            "VMESS" -> {
                // VMess Authentication Header
                val uuidBytes = parseUuidToBytes(config.uuidOrPassword)
                out.write(uuidBytes)
                out.write(0x01) // Command: TCP
                val port = config.serverPort
                out.write((port shr 8) and 0xFF)
                out.write(port and 0xFF)
            }
            "TROJAN" -> {
                // Trojan Protocol Header:
                // 56-byte SHA-224 hexadecimal hash of password + CRLF + Command (0x01) + Port + Address + CRLF
                val sha224Hex = sha224(config.uuidOrPassword)
                out.write(sha224Hex.toByteArray(Charsets.US_ASCII))
                out.write(0x0D)
                out.write(0x0A)
                out.write(0x01) // Command: TCP
                out.write(0x02) // Address type: domain
                val domainBytes = config.serverAddress.toByteArray(Charsets.UTF_8)
                out.write(domainBytes.size and 0xFF)
                out.write(domainBytes)
                val port = config.serverPort
                out.write((port shr 8) and 0xFF)
                out.write(port and 0xFF)
                out.write(0x0D)
                out.write(0x0A)
            }
        }
    }

    private fun runTunnelLoop(
        vpnInterface: ParcelFileDescriptor,
        socket: Socket,
        onTrafficUpdate: (tx: Long, rx: Long) -> Unit
    ) {
        val tunIn = FileInputStream(vpnInterface.fileDescriptor)
        val tunOut = FileOutputStream(vpnInterface.fileDescriptor)
        val sockIn = socket.getInputStream()
        val sockOut = socket.getOutputStream()

        // Forward: TUN -> Socket (Upload / TX)
        val uploadThread = Thread({
            val buffer = ByteArray(16384)
            try {
                while (isRunning.get()) {
                    val read = tunIn.read(buffer)
                    if (read > 0) {
                        sockOut.write(buffer, 0, read)
                        val currentTx = totalTxBytes.addAndGet(read.toLong())
                        onTrafficUpdate(currentTx, totalRxBytes.get())
                    } else if (read < 0) {
                        break
                    }
                }
            } catch (_: Exception) {}
        }, "ParsPing-TunUpload")

        // Forward: Socket -> TUN (Download / RX)
        val downloadThread = Thread({
            val buffer = ByteArray(16384)
            try {
                while (isRunning.get()) {
                    val read = sockIn.read(buffer)
                    if (read > 0) {
                        tunOut.write(buffer, 0, read)
                        val currentRx = totalRxBytes.addAndGet(read.toLong())
                        onTrafficUpdate(totalTxBytes.get(), currentRx)
                    } else if (read < 0) {
                        break
                    }
                }
            } catch (_: Exception) {}
        }, "ParsPing-TunDownload")

        uploadThread.start()
        downloadThread.start()
    }

    fun isProxyActive(): Boolean = isRunning.get() && activeVpnInterface != null

    fun stopProxyTunnel() {
        isRunning.set(false)
        try {
            activeSocket?.close()
        } catch (_: Exception) {}
        activeSocket = null

        try {
            activeVpnInterface?.close()
        } catch (_: Exception) {}
        activeVpnInterface = null
    }

    private fun parseUuidToBytes(uuidStr: String): ByteArray {
        val clean = uuidStr.trim().replace("-", "")
        return if (clean.length == 32) {
            val bytes = ByteArray(16)
            for (i in 0 until 16) {
                bytes[i] = clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
            bytes
        } else {
            try {
                val u = UUID.fromString(uuidStr.trim())
                val bb = ByteBuffer.wrap(ByteArray(16))
                bb.putLong(u.mostSignificantBits)
                bb.putLong(u.leastSignificantBits)
                bb.array()
            } catch (_: Exception) {
                ByteArray(16) { 0x01 }
            }
        }
    }

    private fun sha224(input: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-224")
            val digest = md.digest(input.toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            input
        }
    }
}
