package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.VpnConfigEntity
import com.example.ui.theme.ParsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectorSheet(
    configs: List<VpnConfigEntity>,
    selectedConfig: VpnConfigEntity?,
    isTestingPingForId: Long?,
    isPingingAll: Boolean,
    isSortedByPing: Boolean,
    onSelectConfig: (Long) -> Unit,
    onTestPing: (VpnConfigEntity) -> Unit,
    onPingAll: () -> Unit,
    onCancelPingAll: () -> Unit,
    onToggleSort: () -> Unit,
    onEditConfig: (VpnConfigEntity) -> Unit,
    onDeleteConfig: (VpnConfigEntity) -> Unit,
    onDeleteAllClick: () -> Unit,
    onOpenAddSubscription: () -> Unit,
    onImportFileClick: () -> Unit = {},
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    // Rotation animation for Radar scanner
    val infiniteTransition = rememberInfiniteTransition(label = "radar_spin")
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_angle"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ParsTheme.palette.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(ParsTheme.palette.border)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header with Close, Add, File, and Delete All
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
                    text = "سرورهای اتصال // SERVERS",
                    color = ParsTheme.palette.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (configs.isNotEmpty()) {
                        IconButton(onClick = onDeleteAllClick) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "حذف همه کانفیگ‌ها",
                                tint = ParsTheme.palette.errorRed.copy(alpha = 0.85f)
                            )
                        }
                    }

                    IconButton(onClick = onImportFileClick) {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = "افزودن فایل وایرگارد",
                            tint = Color(0xFF38BDF8)
                        )
                    }

                    IconButton(onClick = onOpenAddSubscription) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "افزودن اشتراک",
                            tint = ParsTheme.palette.glowColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: PING ALL and SORT
            if (configs.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // PING ALL Button (or CANCEL button when pinging)
                    Button(
                        onClick = {
                            if (isPingingAll) onCancelPingAll() else onPingAll()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("ping_all_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ParsTheme.palette.cardBg,
                            contentColor = ParsTheme.palette.textPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.2.dp,
                            if (isPingingAll) ParsTheme.palette.errorRed else ParsTheme.palette.primaryAccent
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isPingingAll) {
                                Icon(
                                    imageVector = Icons.Default.Radar,
                                    contentDescription = null,
                                    tint = ParsTheme.palette.glowColor,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .rotate(radarAngle)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "لغو اسکن پینگ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ParsTheme.palette.errorRed
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.NetworkCheck,
                                    contentDescription = null,
                                    tint = ParsTheme.palette.glowColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PING ALL // سنجش پینگ همه",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // SORT BY PING Button
                    Button(
                        onClick = onToggleSort,
                        modifier = Modifier
                            .height(46.dp)
                            .testTag("sort_by_ping_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSortedByPing) ParsTheme.palette.dimColor else ParsTheme.palette.cardBg,
                            contentColor = ParsTheme.palette.textPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSortedByPing) ParsTheme.palette.glowColor else ParsTheme.palette.border
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "مرتب‌سازی پینگ",
                                tint = if (isSortedByPing) ParsTheme.palette.glowColor else ParsTheme.palette.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSortedByPing) "کمترین پینگ" else "مرتب‌سازی",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSortedByPing) ParsTheme.palette.glowColor else ParsTheme.palette.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Server List
            if (configs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = ParsTheme.palette.textMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "هیچ سروری اضافه نشده است",
                        color = ParsTheme.palette.textMuted,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenAddSubscription,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ParsTheme.palette.primaryAccent,
                            contentColor = ParsTheme.palette.textPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "افزودن اشتراک جدید")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(configs, key = { it.id }) { config ->
                        val isSelected = config.id == selectedConfig?.id
                        val isTestingThis = isTestingPingForId == config.id

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ParsTheme.palette.dimColor else ParsTheme.palette.cardBg)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.border,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectConfig(config.id) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("server_item_${config.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Radio / Check Indicator
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) ParsTheme.palette.primaryAccent.copy(alpha = 0.25f)
                                                else ParsTheme.palette.cardBgElevated
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.border,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Dns,
                                            contentDescription = null,
                                            tint = if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = config.name,
                                            color = ParsTheme.palette.textPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(ParsTheme.palette.dimColor)
                                                    .border(0.8.dp, ParsTheme.palette.glowColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = config.protocol,
                                                    color = ParsTheme.palette.glowColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = "${config.serverAddress}:${config.serverPort}",
                                                color = ParsTheme.palette.textMuted,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                // Ping value, Ping action, Edit, and Delete action
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isTestingThis) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = ParsTheme.palette.glowColor,
                                            strokeWidth = 2.dp
                                        )
                                    } else if (config.latencyMs != null) {
                                        val color = when {
                                            config.latencyMs < 130 -> ParsTheme.palette.connectedGreen
                                            config.latencyMs < 280 -> ParsTheme.palette.warningAmber
                                            else -> ParsTheme.palette.errorRed
                                        }
                                        Text(
                                            text = "${config.latencyMs} ms",
                                            color = color,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(2.dp))

                                    // Ping Single Server Icon
                                    IconButton(
                                        onClick = { onTestPing(config) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NetworkCheck,
                                            contentDescription = "تست پینگ",
                                            tint = ParsTheme.palette.glowColor,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }

                                    // Edit Config Icon
                                    IconButton(
                                        onClick = { onEditConfig(config) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "ویرایش کانفیگ",
                                            tint = ParsTheme.palette.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Delete Config Icon
                                    IconButton(
                                        onClick = { onDeleteConfig(config) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف کانفیگ",
                                            tint = ParsTheme.palette.errorRed.copy(alpha = 0.85f),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
