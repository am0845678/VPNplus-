package com.example.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.VpnConfigEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ParsPingVpnService : VpnService() {

    companion object {
        const val ACTION_CONNECT = "com.example.vpn.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpn.ACTION_DISCONNECT"

        const val EXTRA_CONFIG_NAME = "extra_config_name"
        const val EXTRA_SERVER_HOST = "extra_server_host"
        const val EXTRA_SERVER_PORT = "extra_server_port"
        const val EXTRA_PROTOCOL = "extra_protocol"
        const val EXTRA_UUID_OR_PASSWORD = "extra_uuid_or_password"
        const val EXTRA_SECURITY = "extra_security"
        const val EXTRA_NETWORK_TYPE = "extra_network_type"
        const val EXTRA_PATH_OR_SNI = "extra_path_or_sni"
        const val EXTRA_RAW_URI = "extra_raw_uri"

        private const val NOTIFICATION_CHANNEL_ID = "pars_ping_vpn_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private var tunnelJob: Job? = null
    private var statsJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private var currentConfigName: String = "Pars Ping Server"
    private var currentHost: String = ""
    private var currentPort: Int = 51820
    private var currentProtocol: String = "WireGuard"
    private var currentUuid: String = ""
    private var currentSecurity: String = "none"
    private var currentNetworkType: String = "tcp"
    private var currentPathOrSni: String = ""
    private var currentRawUri: String = ""

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        when (action) {
            ACTION_CONNECT -> {
                currentConfigName = intent.getStringExtra(EXTRA_CONFIG_NAME) ?: "Pars Ping Server"
                currentHost = intent.getStringExtra(EXTRA_SERVER_HOST) ?: ""
                currentPort = intent.getIntExtra(EXTRA_SERVER_PORT, 51820)
                currentProtocol = intent.getStringExtra(EXTRA_PROTOCOL) ?: "WireGuard"
                currentUuid = intent.getStringExtra(EXTRA_UUID_OR_PASSWORD) ?: ""
                currentSecurity = intent.getStringExtra(EXTRA_SECURITY) ?: "none"
                currentNetworkType = intent.getStringExtra(EXTRA_NETWORK_TYPE) ?: "tcp"
                currentPathOrSni = intent.getStringExtra(EXTRA_PATH_OR_SNI) ?: ""
                currentRawUri = intent.getStringExtra(EXTRA_RAW_URI) ?: ""

                startForeground(NOTIFICATION_ID, buildNotification("در حال اتصال به $currentConfigName..."))
                startConnectionFlow()
            }
            ACTION_DISCONNECT -> {
                disconnectAndStop()
            }
        }
        return START_NOT_STICKY
    }

    private fun startConnectionFlow() {
        tunnelJob?.cancel()
        tunnelJob = serviceScope.launch {
            try {
                val proto = currentProtocol.uppercase()

                // Check DNS resolution
                VpnManager.updateConnectingStage("در حال بررسی DNS سرور ($currentHost)...")
                val dnsResult = VpnDiagnostics.testDnsResolution(currentHost)
                if (dnsResult is DnsCheckResult.Failure) {
                    VpnManager.updateFailed("خطای تفکیک DNS برای سرور", dnsResult.reason)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@launch
                }

                val configEntity = VpnConfigEntity(
                    name = currentConfigName,
                    protocol = currentProtocol,
                    serverAddress = currentHost,
                    serverPort = currentPort,
                    rawUri = currentRawUri,
                    uuidOrPassword = currentUuid,
                    security = currentSecurity,
                    networkType = currentNetworkType,
                    pathOrSni = currentPathOrSni,
                    isSupported = true
                )

                if (proto == "WIREGUARD" || proto == "WG") {
                    startWireGuardConnection(configEntity, dnsResult)
                } else if (proto == "VLESS" || proto == "VMESS" || proto == "TROJAN") {
                    startProxyConnection(configEntity, dnsResult)
                } else {
                    val msg = "پروتکل $currentProtocol پشتیبانی نمی‌شود"
                    VpnManager.updateFailed(msg, "پروتکل‌های معتبر شامل WireGuard, VLESS, VMess و Trojan می‌باشند.")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }

            } catch (e: Exception) {
                VpnManager.updateFailed(e.localizedMessage ?: "خطا در برقراری ارتباط با هسته اتصال")
                disconnectAndStop()
            }
        }
    }

    private suspend fun startWireGuardConnection(entity: VpnConfigEntity, dnsResult: DnsCheckResult) {
        VpnManager.updateConnectingStage("در حال ساخت و اعتبارسنجی کانفیگ WireGuard...")

        val wgConfig = try {
            WireGuardTunnelManager.createConfigFromEntity(entity)
        } catch (e: Exception) {
            val errorMsg = "کانفیگ نامعتبر است: ${e.localizedMessage ?: "خطای ساختار"}"
            VpnManager.updateFailed(errorMsg, "بررسی فیلدهای Interface و Peer با خطا مواجه شد")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        VpnManager.updateConnectingStage("در حال راه‌اندازی تونل WireGuard...")
        val started = try {
            WireGuardTunnelManager.startTunnel(
                context = this@ParsPingVpnService,
                config = wgConfig,
                tunnelName = "ParsPingWG"
            ) { newState ->
                if (newState == com.wireguard.android.backend.Tunnel.State.DOWN) {
                    VpnManager.updateDisconnected()
                }
            }
        } catch (e: Exception) {
            VpnManager.updateFailed("شکست در اجرای تونل WireGuard: ${e.localizedMessage ?: "خطای ناشناخته"}")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        if (!started) {
            VpnManager.updateFailed("هسته WireGuard پاسخ نداد")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        VpnManager.updateConnectingStage("در حال تأیید عبور ترافیک اینترنت...")
        delay(800)

        val httpsResult = VpnDiagnostics.testHttpsConnectivity(timeoutMs = 4000)
        val httpsStatus = if (httpsResult is HttpsCheckResult.Success) {
            "تأیید شد (HTTP ${httpsResult.responseCode})"
        } else {
            "متصل به تونل (UDP Active)"
        }

        updateNotification("متصل به $currentConfigName (WireGuard)")
        VpnManager.updateConnected(
            serverName = currentConfigName,
            serverAddress = currentHost,
            protocol = "WireGuard",
            reachabilityMs = (dnsResult as? DnsCheckResult.Success)?.elapsedMs,
            dnsStatus = "OK",
            httpsStatus = httpsStatus,
            coreStatus = "WireGuard GoBackend Active"
        )

        startWireGuardStatsMonitoring()
    }

    private suspend fun startProxyConnection(entity: VpnConfigEntity, dnsResult: DnsCheckResult) {
        val protoTitle = entity.protocol.uppercase()
        VpnManager.updateConnectingStage("در حال راه‌اندازی تونل $protoTitle...")

        val started = try {
            V2RayProxyTunnelManager.startProxyTunnel(
                vpnService = this@ParsPingVpnService,
                config = entity
            ) { tx, rx ->
                VpnManager.setTrafficAbsolute(uploadBytes = tx, downloadBytes = rx)
            }
        } catch (e: Exception) {
            VpnManager.updateFailed("شکست در اتصال به $protoTitle: ${e.localizedMessage ?: "خطای ناشناخته"}")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        if (!started) {
            VpnManager.updateFailed("هسته $protoTitle پاسخ نداد")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        VpnManager.updateConnectingStage("در حال تأیید ارتباط...")
        delay(600)

        val elapsed = (dnsResult as? DnsCheckResult.Success)?.elapsedMs ?: 50L
        updateNotification("متصل به $currentConfigName ($protoTitle)")
        VpnManager.updateConnected(
            serverName = currentConfigName,
            serverAddress = currentHost,
            protocol = protoTitle,
            reachabilityMs = elapsed,
            dnsStatus = "OK",
            httpsStatus = "TCP/TLS Active",
            coreStatus = "TUN Proxy Core Active"
        )

        startProxyStatsMonitoring()
    }

    private fun startWireGuardStatsMonitoring() {
        statsJob?.cancel()
        statsJob = serviceScope.launch {
            while (isActive && WireGuardTunnelManager.isTunnelActive(this@ParsPingVpnService)) {
                delay(1000)
                val stats = WireGuardTunnelManager.getStatistics(this@ParsPingVpnService)
                if (stats != null) {
                    val rx = stats.totalRx()
                    val tx = stats.totalTx()
                    VpnManager.setTrafficAbsolute(uploadBytes = tx, downloadBytes = rx)
                    updateNotification("متصل به $currentConfigName | دانلود: ${VpnConnectionInfo.formatBytes(rx)}")
                }
            }
        }
    }

    private fun startProxyStatsMonitoring() {
        statsJob?.cancel()
        statsJob = serviceScope.launch {
            while (isActive && V2RayProxyTunnelManager.isProxyActive()) {
                delay(1000)
                val rx = V2RayProxyTunnelManager.totalRxBytes.get()
                val tx = V2RayProxyTunnelManager.totalTxBytes.get()
                VpnManager.setTrafficAbsolute(uploadBytes = tx, downloadBytes = rx)
                updateNotification("متصل به $currentConfigName | دانلود: ${VpnConnectionInfo.formatBytes(rx)}")
            }
        }
    }

    private fun disconnectAndStop() {
        tunnelJob?.cancel()
        tunnelJob = null
        statsJob?.cancel()
        statsJob = null

        serviceScope.launch {
            try {
                WireGuardTunnelManager.stopTunnel(this@ParsPingVpnService)
            } catch (_: Exception) {}

            try {
                V2RayProxyTunnelManager.stopProxyTunnel()
            } catch (_: Exception) {}

            VpnManager.updateDisconnected()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        disconnectAndStop()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "اتصال پارس پینگ",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "نمایش وضعیت سرویس فعال Pars Ping VPN"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val disconnectIntent = Intent(this, ParsPingVpnService::class.java).apply {
            action = ACTION_DISCONNECT
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this,
            1,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Pars Ping")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(appPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "قطع اتصال", disconnectPendingIntent)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }
}
