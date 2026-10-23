package com.example.data.local

import android.util.Base64
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Entity(tableName = "vpn_configs")
data class VpnConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val protocol: String,
    val serverAddress: String,
    val serverPort: Int,
    val rawUri: String,
    val uuidOrPassword: String = "",
    val security: String = "none",
    val networkType: String = "tcp",
    val pathOrSni: String = "",
    val alterId: Int = 0,
    val flow: String = "",
    val alpn: String = "",
    val fingerprint: String = "",
    val realityPublicKey: String = "",
    val realityShortId: String = "",
    val realitySpiderX: String = "",
    val isSupported: Boolean = true,
    val isSelected: Boolean = false,
    val latencyMs: Long? = null,
    val lastChecked: Long = System.currentTimeMillis()
) {
    /**
     * Generates a valid sing-box compatible outbound configuration JSON object.
     */
    fun toSingBoxOutboundJson(): JSONObject {
        val outbound = JSONObject()
        val proto = protocol.lowercase()

        when (proto) {
            "vless" -> {
                outbound.put("type", "vless")
                outbound.put("tag", name)
                outbound.put("server", serverAddress)
                outbound.put("server_port", serverPort)
                outbound.put("uuid", uuidOrPassword)
                if (flow.isNotBlank()) outbound.put("flow", flow)

                if (security.equals("tls", ignoreCase = true) || security.equals("reality", ignoreCase = true)) {
                    val tlsObj = JSONObject()
                    tlsObj.put("enabled", true)
                    if (pathOrSni.isNotBlank()) tlsObj.put("server_name", pathOrSni)
                    if (alpn.isNotBlank()) {
                        val alpnArr = JSONArray()
                        alpn.split(",").map { it.trim() }.forEach { alpnArr.put(it) }
                        tlsObj.put("alpn", alpnArr)
                    }
                    if (realityPublicKey.isNotBlank()) {
                        val realityObj = JSONObject()
                        realityObj.put("enabled", true)
                        realityObj.put("public_key", realityPublicKey)
                        if (realityShortId.isNotBlank()) realityObj.put("short_id", realityShortId)
                        tlsObj.put("reality", realityObj)
                    }
                    outbound.put("tls", tlsObj)
                }

                if (networkType.equals("ws", ignoreCase = true)) {
                    val transport = JSONObject()
                    transport.put("type", "ws")
                    transport.put("path", if (pathOrSni.startsWith("/")) pathOrSni else "/")
                    val headers = JSONObject()
                    if (pathOrSni.isNotBlank() && !pathOrSni.startsWith("/")) {
                        headers.put("Host", pathOrSni)
                    }
                    transport.put("headers", headers)
                    outbound.put("transport", transport)
                }
            }
            "vmess" -> {
                outbound.put("type", "vmess")
                outbound.put("tag", name)
                outbound.put("server", serverAddress)
                outbound.put("server_port", serverPort)
                outbound.put("uuid", uuidOrPassword)
                outbound.put("alter_id", alterId)
                outbound.put("security", "auto")

                if (security.equals("tls", ignoreCase = true)) {
                    val tlsObj = JSONObject()
                    tlsObj.put("enabled", true)
                    if (pathOrSni.isNotBlank()) tlsObj.put("server_name", pathOrSni)
                    outbound.put("tls", tlsObj)
                }

                if (networkType.equals("ws", ignoreCase = true)) {
                    val transport = JSONObject()
                    transport.put("type", "ws")
                    transport.put("path", "/")
                    outbound.put("transport", transport)
                }
            }
            "trojan" -> {
                outbound.put("type", "trojan")
                outbound.put("tag", name)
                outbound.put("server", serverAddress)
                outbound.put("server_port", serverPort)
                outbound.put("password", uuidOrPassword)

                val tlsObj = JSONObject()
                tlsObj.put("enabled", true)
                if (pathOrSni.isNotBlank()) tlsObj.put("server_name", pathOrSni)
                outbound.put("tls", tlsObj)
            }
            "wireguard" -> {
                outbound.put("type", "wireguard")
                outbound.put("tag", name)
                outbound.put("server", serverAddress)
                outbound.put("server_port", serverPort)
                outbound.put("system_interface", true)
            }
            else -> {
                outbound.put("type", "direct")
                outbound.put("tag", name)
            }
        }
        return outbound
    }

    /**
     * Reconstructs an exportable, shareable configuration URI.
     */
    fun toShareableUri(): String {
        if (rawUri.isNotBlank()) return rawUri

        return when (protocol.uppercase()) {
            "VLESS" -> {
                val remark = URLEncoder.encode(name, StandardCharsets.UTF_8.name())
                val queryParams = mutableListOf<String>()
                if (security.isNotBlank()) queryParams.add("security=$security")
                if (networkType.isNotBlank()) queryParams.add("type=$networkType")
                if (pathOrSni.isNotBlank()) queryParams.add("sni=$pathOrSni")
                if (flow.isNotBlank()) queryParams.add("flow=$flow")
                if (alpn.isNotBlank()) queryParams.add("alpn=$alpn")
                if (fingerprint.isNotBlank()) queryParams.add("fp=$fingerprint")
                if (realityPublicKey.isNotBlank()) queryParams.add("pbk=$realityPublicKey")
                if (realityShortId.isNotBlank()) queryParams.add("sid=$realityShortId")
                val queryStr = if (queryParams.isNotEmpty()) "?${queryParams.joinToString("&")}" else ""
                "vless://$uuidOrPassword@$serverAddress:$serverPort$queryStr#$remark"
            }
            "VMESS" -> {
                val json = JSONObject().apply {
                    put("v", "2")
                    put("ps", name)
                    put("add", serverAddress)
                    put("port", serverPort)
                    put("id", uuidOrPassword)
                    put("aid", alterId)
                    put("net", networkType)
                    put("type", "none")
                    put("host", pathOrSni)
                    put("path", "")
                    put("tls", security)
                    put("sni", pathOrSni)
                }
                val base64 = Base64.encodeToString(json.toString().toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
                "vmess://$base64"
            }
            "TROJAN" -> {
                val remark = URLEncoder.encode(name, StandardCharsets.UTF_8.name())
                val query = if (pathOrSni.isNotBlank()) "?sni=$pathOrSni" else ""
                "trojan://$uuidOrPassword@$serverAddress:$serverPort$query#$remark"
            }
            else -> rawUri
        }
    }
}
