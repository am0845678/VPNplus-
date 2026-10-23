package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.VpnConfigEntity
import com.example.ui.theme.ParsTheme

@Composable
fun EditConfigDialog(
    config: VpnConfigEntity,
    onSave: (VpnConfigEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(config.name) }
    var serverAddress by remember { mutableStateOf(config.serverAddress) }
    var serverPort by remember { mutableStateOf(config.serverPort.toString()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.2.dp, ParsTheme.palette.glowColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .testTag("edit_config_dialog"),
            color = ParsTheme.palette.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ویرایش کانفیگ // EDIT",
                            color = ParsTheme.palette.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = ParsTheme.palette.glowColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام سرور / عنوان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ParsTheme.palette.glowColor,
                        unfocusedBorderColor = ParsTheme.palette.border,
                        focusedLabelColor = ParsTheme.palette.glowColor,
                        unfocusedLabelColor = ParsTheme.palette.textMuted,
                        focusedTextColor = ParsTheme.palette.textPrimary,
                        unfocusedTextColor = ParsTheme.palette.textPrimary,
                        focusedContainerColor = ParsTheme.palette.cardBg,
                        unfocusedContainerColor = ParsTheme.palette.cardBg
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Server address field
                OutlinedTextField(
                    value = serverAddress,
                    onValueChange = { serverAddress = it },
                    label = { Text("آدرس سرور (دامنه یا IP)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ParsTheme.palette.glowColor,
                        unfocusedBorderColor = ParsTheme.palette.border,
                        focusedLabelColor = ParsTheme.palette.glowColor,
                        unfocusedLabelColor = ParsTheme.palette.textMuted,
                        focusedTextColor = ParsTheme.palette.textPrimary,
                        unfocusedTextColor = ParsTheme.palette.textPrimary,
                        focusedContainerColor = ParsTheme.palette.cardBg,
                        unfocusedContainerColor = ParsTheme.palette.cardBg
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Port field
                OutlinedTextField(
                    value = serverPort,
                    onValueChange = { serverPort = it },
                    label = { Text("پورت (Port)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ParsTheme.palette.glowColor,
                        unfocusedBorderColor = ParsTheme.palette.border,
                        focusedLabelColor = ParsTheme.palette.glowColor,
                        unfocusedLabelColor = ParsTheme.palette.textMuted,
                        focusedTextColor = ParsTheme.palette.textPrimary,
                        unfocusedTextColor = ParsTheme.palette.textPrimary,
                        focusedContainerColor = ParsTheme.palette.cardBg,
                        unfocusedContainerColor = ParsTheme.palette.cardBg
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ParsTheme.palette.cardBgElevated,
                            contentColor = ParsTheme.palette.textSecondary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("انصراف")
                    }

                    Button(
                        onClick = {
                            val parsedPort = serverPort.toIntOrNull() ?: config.serverPort
                            val updated = config.copy(
                                name = name.trim().ifBlank { config.name },
                                serverAddress = serverAddress.trim().ifBlank { config.serverAddress },
                                serverPort = parsedPort
                            )
                            onSave(updated)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ParsTheme.palette.primaryAccent,
                            contentColor = ParsTheme.palette.textPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ذخیره تغییرات", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
