package com.example

import com.example.vpn.ReachabilityResult
import com.example.vpn.VpnConnectionInfo
import com.example.vpn.VpnDiagnostics
import com.example.vpn.VpnStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VpnDiagnosticsTest {

    @Test
    fun testProtocolCoreInspectionAccuratelyReportsMissingNativeBinaries() {
        // WireGuard has real official GoBackend userspace core compiled into the app
        assertTrue(VpnDiagnostics.isProtocolCoreInstalled("WireGuard"))
        assertTrue(VpnDiagnostics.isProtocolCoreInstalled("WG"))

        // VLESS, VMess, Trojan require Xray-core / v2ray-core native binaries
        assertFalse(VpnDiagnostics.isProtocolCoreInstalled("VLESS"))
        assertFalse(VpnDiagnostics.isProtocolCoreInstalled("VMess"))
        assertFalse(VpnDiagnostics.isProtocolCoreInstalled("Trojan"))
        assertFalse(VpnDiagnostics.isProtocolCoreInstalled("Shadowsocks"))
    }

    @Test
    fun testBlankHostFailsReachabilityCleanly() = runBlocking {
        val result = VpnDiagnostics.testServerReachability("", 443)
        assertTrue(result is ReachabilityResult.Failure)
        assertEquals("آدرس سرور خالی است", (result as ReachabilityResult.Failure).reason)
    }

    @Test
    fun testVpnConnectionInfoDiagnosticsFields() {
        val info = VpnConnectionInfo(
            status = VpnStatus.FAILED,
            serverName = "Pars Ping Fast 1",
            serverAddress = "zerofee.ok-exor.ir",
            protocol = "VLESS",
            serverReachabilityMs = 45L,
            dnsStatus = "OK",
            diagnosticMessage = "هسته اجرایی پروتکل VLESS در این پکیج وجود ندارد"
        )

        assertEquals("هسته اجرایی پروتکل VLESS در این پکیج وجود ندارد", info.diagnosticMessage)
        assertEquals(45L, info.serverReachabilityMs)
        assertEquals("OK", info.dnsStatus)
        assertEquals(VpnStatus.FAILED, info.status)
        assertNotNull(info.formattedDuration)
    }
}
