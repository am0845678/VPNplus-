package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.VpnConfigEntity
import com.example.ui.theme.CyberTelegram
import com.example.ui.theme.ParsTheme
import com.example.ui.viewmodel.SubscriptionUiNotice

private const val PARS_PING_BOT_URL = "https://t.me/pars_pingbot?start=8361681340"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionSheet(
    urlInput: String,
    isLoading: Boolean,
    notice: SubscriptionUiNotice?,
    configs: List<VpnConfigEntity>,
    onUrlChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onPasteClipboard: () -> Unit,
    onSelectConfig: (Long) -> Unit,
    onDeleteConfig: (VpnConfigEntity) -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    onAddSampleWireGuard: () -> Unit = {},
    onImportConfigFile: (Uri) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportConfigFile(uri)
        }
    }

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
            // Sheet Header with Close Button
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
                    text = "افزودن اشتراک // SUBSCRIPTION",
                    color = ParsTheme.palette.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Subscription Input Form
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ParsTheme.palette.cardBg)
                            .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "لینک اشتراک یا کانفیگ سرور",
                            color = ParsTheme.palette.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "پشتیبانی از WireGuard (.conf/URI) و لینک‌های VMess / VLESS / Trojan / SS",
                            color = ParsTheme.palette.textMuted,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // URL / Config TextField
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = onUrlChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("subscription_url_input"),
                            placeholder = {
                                Text(
                                    text = "https://... یا vless:// یا متن [Interface] وایرگارد",
                                    color = ParsTheme.palette.textMuted,
                                    fontSize = 12.sp
                                )
                            },
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ParsTheme.palette.background,
                                unfocusedContainerColor = ParsTheme.palette.background,
                                focusedBorderColor = ParsTheme.palette.glowColor,
                                unfocusedBorderColor = ParsTheme.palette.border,
                                focusedTextColor = ParsTheme.palette.textPrimary,
                                unfocusedTextColor = ParsTheme.palette.textPrimary
                            ),
                            trailingIcon = {
                                if (urlInput.isNotEmpty()) {
                                    IconButton(onClick = { onUrlChange("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "پاک کردن",
                                            tint = ParsTheme.palette.textMuted
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Paste Clipboard & Submit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Paste from Clipboard Button
                            Button(
                                onClick = onPasteClipboard,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("paste_clipboard_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ParsTheme.palette.cardBgElevated,
                                    contentColor = ParsTheme.palette.textPrimary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ParsTheme.palette.border)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = ParsTheme.palette.glowColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "از کلیپبورد", fontSize = 12.sp)
                            }

                            // Submit & Check Button
                            Button(
                                onClick = onSubmit,
                                enabled = !isLoading,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(48.dp)
                                    .testTag("submit_subscription_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ParsTheme.palette.primaryAccent,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "بررسی و افزودن",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Import WireGuard File (.conf) Button
                        Button(
                            onClick = {
                                try {
                                    filePickerLauncher.launch(arrayOf("*/*"))
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("import_wireguard_file_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF16253B),
                                contentColor = Color(0xFF38BDF8)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileOpen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "افزودن فایل وایرگارد (.conf)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Sample WireGuard Config Button
                        Button(
                            onClick = onAddSampleWireGuard,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("add_sample_wireguard_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ParsTheme.palette.cardBgElevated,
                                contentColor = ParsTheme.palette.textSecondary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ParsTheme.palette.border)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = ParsTheme.palette.glowColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تست سریع: افزودن کانفیگ نمونه WireGuard",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Status / Notification Box
                        if (notice != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            when (notice) {
                                is SubscriptionUiNotice.Success -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ParsTheme.palette.connectedGreen.copy(alpha = 0.15f))
                                            .border(1.dp, ParsTheme.palette.connectedGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = ParsTheme.palette.connectedGreen,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = notice.message,
                                                color = ParsTheme.palette.connectedGreen,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                                is SubscriptionUiNotice.Error -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ParsTheme.palette.errorRed.copy(alpha = 0.15f))
                                            .border(1.dp, ParsTheme.palette.errorRed.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                            .testTag("subscription_error_notice")
                                        ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = null,
                                                tint = ParsTheme.palette.errorRed,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = notice.message,
                                                color = ParsTheme.palette.errorRed,
                                                fontSize = 13.sp,
                                                fontWeight = if (notice.isRestricted) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Buy Config Button
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ParsTheme.palette.cardBg)
                            .border(1.2.dp, CyberTelegram.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "کانفیگ نداری؟ از ربات پارس پینگ خرید کن",
                            color = ParsTheme.palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PARS_PING_BOT_URL))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("buy_config_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberTelegram,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "خرید کانفیگ",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "خرید کانفیگ از پارس پینگ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Section 3: Imported Configs List Header
                item {
                    Text(
                        text = "کانفیگ‌های فعال (${configs.size})",
                        color = ParsTheme.palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Section 4: Imported Config Items
                if (configs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هنوز کانفیگی اضافه نشده است",
                                color = ParsTheme.palette.textMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(configs, key = { it.id }) { config ->
                        ConfigListItem(
                            config = config,
                            onSelect = { onSelectConfig(config.id) },
                            onDelete = { onDeleteConfig(config) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigListItem(
    config: VpnConfigEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (config.isSelected) ParsTheme.palette.dimColor else ParsTheme.palette.cardBg)
            .border(
                width = if (config.isSelected) 1.5.dp else 1.dp,
                color = if (config.isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.border,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 12.dp)
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
                RadioButton(
                    selected = config.isSelected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = ParsTheme.palette.glowColor,
                        unselectedColor = ParsTheme.palette.textMuted
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.name,
                        color = ParsTheme.palette.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = if (config.isSelected) FontWeight.Bold else FontWeight.Medium,
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

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف کانفیگ",
                    tint = ParsTheme.palette.errorRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
