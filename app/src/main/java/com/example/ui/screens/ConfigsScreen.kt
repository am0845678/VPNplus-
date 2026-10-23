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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.VpnConfigEntity
import com.example.ui.theme.ParsTheme
import com.example.ui.viewmodel.VpnViewModel
import com.example.vpn.VpnStatus

@Composable
fun ConfigsScreen(
    viewModel: VpnViewModel,
    uiState: com.example.ui.viewmodel.VpnScreenUiState,
    onImportFileClick: () -> Unit,
    onConnectConfig: ((VpnConfigEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var searchQuery by remember { mutableStateOf("") }

    val filterTabs = listOf("ALL" to "همه", "VLESS" to "VLESS", "VMESS" to "VMess", "WIREGUARD" to "WireGuard", "TROJAN" to "Trojan")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مدیریت کانفیگ‌ها (${uiState.configs.size})",
                color = ParsTheme.palette.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Add Config (+)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ParsTheme.palette.primaryAccent)
                        .clickable { viewModel.openAddSubscription() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "افزودن کانفیگ",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Paste from Clipboard
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ParsTheme.palette.cardBg)
                        .border(1.dp, ParsTheme.palette.border, CircleShape)
                        .clickable {
                            val clip = clipboardManager.getText()?.text
                            viewModel.handleClipboardUrl(clip)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "جایگذاری از کلیپبورد",
                        tint = ParsTheme.palette.glowColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Manual Form
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ParsTheme.palette.cardBg)
                        .border(1.dp, ParsTheme.palette.border, CircleShape)
                        .clickable { viewModel.openManualAddDialog() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "افزودن دستی",
                        tint = ParsTheme.palette.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Import File
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ParsTheme.palette.cardBg)
                        .border(1.dp, ParsTheme.palette.border, CircleShape)
                        .clickable(onClick = onImportFileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "فایل کانفیگ",
                        tint = ParsTheme.palette.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Live Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("جستجو بر اساس نام، آدرس یا پورت...", fontSize = 12.sp, color = ParsTheme.palette.textMuted) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ParsTheme.palette.glowColor, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "پاک کردن", tint = ParsTheme.palette.textSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ParsTheme.palette.glowColor,
                unfocusedBorderColor = ParsTheme.palette.border,
                focusedContainerColor = ParsTheme.palette.cardBg,
                unfocusedContainerColor = ParsTheme.palette.cardBg,
                focusedTextColor = ParsTheme.palette.textPrimary,
                unfocusedTextColor = ParsTheme.palette.textPrimary
            )
        )

        // Filter Chips (All, VLESS, VMess, WireGuard, Trojan)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterTabs) { (key, label) ->
                val isSelected = uiState.selectedProtocolFilter.equals(key, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ParsTheme.palette.primaryAccent else ParsTheme.palette.cardBg)
                        .border(
                            1.dp,
                            if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.border,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.setProtocolFilter(key) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else ParsTheme.palette.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Secondary Utility Bar: Ping All, Sort by Ping, Delete All
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Ping All Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ParsTheme.palette.cardBg)
                        .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(6.dp))
                        .clickable {
                            if (uiState.isPingingAll) viewModel.cancelPingAll() else viewModel.pingAllConfigs()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.isPingingAll) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = ParsTheme.palette.glowColor
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.NetworkPing,
                                contentDescription = null,
                                tint = ParsTheme.palette.glowColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uiState.isPingingAll) "در حال تست..." else "تست پینگ همه",
                            color = ParsTheme.palette.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sort by ping toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (uiState.isSortedByPing) ParsTheme.palette.primaryAccent.copy(alpha = 0.2f) else ParsTheme.palette.cardBg)
                        .border(
                            1.dp,
                            if (uiState.isSortedByPing) ParsTheme.palette.glowColor else ParsTheme.palette.border,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { viewModel.toggleSortByPing() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            tint = if (uiState.isSortedByPing) ParsTheme.palette.glowColor else ParsTheme.palette.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مرتب‌سازی پینگ",
                            color = if (uiState.isSortedByPing) ParsTheme.palette.glowColor else ParsTheme.palette.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (uiState.configs.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.openDeleteAllConfirm() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف همه",
                        tint = ParsTheme.palette.errorRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Filtered configs by Protocol AND Search Query
        val displayConfigs = uiState.filteredConfigs.filter { cfg ->
            if (searchQuery.isBlank()) true
            else {
                cfg.name.contains(searchQuery, ignoreCase = true) ||
                        cfg.serverAddress.contains(searchQuery, ignoreCase = true) ||
                        cfg.serverPort.toString().contains(searchQuery) ||
                        cfg.protocol.contains(searchQuery, ignoreCase = true)
            }
        }

        if (displayConfigs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "کانفیگی با این مشخصات یافت نشد" else "هیچ کانفیگی یافت نشد",
                        color = ParsTheme.palette.textMuted,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.openAddSubscription() },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("افزودن کانفیگ جدید")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayConfigs, key = { it.id }) { config ->
                    ConfigItemCard(
                        config = config,
                        isSelected = config.isSelected,
                        isTestingPing = (uiState.isTestingPingForId == config.id),
                        isConnected = (config.isSelected && uiState.connectionInfo.status == VpnStatus.CONNECTED),
                        onSelect = { viewModel.selectConfig(config.id) },
                        onTestPing = { viewModel.testPingForConfig(config) },
                        onEdit = { viewModel.openEditConfig(config) },
                        onDelete = { viewModel.deleteConfig(config) },
                        onShare = {
                            val uri = config.toShareableUri()
                            clipboardManager.setText(AnnotatedString(uri))
                        },
                        onConnect = {
                            viewModel.selectConfig(config.id)
                            onConnectConfig?.invoke(config)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ConfigItemCard(
    config: VpnConfigEntity,
    isSelected: Boolean,
    isTestingPing: Boolean,
    isConnected: Boolean,
    onSelect: () -> Unit,
    onTestPing: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onConnect: () -> Unit
) {
    val borderColor = if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.border
    val bgColor = if (isSelected) ParsTheme.palette.primaryAccent.copy(alpha = 0.12f) else ParsTheme.palette.cardBg

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "انتخاب شده",
                            tint = if (isConnected) ParsTheme.palette.connectedGreen else ParsTheme.palette.glowColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = config.name,
                        color = ParsTheme.palette.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }

                // Protocol Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ParsTheme.palette.primaryAccent.copy(alpha = 0.2f))
                        .border(1.dp, ParsTheme.palette.glowColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = config.protocol.uppercase(),
                        color = ParsTheme.palette.glowColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Server Address & Port
            Text(
                text = "${config.serverAddress}:${config.serverPort}",
                color = ParsTheme.palette.textMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Latency badge, test ping button, connect button, edit, share, delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Latency Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ParsTheme.palette.background)
                        .clickable(onClick = onTestPing)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isTestingPing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.2.dp,
                                color = ParsTheme.palette.glowColor
                            )
                        } else {
                            val latency = config.latencyMs
                            val latencyColor = when {
                                latency == null -> ParsTheme.palette.textMuted
                                latency < 150 -> ParsTheme.palette.connectedGreen
                                latency < 350 -> ParsTheme.palette.warningAmber
                                else -> ParsTheme.palette.errorRed
                            }
                            Text(
                                text = if (latency != null) "$latency ms" else "تست پینگ",
                                color = latencyColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Quick Connect button & Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isConnected) ParsTheme.palette.connectedGreen.copy(alpha = 0.2f) else ParsTheme.palette.primaryAccent.copy(alpha = 0.2f))
                            .border(1.dp, if (isConnected) ParsTheme.palette.connectedGreen else ParsTheme.palette.glowColor, RoundedCornerShape(7.dp))
                            .clickable(onClick = onConnect)
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = if (isConnected) ParsTheme.palette.connectedGreen else ParsTheme.palette.glowColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isConnected) "متصل" else "اتصال",
                                color = if (isConnected) ParsTheme.palette.connectedGreen else ParsTheme.palette.glowColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Share Link
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(ParsTheme.palette.background)
                            .border(0.8.dp, ParsTheme.palette.border, RoundedCornerShape(7.dp))
                            .clickable(onClick = onShare),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "کپی لینک کانفیگ",
                            tint = ParsTheme.palette.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Edit Config
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(ParsTheme.palette.background)
                            .border(0.8.dp, ParsTheme.palette.border, RoundedCornerShape(7.dp))
                            .clickable(onClick = onEdit),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = ParsTheme.palette.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Delete Config
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(ParsTheme.palette.background)
                            .border(0.8.dp, ParsTheme.palette.border, RoundedCornerShape(7.dp))
                            .clickable(onClick = onDelete),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = ParsTheme.palette.errorRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
