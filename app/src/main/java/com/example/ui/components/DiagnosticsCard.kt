package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ParsTheme
import com.example.vpn.VpnConnectionInfo
import com.example.vpn.VpnStatus

@Composable
fun DiagnosticsCard(
    connectionInfo: VpnConnectionInfo,
    modifier: Modifier = Modifier
) {
    if (connectionInfo.status == VpnStatus.DISCONNECTED && connectionInfo.diagnosticMessage == null) {
        return
    }

    val isError = connectionInfo.status == VpnStatus.FAILED
    val isConnecting = connectionInfo.status == VpnStatus.CONNECTING
    val isConnected = connectionInfo.status == VpnStatus.CONNECTED

    val borderColor = when {
        isError -> ParsTheme.palette.errorRed.copy(alpha = 0.7f)
        isConnected -> ParsTheme.palette.connectedGreen.copy(alpha = 0.6f)
        else -> ParsTheme.palette.border
    }

    val bgColor = when {
        isError -> Color(0xFF1E080C)
        isConnected -> Color(0xFF081C10)
        else -> ParsTheme.palette.cardBg
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("vpn_diagnostics_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isError) Icons.Default.Warning else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isError) ParsTheme.palette.errorRed
                               else if (isConnected) ParsTheme.palette.connectedGreen
                               else ParsTheme.palette.glowColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "وضعیت عیب‌یابی اتصال // DIAGNOSTICS",
                        color = ParsTheme.palette.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                if (connectionInfo.serverReachabilityMs != null) {
                    Text(
                        text = "پینگ سوکت: ${connectionInfo.serverReachabilityMs} ms",
                        color = ParsTheme.palette.connectedGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val displayMsg = connectionInfo.diagnosticMessage
                ?: connectionInfo.errorMessage
                ?: if (isConnecting) "در حال اتصال به سرور..." else "آماده به کار"

            Text(
                text = displayMsg,
                color = if (isError) ParsTheme.palette.errorRed else ParsTheme.palette.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = if (isError) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
