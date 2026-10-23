package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.VpnConfigEntity
import com.example.ui.theme.ParsTheme
import kotlinx.coroutines.launch

@Composable
fun ServerCard(
    selectedConfig: VpnConfigEntity?,
    isTestingPing: Boolean,
    onCardClick: () -> Unit,
    onTestPingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val cardScale = remember { Animatable(1f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale.value)
            .clip(RoundedCornerShape(14.dp))
            .background(ParsTheme.palette.cardBg)
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        ParsTheme.palette.primaryAccent.copy(alpha = 0.5f),
                        ParsTheme.palette.border,
                        ParsTheme.palette.border
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        scope.launch { cardScale.animateTo(0.98f, tween(100)) }
                        tryAwaitRelease()
                        scope.launch { cardScale.animateTo(1f, tween(150)) }
                        onCardClick()
                    }
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("current_server_card")
    ) {
        if (selectedConfig == null) {
            // No server selected view
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ParsTheme.palette.dimColor)
                            .border(1.dp, ParsTheme.palette.glowColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = ParsTheme.palette.glowColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "سرور فعالی انتخاب نشده",
                            color = ParsTheme.palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "جهت افزودن یا انتخاب سرور ضربه بزنید",
                            color = ParsTheme.palette.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "انتخاب سرور",
                    tint = ParsTheme.palette.glowColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // Active server view
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ParsTheme.palette.dimColor)
                            .border(1.dp, ParsTheme.palette.glowColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = ParsTheme.palette.glowColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedConfig.name,
                            color = ParsTheme.palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Protocol badge with sharp tactical border
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ParsTheme.palette.dimColor)
                                    .border(0.8.dp, ParsTheme.palette.glowColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = selectedConfig.protocol,
                                    color = ParsTheme.palette.glowColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Actual Ping latency measurement
                            if (isTestingPing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = ParsTheme.palette.glowColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "سنجش پینگ...",
                                    color = ParsTheme.palette.textMuted,
                                    fontSize = 11.sp
                                )
                            } else if (selectedConfig.latencyMs != null) {
                                val pingColor = when {
                                    selectedConfig.latencyMs < 130 -> ParsTheme.palette.connectedGreen
                                    selectedConfig.latencyMs < 280 -> ParsTheme.palette.warningAmber
                                    else -> ParsTheme.palette.errorRed
                                }
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(pingColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${selectedConfig.latencyMs} ms",
                                    color = pingColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            } else {
                                Text(
                                    text = "پینگ تست نشده",
                                    color = ParsTheme.palette.textMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Test Ping Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ParsTheme.palette.cardBgElevated)
                            .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(8.dp))
                            .clickable(onClick = onTestPingClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = "تست پینگ واقعی",
                            tint = ParsTheme.palette.glowColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Switch Server Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ParsTheme.palette.cardBgElevated)
                            .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(8.dp))
                            .clickable(onClick = onCardClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "تغییر سرور",
                            tint = ParsTheme.palette.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
