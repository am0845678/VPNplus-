package com.example.vpn

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.VpnConfigEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object VpnManager {
    private val _connectionInfo = MutableStateFlow(VpnConnectionInfo())
    val connectionInfo: StateFlow<VpnConnectionInfo> = _connectionInfo.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startVpn(context: Context, config: VpnConfigEntity) {
        if (_connectionInfo.value.status == VpnStatus.CONNECTING ||
            _connectionInfo.value.status == VpnStatus.CONNECTED
        ) {
            return
        }

        // Initialize connecting state with clear diagnostic progress
        _connectionInfo.value = VpnConnectionInfo(
            status = VpnStatus.CONNECTING,
            serverName = config.name,
            serverAddress = config.serverAddress,
            protocol = config.protocol,
            durationSeconds = 0,
            errorMessage = null,
            diagnosticMessage = "در حال شروع سرویس و بررسی هسته اتصال..."
        )

        val intent = Intent(context, ParsPingVpnService::class.java).apply {
            action = ParsPingVpnService.ACTION_CONNECT
            putExtra(ParsPingVpnService.EXTRA_CONFIG_NAME, config.name)
            putExtra(ParsPingVpnService.EXTRA_SERVER_HOST, config.serverAddress)
            putExtra(ParsPingVpnService.EXTRA_SERVER_PORT, config.serverPort)
            putExtra(ParsPingVpnService.EXTRA_PROTOCOL, config.protocol)
            putExtra(ParsPingVpnService.EXTRA_UUID_OR_PASSWORD, config.uuidOrPassword)
            putExtra(ParsPingVpnService.EXTRA_SECURITY, config.security)
            putExtra(ParsPingVpnService.EXTRA_NETWORK_TYPE, config.networkType)
            putExtra(ParsPingVpnService.EXTRA_PATH_OR_SNI, config.pathOrSni)
            putExtra(ParsPingVpnService.EXTRA_RAW_URI, config.rawUri)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopVpn(context: Context) {
        if (_connectionInfo.value.status == VpnStatus.DISCONNECTED) return

        _connectionInfo.value = _connectionInfo.value.copy(
            status = VpnStatus.DISCONNECTING,
            diagnosticMessage = "در حال قطع اتصال و آزادسازی ترافیک..."
        )

        val intent = Intent(context, ParsPingVpnService::class.java).apply {
            action = ParsPingVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    fun updateConnectingStage(stage: String) {
        _connectionInfo.value = _connectionInfo.value.copy(
            status = VpnStatus.CONNECTING,
            diagnosticMessage = stage
        )
    }

    fun updateConnected(
        serverName: String,
        serverAddress: String,
        protocol: String,
        reachabilityMs: Long? = null,
        dnsStatus: String? = "OK",
        httpsStatus: String? = "Verified",
        coreStatus: String? = "Active"
    ) {
        _connectionInfo.value = _connectionInfo.value.copy(
            status = VpnStatus.CONNECTED,
            serverName = serverName,
            serverAddress = serverAddress,
            protocol = protocol,
            errorMessage = null,
            diagnosticMessage = "اتصال و ترافیک اینترنت تأیید شد",
            serverReachabilityMs = reachabilityMs,
            dnsStatus = dnsStatus,
            httpsStatus = httpsStatus,
            coreStatus = coreStatus
        )
        startTimer()
    }

    fun updateDisconnected() {
        stopTimer()
        _connectionInfo.value = VpnConnectionInfo(
            status = VpnStatus.DISCONNECTED,
            durationSeconds = 0,
            uploadBytes = 0,
            downloadBytes = 0,
            errorMessage = null,
            diagnosticMessage = "سرویس غیرفعال است"
        )
    }

    fun updateFailed(message: String, diagnostic: String? = null) {
        stopTimer()
        _connectionInfo.value = _connectionInfo.value.copy(
            status = VpnStatus.FAILED,
            errorMessage = message,
            diagnosticMessage = diagnostic ?: message,
            durationSeconds = 0
        )
    }

    fun updateTraffic(uploadDelta: Long, downloadDelta: Long) {
        val current = _connectionInfo.value
        _connectionInfo.value = current.copy(
            uploadBytes = current.uploadBytes + uploadDelta,
            downloadBytes = current.downloadBytes + downloadDelta
        )
    }

    fun setTrafficAbsolute(uploadBytes: Long, downloadBytes: Long) {
        val current = _connectionInfo.value
        _connectionInfo.value = current.copy(
            uploadBytes = uploadBytes,
            downloadBytes = downloadBytes
        )
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            var seconds = 0L
            while (isActive) {
                delay(1000)
                seconds++
                _connectionInfo.value = _connectionInfo.value.copy(durationSeconds = seconds)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}
