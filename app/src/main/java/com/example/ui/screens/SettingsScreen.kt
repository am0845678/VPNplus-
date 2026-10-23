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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ParsTheme
import com.example.ui.viewmodel.VpnViewModel

@Composable
fun SettingsScreen(
    viewModel: VpnViewModel,
    onOpenAbout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentTheme = ParsTheme.palette

    var autoConnect by remember { mutableStateOf(false) }
    var killSwitchNotice by remember { mutableStateOf(true) }
    var selectedDns by remember { mutableStateOf("Cloudflare (1.1.1.1)") }
    var selectedMtu by remember { mutableStateOf("1500 (استاندارد)") }

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
                text = "تنظیمات پیشرفته // SETTINGS",
                color = ParsTheme.palette.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = ParsTheme.palette.glowColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dedicated "تم دلخواه" (Custom Theme) Action Button
        Text(
            text = "ظاهر و شخصی‌سازی:",
            color = ParsTheme.palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ParsTheme.palette.cardBg)
                .border(1.2.dp, ParsTheme.palette.glowColor, RoundedCornerShape(12.dp))
                .clickable { viewModel.openThemeDialog() }
                .padding(16.dp)
                .testTag("custom_theme_button")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(currentTheme.primaryAccent.copy(alpha = 0.2f))
                            .border(1.dp, currentTheme.glowColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = currentTheme.glowColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "تم دلخواه",
                            color = ParsTheme.palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "انتخاب رنگ اصلی، پس‌زمینه و میزان درخشش (فعلی: ${currentTheme.displayName})",
                            color = ParsTheme.palette.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = ParsTheme.palette.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 1. General Settings
        Text(
            text = "تنظیمات عمومی (General):",
            color = ParsTheme.palette.textSecondary,
            fontSize = 12.sp,
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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsSwitchRow(
                    title = "اتصال خودکار هنگام باز شدن اپ",
                    subtitle = "به آخرین سرور انتخاب شده به صورت خودکار متصل شود",
                    checked = autoConnect,
                    onCheckedChange = { autoConnect = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. VPN & Routing Settings
        Text(
            text = "تنظیمات شبکه و DNS (VPN Settings):",
            color = ParsTheme.palette.textSecondary,
            fontSize = 12.sp,
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsItemSelector(
                    icon = Icons.Default.Dns,
                    title = "سرور امن DNS",
                    currentValue = selectedDns,
                    options = listOf("Cloudflare (1.1.1.1)", "Google (8.8.8.8)", "Quad9 (9.9.9.9)"),
                    onSelect = { selectedDns = it }
                )

                Spacer(modifier = Modifier.height(4.dp))

                SettingsItemSelector(
                    icon = Icons.Default.Tune,
                    title = "سایز MTU رابط تونل",
                    currentValue = selectedMtu,
                    options = listOf("1500 (استاندارد)", "1420 (بهینه WireGuard)", "1280 (حداقل)"),
                    onSelect = { selectedMtu = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Privacy & Security Settings
        Text(
            text = "حریم خصوصی و امنیت (Privacy):",
            color = ParsTheme.palette.textSecondary,
            fontSize = 12.sp,
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SettingsFeatureRow(
                    icon = Icons.Default.Lock,
                    title = "خط‌مشی عدم ذخیره لاگ (Zero-Log)",
                    subtitle = "هیچ‌گونه سابقه مرورگر یا محتوای ترافیکی روی دستگاه ثبت نمی‌گردد",
                    tag = "فعال"
                )
                SettingsFeatureRow(
                    icon = Icons.Default.Security,
                    title = "محافظت از نشت سوکت (Socket Protection)",
                    subtitle = "ترافیک خروجی اپ با VpnService.protect از تونل مجزا می‌شود",
                    tag = "امن"
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4. About Pars Ping
        Text(
            text = "اطلاعات برنامه:",
            color = ParsTheme.palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ParsTheme.palette.cardBg)
                .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(12.dp))
                .clickable { onOpenAbout() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ParsTheme.palette.glowColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "درباره پارس پینگ (About Pars Ping)",
                            color = ParsTheme.palette.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "نسخه ۱.۰ • کانال تلگرام و پشتیبانی",
                            color = ParsTheme.palette.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = ParsTheme.palette.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = ParsTheme.palette.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = ParsTheme.palette.textMuted, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ParsTheme.palette.primaryAccent,
                uncheckedThumbColor = ParsTheme.palette.textMuted,
                uncheckedTrackColor = ParsTheme.palette.surface
            )
        )
    }
}

@Composable
private fun SettingsItemSelector(
    icon: ImageVector,
    title: String,
    currentValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = ParsTheme.palette.glowColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, color = ParsTheme.palette.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Text(text = currentValue, color = ParsTheme.palette.glowColor, fontSize = 11.sp)
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEach { opt ->
                    val isSelected = opt == currentValue
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) ParsTheme.palette.primaryAccent.copy(alpha = 0.2f) else ParsTheme.palette.surface)
                            .border(1.dp, if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.border, RoundedCornerShape(6.dp))
                            .clickable {
                                onSelect(opt)
                                expanded = false
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = opt.substringBefore(" ("),
                            color = if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.textPrimary,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ParsTheme.palette.glowColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, color = ParsTheme.palette.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = ParsTheme.palette.textMuted, fontSize = 11.sp)
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(ParsTheme.palette.connectedGreen.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(text = tag, color = ParsTheme.palette.connectedGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
