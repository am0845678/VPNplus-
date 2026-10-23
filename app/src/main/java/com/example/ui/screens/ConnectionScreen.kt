package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ParsTheme
import com.example.vpn.VpnConnectionInfo
import com.example.vpn.VpnStatus

@Composable
fun ConnectionScreen(
    connectionInfo: VpnConnectionInfo,
    onToggleVpn: () -> Unit,
    onDismiss: () -> Unit
) {
    val status = connectionInfo.status
    val infiniteTransition = rememberInfiniteTransition(label = "ConnectionRings")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val statusColor = when (status) {
        VpnStatus.CONNECTED -> ParsTheme.palette.connectedGreen
        VpnStatus.CONNECTING -> ParsTheme.palette.warningAmber
        VpnStatus.FAILED -> ParsTheme.palette.errorRed
        VpnStatus.DISCONNECTED, VpnStatus.DISCONNECTING -> ParsTheme.palette.glowColor
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, ParsTheme.palette.glowColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = ParsTheme.palette.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = ParsTheme.palette.textSecondary
                        )
                    }

                    Text(
                        text = "وضعیت اتصال تونل // TUNNEL",
                        color = ParsTheme.palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Animated Circular Connection Indicator
                val ringBorderColor = ParsTheme.palette.border
                val ringGlowColor = ParsTheme.palette.glowColor
                Box(
                    modifier = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 5.dp.toPx()
                        // Outer background ring
                        drawCircle(
                            color = ringBorderColor.copy(alpha = 0.4f),
                            style = Stroke(width = strokeWidth)
                        )

                        // Animated glow arc
                        if (status == VpnStatus.CONNECTING || status == VpnStatus.CONNECTED) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(
                                        statusColor.copy(alpha = 0.2f),
                                        statusColor,
                                        ringGlowColor
                                    )
                                ),
                                startAngle = rotation,
                                sweepAngle = if (status == VpnStatus.CONNECTED) 360f else 180f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth * pulse, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Inner Circular Core Button
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        statusColor.copy(alpha = 0.25f),
                                        ParsTheme.palette.cardBg
                                    )
                                )
                            )
                            .border(2.dp, statusColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (status == VpnStatus.CONNECTED) Icons.Default.Shield else Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (status == VpnStatus.CONNECTED) connectionInfo.formattedDuration else status.persianTitle,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Server & Protocol Info Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ParsTheme.palette.cardBg)
                        .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "سرور فعال:", color = ParsTheme.palette.textMuted, fontSize = 12.sp)
                            Text(
                                text = connectionInfo.serverName ?: "انتخاب نشده",
                                color = ParsTheme.palette.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "پروتکل:", color = ParsTheme.palette.textMuted, fontSize = 12.sp)
                            Text(
                                text = connectionInfo.protocol ?: "—",
                                color = ParsTheme.palette.glowColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (connectionInfo.diagnosticMessage != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "مرحله عیب‌یابی:", color = ParsTheme.palette.textMuted, fontSize = 11.sp)
                                Text(
                                    text = connectionInfo.diagnosticMessage,
                                    color = statusColor,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Connect / Disconnect Action Button
                Button(
                    onClick = {
                        onToggleVpn()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == VpnStatus.CONNECTED || status == VpnStatus.CONNECTING) ParsTheme.palette.errorRed else ParsTheme.palette.primaryAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (status == VpnStatus.CONNECTED) "قطع اتصال تونل" else if (status == VpnStatus.CONNECTING) "لغو در حال اتصال..." else "برقراری اتصال تونل",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
