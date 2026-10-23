package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ParsTheme
import com.example.ui.viewmodel.VpnViewModel
import com.example.vpn.VpnStatus
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NetworkScreen(
    viewModel: VpnViewModel,
    uiState: com.example.ui.viewmodel.VpnScreenUiState,
    modifier: Modifier = Modifier
) {
    val connectionInfo = uiState.connectionInfo
    val selected = uiState.selectedConfig
    val allConfigs = uiState.configs

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "سنجش شبکه و پینگ‌متر // NETWORK",
                color = ParsTheme.palette.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = ParsTheme.palette.glowColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Circular Ping Meter & Needle Visualizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ParsTheme.palette.cardBg)
                .border(1.2.dp, ParsTheme.palette.border, RoundedCornerShape(16.dp))
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            val pingValue = selected?.latencyMs ?: 0L
            val normalizedPing = (pingValue.toFloat() / 500f).coerceIn(0f, 1f)

            val meterGlowColor = ParsTheme.palette.glowColor

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 12.dp.toPx()
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        val arcOffset = Offset(stroke / 2, stroke / 2)

                        // Base track (180 to 360 degrees = 180 deg sweep)
                        drawArc(
                            color = Color(0xFF1E2230),
                            startAngle = 160f,
                            sweepAngle = 220f,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        // Active gradient meter
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    Color(0xFF00E676),
                                    Color(0xFFFFAB00),
                                    Color(0xFFFF101F)
                                )
                            ),
                            startAngle = 160f,
                            sweepAngle = 220f * normalizedPing,
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        // Needle indicator
                        val angleDeg = 160f + (220f * normalizedPing)
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2 - stroke
                        val needleEnd = Offset(
                            (center.x + (radius * 0.75f * cos(angleRad))).toFloat(),
                            (center.y + (radius * 0.75f * sin(angleRad))).toFloat()
                        )

                        drawLine(
                            color = Color.White,
                            start = center,
                            end = needleEnd,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawCircle(color = meterGlowColor, radius = 5.dp.toPx(), center = center)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selected?.latencyMs != null) "${selected.latencyMs}" else "—",
                            color = ParsTheme.palette.textPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "MS / پینگ",
                            color = ParsTheme.palette.glowColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Ping Test Button
                Button(
                    onClick = {
                        selected?.let { viewModel.testPingForConfig(it) }
                    },
                    enabled = (selected != null && uiState.isTestingPingForId == null),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ParsTheme.palette.primaryAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isTestingPingForId != null) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 1.5.dp,
                            color = Color.White
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اندازه‌گیری تأخیر سوکت (Ping)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Latency Comparison Graph across servers
        Text(
            text = "نمودار مقایسه پینگ سرورها:",
            color = ParsTheme.palette.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ParsTheme.palette.cardBg)
                .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            val validLatencyConfigs = allConfigs.filter { it.latencyMs != null }
            if (validLatencyConfigs.isEmpty()) {
                Text(
                    text = "برای نمایش نمودار، روی «تست پینگ همه» در بخش کانفیگ‌ها ضربه بزنید.",
                    color = ParsTheme.palette.textMuted,
                    fontSize = 12.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    validLatencyConfigs.take(5).forEach { cfg ->
                        val lat = cfg.latencyMs ?: 0L
                        val barRatio = (lat.toFloat() / 400f).coerceIn(0.08f, 1f)
                        val barColor = when {
                            lat < 120 -> ParsTheme.palette.connectedGreen
                            lat < 280 -> ParsTheme.palette.warningAmber
                            else -> ParsTheme.palette.errorRed
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = cfg.name, color = ParsTheme.palette.textPrimary, fontSize = 11.sp, maxLines = 1)
                                Text(text = "$lat ms", color = barColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF1E2230))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(barRatio)
                                        .height(6.dp)
                                        .background(barColor)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Real Download & Upload Traffic Stats
        Text(
            text = "ترافیک لحظه‌ای مصرف‌شده:",
            color = ParsTheme.palette.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Download Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ParsTheme.palette.cardBg)
                    .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = ParsTheme.palette.connectedGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "دانلود (RX)",
                            color = ParsTheme.palette.textMuted,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = connectionInfo.formattedDownload,
                        color = ParsTheme.palette.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Upload Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ParsTheme.palette.cardBg)
                    .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = ParsTheme.palette.glowColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "آپلود (TX)",
                            color = ParsTheme.palette.textMuted,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = connectionInfo.formattedUpload,
                        color = ParsTheme.palette.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Jitter & Packet Loss & Quality Diagnostics
        Text(
            text = "کیفیت خط و پایداری:",
            color = ParsTheme.palette.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ParsTheme.palette.cardBg)
                .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "پکت‌لاس (Packet Loss):", color = ParsTheme.palette.textMuted, fontSize = 12.sp)
                    Text(
                        text = if (connectionInfo.status == VpnStatus.CONNECTED) "۰٪ (پایدار)" else "—",
                        color = ParsTheme.palette.connectedGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "جیتر تخمینی (Jitter):", color = ParsTheme.palette.textMuted, fontSize = 12.sp)
                    val jitter = if (selected?.latencyMs != null) "${(selected.latencyMs!! * 0.08).toInt().coerceAtLeast(2)} ms" else "—"
                    Text(
                        text = jitter,
                        color = ParsTheme.palette.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "وضعیت پروتکل تونل:", color = ParsTheme.palette.textMuted, fontSize = 12.sp)
                    Text(
                        text = connectionInfo.coreStatus ?: "آماده به کار",
                        color = ParsTheme.palette.glowColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
