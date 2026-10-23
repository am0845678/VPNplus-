package com.example.vpn

import android.content.Context
import com.example.data.local.VpnConfigEntity
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Statistics
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.InetAddresses
import com.wireguard.config.InetEndpoint
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object WireGuardTunnelManager {

    private var backend: Backend? = null
    private var currentTunnel: Tunnel? = null
    private var activeConfig: Config? = null

    private fun getBackend(context: Context): Backend {
        return backend ?: synchronized(this) {
            backend ?: GoBackend(context.applicationContext).also { backend = it }
        }
    }

    /**
     * Parses standard WireGuard configuration text (including [Interface] and [Peer])
     * into the official WireGuard Config object.
     */
    fun parseWireGuardConfig(configText: String): Config {
        val trimmed = configText.trim()
        val inputStream = ByteArrayInputStream(trimmed.toByteArray(StandardCharsets.UTF_8))
        return Config.parse(inputStream)
    }

    /**
     * Validates whether a configuration text is valid WireGuard format.
     */
    fun validateWireGuardConfig(configText: String): Result<Config> {
        return try {
            val config = parseWireGuardConfig(configText)
            if (config.peers.isEmpty()) {
                Result.failure(IllegalArgumentException("کانفیگ فاقد بخش [Peer] است"))
            } else {
                Result.success(config)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Converts a VpnConfigEntity into a standard WireGuard Config object.
     * Uses rawUri if it contains a full WireGuard config, or builds a valid Config.
     */
    fun createConfigFromEntity(entity: VpnConfigEntity): Config {
        val raw = entity.rawUri.trim()

        // 1. Direct standard INI configuration text
        if (raw.contains("[Interface]", ignoreCase = true) && raw.contains("[Peer]", ignoreCase = true)) {
            return parseWireGuardConfig(raw)
        }

        // 2. wireguard:// or wg:// URI format
        if (raw.startsWith("wireguard://", ignoreCase = true) || raw.startsWith("wg://", ignoreCase = true)) {
            return parseWireGuardUri(raw, entity)
        }

        // 3. Structured entity fields fallback
        val privateKeyStr = if (isValidBase64Key(entity.uuidOrPassword)) {
            entity.uuidOrPassword
        } else {
            // Generate valid Curve25519 KeyPair for client if not provided
            KeyPair().privateKey.toBase64()
        }

        val publicKeyStr = if (isValidBase64Key(entity.pathOrSni)) {
            entity.pathOrSni
        } else {
            // Fallback public key for peer
            KeyPair().publicKey.toBase64()
        }

        val targetPort = if (entity.serverPort in 1..65535) entity.serverPort else 51820
        val targetHost = entity.serverAddress.ifBlank { "engage.cloudflareclient.com" }

        val ini = buildString {
            appendLine("[Interface]")
            appendLine("PrivateKey = $privateKeyStr")
            appendLine("Address = 10.14.0.2/32, fd00::2/128")
            appendLine("DNS = 1.1.1.1, 8.8.8.8")
            appendLine("MTU = 1420")
            appendLine()
            appendLine("[Peer]")
            appendLine("PublicKey = $publicKeyStr")
            appendLine("Endpoint = $targetHost:$targetPort")
            appendLine("AllowedIPs = 0.0.0.0/0, ::/0")
            appendLine("PersistentKeepalive = 25")
        }

        return parseWireGuardConfig(ini)
    }

    private fun parseWireGuardUri(uriString: String, fallbackEntity: VpnConfigEntity): Config {
        try {
            val uri = URI(uriString)
            val privateKey = uri.userInfo ?: fallbackEntity.uuidOrPassword
            val host = uri.host ?: fallbackEntity.serverAddress
            val port = if (uri.port > 0) uri.port else fallbackEntity.serverPort

            val queryMap = mutableMapOf<String, String>()
            uri.rawQuery?.split("&")?.forEach { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) {
                    queryMap[parts[0].lowercase()] = URLDecoder.decode(parts[1], "UTF-8")
                }
            }

            val pubKey = queryMap["publickey"] ?: queryMap["pubkey"] ?: fallbackEntity.pathOrSni
            val address = queryMap["address"] ?: queryMap["ip"] ?: "10.14.0.2/32"
            val dns = queryMap["dns"] ?: "1.1.1.1, 8.8.8.8"
            val mtu = queryMap["mtu"]?.toIntOrNull() ?: 1420
            val allowedIps = queryMap["allowedips"] ?: "0.0.0.0/0, ::/0"
            val keepalive = queryMap["keepalive"]?.toIntOrNull() ?: 25

            val ini = buildString {
                appendLine("[Interface]")
                appendLine("PrivateKey = $privateKey")
                appendLine("Address = $address")
                appendLine("DNS = $dns")
                appendLine("MTU = $mtu")
                appendLine()
                appendLine("[Peer]")
                appendLine("PublicKey = $pubKey")
                appendLine("Endpoint = $host:$port")
                appendLine("AllowedIPs = $allowedIps")
                appendLine("PersistentKeepalive = $keepalive")
            }
            return parseWireGuardConfig(ini)
        } catch (_: Exception) {
            return createConfigFromEntity(fallbackEntity.copy(rawUri = ""))
        }
    }

    private fun isValidBase64Key(keyStr: String): Boolean {
        if (keyStr.isBlank()) return false
        return try {
            Key.fromBase64(keyStr.trim())
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Starts a real WireGuard tunnel using the official GoBackend.
     */
    suspend fun startTunnel(
        context: Context,
        config: Config,
        tunnelName: String = "ParsPingWG",
        onStateChanged: (Tunnel.State) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val wgBackend = getBackend(context)

            // If a previous tunnel is still running, shut it down cleanly first
            currentTunnel?.let { oldTunnel ->
                try {
                    wgBackend.setState(oldTunnel, Tunnel.State.DOWN, null)
                } catch (_: Exception) {}
            }

            val tunnel = object : Tunnel {
                override fun getName(): String = tunnelName
                override fun onStateChange(newState: Tunnel.State) {
                    onStateChanged(newState)
                }
            }

            currentTunnel = tunnel
            activeConfig = config

            val state = wgBackend.setState(tunnel, Tunnel.State.UP, config)
            state == Tunnel.State.UP
        } catch (e: Exception) {
            currentTunnel = null
            activeConfig = null
            throw e
        }
    }

    /**
     * Stops the running WireGuard tunnel cleanly.
     */
    suspend fun stopTunnel(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val tunnel = currentTunnel ?: return@withContext true
            val wgBackend = getBackend(context)
            val state = wgBackend.setState(tunnel, Tunnel.State.DOWN, null)
            currentTunnel = null
            activeConfig = null
            state == Tunnel.State.DOWN
        } catch (e: Exception) {
            currentTunnel = null
            activeConfig = null
            throw e
        }
    }

    /**
     * Returns true if WireGuard GoBackend is currently active.
     */
    fun isTunnelActive(context: Context): Boolean {
        return try {
            val tunnel = currentTunnel ?: return false
            getBackend(context).getState(tunnel) == Tunnel.State.UP
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Reads real traffic statistics from GoBackend.
     */
    fun getStatistics(context: Context): Statistics? {
        val tunnel = currentTunnel ?: return null
        return try {
            getBackend(context).getStatistics(tunnel)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Generates a sample, fully valid WireGuard configuration with authentic Curve25519 keypairs.
     */
    fun createSampleWireGuardEntity(
        name: String,
        host: String,
        port: Int = 2408,
        mtu: Int = 1420,
        isSelected: Boolean = false
    ): VpnConfigEntity {
        val clientKey = KeyPair()
        val peerKey = KeyPair()

        val confText = buildString {
            appendLine("[Interface]")
            appendLine("PrivateKey = ${clientKey.privateKey.toBase64()}")
            appendLine("Address = 10.14.0.2/32, fd00::2/128")
            appendLine("DNS = 1.1.1.1, 8.8.8.8")
            appendLine("MTU = $mtu")
            appendLine()
            appendLine("[Peer]")
            appendLine("PublicKey = ${peerKey.publicKey.toBase64()}")
            appendLine("Endpoint = $host:$port")
            appendLine("AllowedIPs = 0.0.0.0/0, ::/0")
            appendLine("PersistentKeepalive = 25")
        }

        return VpnConfigEntity(
            name = name,
            protocol = "WireGuard",
            serverAddress = host,
            serverPort = port,
            rawUri = confText,
            uuidOrPassword = clientKey.privateKey.toBase64(),
            pathOrSni = peerKey.publicKey.toBase64(),
            isSupported = true,
            isSelected = isSelected
        )
    }
}
