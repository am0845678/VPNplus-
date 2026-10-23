package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ParsTheme

@Composable
fun ProtocolSelectionScreen(
    currentProtocol: String,
    onProtocolSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf(currentProtocol.uppercase()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "انتخاب پروتکل ارتباطی // PROTOCOL",
            color = ParsTheme.palette.textPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "پروتکل مورد نظر خود را برای اتصال بهینه انتخاب نمایید",
            color = ParsTheme.palette.textMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )

        // 1. VLESS Card
        ProtocolCard(
            title = "VLESS (وی‌لس)",
            protocolKey = "VLESS",
            subtitle = "سبک‌ترین پروتکل با حداقل تأخیر گیمینگ و پایداری بالا",
            tags = listOf("XTLS Reality", "WebSocket", "gRPC", "بدون سربار"),
            icon = Icons.Default.Speed,
            isSelected = selected == "VLESS",
            onClick = {
                selected = "VLESS"
                onProtocolSelected("VLESS")
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. VMess Card
        ProtocolCard(
            title = "VMess (وی‌مس)",
            protocolKey = "VMESS",
            subtitle = "رمزنگاری کامل ترافیک با استاندارد V2Ray و عبور مطمئن از فایروال",
            tags = listOf("AEAD Encrypted", "AlterID", "TLS/SNI", "پایداری بالا"),
            icon = Icons.Default.Security,
            isSelected = selected == "VMESS",
            onClick = {
                selected = "VMESS"
                onProtocolSelected("VMESS")
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. WireGuard Card
        ProtocolCard(
            title = "WireGuard (وایرگارد)",
            protocolKey = "WIREGUARD",
            subtitle = "هسته مدرن کرنل لینوکس با رمزنگاری مدرن و پینگ بسیار پایین",
            tags = listOf("GoBackend Active", "Curve25519", "ChaCha20", "UDP Tunnel"),
            icon = Icons.Default.Dns,
            isSelected = selected == "WIREGUARD",
            onClick = {
                selected = "WIREGUARD"
                onProtocolSelected("WIREGUARD")
            }
        )
    }
}

@Composable
private fun ProtocolCard(
    title: String,
    protocolKey: String,
    subtitle: String,
    tags: List<String>,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.border
    val bgColor = if (isSelected) ParsTheme.palette.primaryAccent.copy(alpha = 0.12f) else ParsTheme.palette.cardBg

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.8.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        ParsTheme.palette.glowColor.copy(alpha = 0.3f),
                                        ParsTheme.palette.dimColor
                                    )
                                )
                            )
                            .border(1.dp, ParsTheme.palette.glowColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = ParsTheme.palette.glowColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            color = ParsTheme.palette.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = protocolKey,
                            color = ParsTheme.palette.glowColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "انتخاب شده",
                        tint = ParsTheme.palette.glowColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = subtitle,
                color = ParsTheme.palette.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ParsTheme.palette.surface)
                            .border(0.8.dp, ParsTheme.palette.border, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tag,
                            color = ParsTheme.palette.textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
