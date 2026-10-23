package com.example.vpn

enum class VpnStatus(val persianTitle: String) {
    DISCONNECTED("قطع شده"),
    CONNECTING("در حال اتصال..."),
    CONNECTED("متصل"),
    FAILED("خطا در اتصال"),
    DISCONNECTING("در حال قطع...")
}

data class VpnConnectionInfo(
    val status: VpnStatus = VpnStatus.DISCONNECTED,
    val durationSeconds: Long = 0,
    val uploadBytes: Long = 0,
    val downloadBytes: Long = 0,
    val serverName: String? = null,
    val serverAddress: String? = null,
    val protocol: String? = null,
    val pingMs: Long? = null,
    val errorMessage: String? = null,
    val diagnosticMessage: String? = null,
    val serverReachabilityMs: Long? = null,
    val dnsStatus: String? = null,
    val httpsStatus: String? = null,
    val coreStatus: String? = null
) {
    val formattedDuration: String
        get() {
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val seconds = durationSeconds % 60
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

    val formattedUpload: String
        get() = formatBytes(uploadBytes)

    val formattedDownload: String
        get() = formatBytes(downloadBytes)

    companion object {
        fun formatBytes(bytes: Long): String {
            if (bytes < 1024) return "$bytes B"
            val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
            val pre = "KMGTPE"[exp - 1]
            return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
        }
    }
}
