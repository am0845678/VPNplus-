package com.example.data.network

import android.util.Base64
import com.example.data.local.VpnConfigEntity
import com.example.vpn.WireGuardTunnelManager
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object SubscriptionParser {

    /**
     * Parses raw subscription response body or pasted text (base64-encoded,
     * multi-line URIs, or standard WireGuard [Interface]...[Peer] INI config)
     * and produces a de-duplicated list of VpnConfigEntity.
     */
    fun parseSubscription(content: String): List<VpnConfigEntity> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return emptyList()

        // Case 1: Standard WireGuard .conf INI format
        if (trimmed.contains("[Interface]", ignoreCase = true) && trimmed.contains("[Peer]", ignoreCase = true)) {
            val wgConfig = parseWireGuardConf(trimmed)
            return if (wgConfig != null) listOf(wgConfig) else emptyList()
        }

        // Case 2: Base64 decoded or multi-line URIs
        val decodedLines = tryDecodeBase64Lines(trimmed)
        val configs = mutableListOf<VpnConfigEntity>()
        val seenKeys = mutableSetOf<String>()

        for (line in decodedLines) {
            val cleanLine = line.trim()
            if (cleanLine.isBlank() || cleanLine.startsWith("#")) continue

            val config = parseLine(cleanLine)
            if (config != null) {
                val dedupeKey = "${config.protocol}:${config.serverAddress}:${config.serverPort}:${config.uuidOrPassword}"
                if (seenKeys.add(dedupeKey)) {
                    configs.add(config)
                }
            }
        }
        return configs
    }

    /**
     * Parses a standard WireGuard INI configuration text into a VpnConfigEntity.
     */
    fun parseWireGuardConf(confText: String, defaultName: String? = null): VpnConfigEntity? {
        return try {
            val config = WireGuardTunnelManager.parseWireGuardConfig(confText)
            val peer = if (config.peers.isNotEmpty()) config.peers[0] else null
            val endpoint = if (peer != null && peer.endpoint.isPresent) peer.endpoint.get() else null

            val host = endpoint?.host ?: "wg.parsping.net"
            val port = endpoint?.port ?: 51820
            val peerPublicKey = peer?.publicKey?.toBase64() ?: ""
            val clientPrivateKey = config.`interface`.keyPair.privateKey.toBase64()
            val name = defaultName ?: "Pars Ping WireGuard ($host)"

            VpnConfigEntity(
                name = name,
                protocol = "WireGuard",
                serverAddress = host,
                serverPort = port,
                rawUri = confText.trim(),
                uuidOrPassword = clientPrivateKey,
                pathOrSni = peerPublicKey,
                isSupported = true
            )
        } catch (_: Exception) {
            parseWireGuardIniManual(confText, defaultName)
        }
    }

    private fun parseWireGuardIniManual(confText: String, defaultName: String? = null): VpnConfigEntity? {
        if (!confText.contains("[Interface]", ignoreCase = true)) return null

        var privateKey = ""
        var publicKey = ""
        var endpoint = ""

        confText.lines().forEach { line ->
            val clean = line.substringBefore("#").trim()
            val lower = clean.lowercase()
            when {
                lower.startsWith("privatekey") -> {
                    privateKey = clean.substringAfter("=").trim()
                }
                lower.startsWith("publickey") -> {
                    publicKey = clean.substringAfter("=").trim()
                }
                lower.startsWith("endpoint") -> {
                    endpoint = clean.substringAfter("=").trim()
                }
            }
        }

        val host = if (endpoint.contains(":")) endpoint.substringBefore(":") else endpoint.ifBlank { "wg.parsping.net" }
        val port = if (endpoint.contains(":")) endpoint.substringAfter(":").toIntOrNull() ?: 51820 else 51820
        val name = defaultName ?: "Pars Ping WireGuard ($host)"

        return VpnConfigEntity(
            name = name,
            protocol = "WireGuard",
            serverAddress = host,
            serverPort = port,
            rawUri = confText.trim(),
            uuidOrPassword = privateKey,
            pathOrSni = publicKey,
            isSupported = true
        )
    }

    private fun tryDecodeBase64Lines(input: String): List<String> {
        val decoded = tryDecodeBase64String(input)
        return if (decoded != null && (decoded.contains("://") || decoded.contains("[Interface]"))) {
            decoded.split("\r\n", "\n")
        } else {
            input.split("\r\n", "\n")
        }
    }

    private fun tryDecodeBase64String(input: String): String? {
        val sanitized = input.replace("\r", "").replace("\n", "").trim()
        val flags = listOf(Base64.DEFAULT, Base64.URL_SAFE, Base64.NO_WRAP)
        for (flag in flags) {
            try {
                val bytes = Base64.decode(sanitized, flag)
                val text = String(bytes, StandardCharsets.UTF_8)
                if (text.isNotBlank() && text.any { it.isLetterOrDigit() }) {
                    return text
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun parseLine(line: String): VpnConfigEntity? {
        return when {
            line.startsWith("vless://", ignoreCase = true) -> parseVless(line)
            line.startsWith("vmess://", ignoreCase = true) -> parseVmess(line)
            line.startsWith("trojan://", ignoreCase = true) -> parseTrojan(line)
            line.startsWith("ss://", ignoreCase = true) -> parseShadowsocks(line)
            line.startsWith("wireguard://", ignoreCase = true) || line.startsWith("wg://", ignoreCase = true) -> parseWireguard(line)
            line.startsWith("openvpn://", ignoreCase = true) || line.contains("BEGIN OpenVPN") -> parseOpenVpn(line)
            else -> null
        }
    }

    private fun parseVless(rawUri: String): VpnConfigEntity? {
        return try {
            val uri = URI(rawUri)
            val uuid = uri.userInfo ?: ""
            val host = uri.host ?: return null
            val port = if (uri.port > 0) uri.port else 443
            val queryParams = parseQueryParams(uri.rawQuery)
            val remark = decodeRemark(uri.rawFragment, "Pars Ping VLESS ($host)")

            val security = queryParams["security"] ?: "none"
            val type = queryParams["type"] ?: "tcp"
            val sni = queryParams["sni"] ?: queryParams["host"] ?: ""
            val flow = queryParams["flow"] ?: ""
            val alpn = queryParams["alpn"] ?: ""
            val fp = queryParams["fp"] ?: ""
            val pbk = queryParams["pbk"] ?: ""
            val sid = queryParams["sid"] ?: ""
            val spx = queryParams["spx"] ?: ""

            val path = queryParams["path"] ?: ""
            val hostHeader = queryParams["host"] ?: ""
            val pathOrSniCombined = when {
                sni.isNotBlank() -> sni
                hostHeader.isNotBlank() -> hostHeader
                path.isNotBlank() -> path
                else -> ""
            }

            VpnConfigEntity(
                name = remark,
                protocol = "VLESS",
                serverAddress = host,
                serverPort = port,
                rawUri = rawUri,
                uuidOrPassword = uuid,
                security = security,
                networkType = type,
                pathOrSni = pathOrSniCombined,
                flow = flow,
                alpn = alpn,
                fingerprint = fp,
                realityPublicKey = pbk,
                realityShortId = sid,
                realitySpiderX = spx,
                isSupported = true
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseVmess(rawUri: String): VpnConfigEntity? {
        return try {
            val base64Part = rawUri.substringAfter("vmess://").trim()
            val decodedJson = tryDecodeBase64String(base64Part) ?: return null
            val json = JSONObject(decodedJson)

            val name = json.optString("ps", "Pars Ping VMess")
            val address = json.optString("add", "")
            val port = json.optInt("port", 443)
            val id = json.optString("id", "")
            val aid = json.optInt("aid", 0)
            val net = json.optString("net", "tcp")
            val tls = json.optString("tls", "none")
            val host = json.optString("host", "")
            val sni = json.optString("sni", host)
            val path = json.optString("path", "")
            val alpn = json.optString("alpn", "")
            val fp = json.optString("fp", "")

            if (address.isBlank()) return null

            val pathOrSniCombined = if (sni.isNotBlank()) sni else if (path.isNotBlank()) path else host

            VpnConfigEntity(
                name = name,
                protocol = "VMess",
                serverAddress = address,
                serverPort = port,
                rawUri = rawUri,
                uuidOrPassword = id,
                security = tls,
                networkType = net,
                pathOrSni = pathOrSniCombined,
                alterId = aid,
                alpn = alpn,
                fingerprint = fp,
                isSupported = true
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseTrojan(rawUri: String): VpnConfigEntity? {
        return try {
            val uri = URI(rawUri)
            val password = uri.userInfo ?: ""
            val host = uri.host ?: return null
            val port = if (uri.port > 0) uri.port else 443
            val queryParams = parseQueryParams(uri.rawQuery)
            val remark = decodeRemark(uri.rawFragment, "Pars Ping Trojan ($host)")

            val security = queryParams["security"] ?: "tls"
            val type = queryParams["type"] ?: "tcp"
            val sni = queryParams["sni"] ?: queryParams["peer"] ?: ""
            val alpn = queryParams["alpn"] ?: ""
            val fp = queryParams["fp"] ?: ""

            VpnConfigEntity(
                name = remark,
                protocol = "Trojan",
                serverAddress = host,
                serverPort = port,
                rawUri = rawUri,
                uuidOrPassword = password,
                security = security,
                networkType = type,
                pathOrSni = sni,
                alpn = alpn,
                fingerprint = fp,
                isSupported = true
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseShadowsocks(rawUri: String): VpnConfigEntity? {
        return try {
            val withoutScheme = rawUri.substringAfter("ss://")
            val fragment = withoutScheme.substringAfter("#", "")
            val remark = if (fragment.isNotEmpty()) decodeRemark(fragment, "Pars Ping SS") else "Pars Ping SS"
            val mainPart = withoutScheme.substringBefore("#")

            if (mainPart.contains("@")) {
                val hostPort = mainPart.substringAfter("@")
                val host = hostPort.substringBefore(":")
                val port = hostPort.substringAfter(":").toIntOrNull() ?: 8388
                val userInfo = mainPart.substringBefore("@")
                val decodedUserInfo = tryDecodeBase64String(userInfo) ?: userInfo

                VpnConfigEntity(
                    name = remark,
                    protocol = "Shadowsocks",
                    serverAddress = host,
                    serverPort = port,
                    rawUri = rawUri,
                    uuidOrPassword = decodedUserInfo,
                    isSupported = false
                )
            } else {
                val decoded = tryDecodeBase64String(mainPart) ?: return null
                val uri = URI("ss://$decoded")
                val host = uri.host ?: return null
                val port = if (uri.port > 0) uri.port else 8388

                VpnConfigEntity(
                    name = remark,
                    protocol = "Shadowsocks",
                    serverAddress = host,
                    serverPort = port,
                    rawUri = rawUri,
                    uuidOrPassword = uri.userInfo ?: "",
                    isSupported = false
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseWireguard(rawUri: String): VpnConfigEntity {
        val uri = try { URI(rawUri) } catch (_: Exception) { null }
        val remark = uri?.rawFragment?.let { decodeRemark(it, "Pars Ping WireGuard") } ?: "Pars Ping WireGuard"
        val host = uri?.host ?: "engage.cloudflareclient.com"
        val port = if (uri != null && uri.port > 0) uri.port else 2408
        val privateKey = uri?.userInfo ?: ""

        val queryParams = parseQueryParams(uri?.rawQuery)
        val peerPublicKey = queryParams["publickey"] ?: queryParams["pubkey"] ?: ""

        return VpnConfigEntity(
            name = remark,
            protocol = "WireGuard",
            serverAddress = host,
            serverPort = port,
            rawUri = rawUri,
            uuidOrPassword = privateKey,
            pathOrSni = peerPublicKey,
            isSupported = true
        )
    }

    private fun parseOpenVpn(rawUri: String): VpnConfigEntity {
        return VpnConfigEntity(
            name = "Pars Ping OpenVPN",
            protocol = "OpenVPN",
            serverAddress = "ovpn.parsping.net",
            serverPort = 1194,
            rawUri = rawUri,
            isSupported = false
        )
    }

    private fun parseQueryParams(query: String?): Map<String, String> {
        if (query.isNullOrBlank()) return emptyMap()
        val result = mutableMapOf<String, String>()
        for (param in query.split("&")) {
            val pair = param.split("=", limit = 2)
            if (pair.size == 2) {
                result[pair[0].lowercase()] = pair[1]
            }
        }
        return result
    }

    private fun decodeRemark(rawFragment: String?, defaultVal: String): String {
        if (rawFragment.isNullOrBlank()) return defaultVal
        return try {
            URLDecoder.decode(rawFragment, StandardCharsets.UTF_8.name())
        } catch (_: Exception) {
            rawFragment
        }
    }

    /**
     * Returns pre-configured working sample WireGuard configurations for testing.
     */
    fun getSampleWireGuardConfigs(): List<VpnConfigEntity> {
        return listOf(
            WireGuardTunnelManager.createSampleWireGuardEntity(
                name = "Pars Ping WireGuard (آلمان - اصلی)",
                host = "engage.cloudflareclient.com",
                port = 2408,
                mtu = 1420,
                isSelected = true
            ),
            WireGuardTunnelManager.createSampleWireGuardEntity(
                name = "Pars Ping WireGuard (فنلاند - گیمینگ)",
                host = "engage.cloudflareclient.com",
                port = 2408,
                mtu = 1280,
                isSelected = false
            )
        )
    }
}
