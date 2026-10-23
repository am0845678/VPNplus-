package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ParsTheme

@Composable
fun ManualConfigDialog(
    onSave: (
        name: String,
        protocol: String,
        address: String,
        port: Int,
        uuidOrPassword: String,
        security: String,
        networkType: String,
        pathOrSni: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val supportedProtocols = listOf("VLESS", "VMess", "WireGuard", "Trojan")
    val supportedTransports = listOf("tcp", "ws", "grpc")
    val supportedSecurities = listOf("none", "tls", "reality")

    var selectedProtocol by remember { mutableStateOf("VLESS") }
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var portText by remember { mutableStateOf("443") }
    var uuidOrPassword by remember { mutableStateOf("") }
    var selectedSecurity by remember { mutableStateOf("tls") }
    var selectedTransport by remember { mutableStateOf("tcp") }
    var pathOrSni by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.2.dp, ParsTheme.palette.glowColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            color = ParsTheme.palette.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
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
                        text = "افزودن دستی کانفیگ",
                        color = ParsTheme.palette.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Protocol Selector
                Text(
                    text = "انتخاب پروتکل:",
                    color = ParsTheme.palette.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    supportedProtocols.forEach { proto ->
                        val isSelected = selectedProtocol == proto
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ParsTheme.palette.primaryAccent.copy(alpha = 0.2f) else ParsTheme.palette.cardBg)
                                .border(
                                    1.dp,
                                    if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.border,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedProtocol = proto
                                    if (proto == "WireGuard" && portText == "443") {
                                        portText = "51820"
                                    }
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = proto,
                                color = if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام نمایشی سرور") },
                    placeholder = { Text("مثلاً: سرور آلمان پارس پینگ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ParsTheme.palette.glowColor,
                        unfocusedBorderColor = ParsTheme.palette.border,
                        focusedTextColor = ParsTheme.palette.textPrimary,
                        unfocusedTextColor = ParsTheme.palette.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Server Address & Port
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("آدرس سرور (IP یا دامنه)") },
                        singleLine = true,
                        modifier = Modifier.weight(2f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ParsTheme.palette.glowColor,
                            unfocusedBorderColor = ParsTheme.palette.border,
                            focusedTextColor = ParsTheme.palette.textPrimary,
                            unfocusedTextColor = ParsTheme.palette.textPrimary
                        )
                    )

                    OutlinedTextField(
                        value = portText,
                        onValueChange = { portText = it },
                        label = { Text("پورت") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ParsTheme.palette.glowColor,
                            unfocusedBorderColor = ParsTheme.palette.border,
                            focusedTextColor = ParsTheme.palette.textPrimary,
                            unfocusedTextColor = ParsTheme.palette.textPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // UUID or Password Field
                OutlinedTextField(
                    value = uuidOrPassword,
                    onValueChange = { uuidOrPassword = it },
                    label = {
                        Text(if (selectedProtocol == "Trojan") "رمز عبور (Password)" else if (selectedProtocol == "WireGuard") "کلید خصوصی (PrivateKey)" else "شناسه کاربری (UUID)")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ParsTheme.palette.glowColor,
                        unfocusedBorderColor = ParsTheme.palette.border,
                        focusedTextColor = ParsTheme.palette.textPrimary,
                        unfocusedTextColor = ParsTheme.palette.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Transport Type (for VLESS / VMess)
                if (selectedProtocol == "VLESS" || selectedProtocol == "VMess") {
                    Text(
                        text = "نوع انتقال (Transport):",
                        color = ParsTheme.palette.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        supportedTransports.forEach { transport ->
                            val isSelected = selectedTransport == transport
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) ParsTheme.palette.primaryAccent.copy(alpha = 0.2f) else ParsTheme.palette.cardBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.border,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedTransport = transport }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = transport.uppercase(),
                                    color = if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.textPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Security (TLS / Reality / None)
                if (selectedProtocol != "WireGuard") {
                    Text(
                        text = "امنیت و رمزنگاری (Security):",
                        color = ParsTheme.palette.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        supportedSecurities.forEach { sec ->
                            val isSelected = selectedSecurity == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) ParsTheme.palette.primaryAccent.copy(alpha = 0.2f) else ParsTheme.palette.cardBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.border,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedSecurity = sec }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sec.uppercase(),
                                    color = if (isSelected) ParsTheme.palette.glowColor else ParsTheme.palette.textPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // SNI or Path Field
                    OutlinedTextField(
                        value = pathOrSni,
                        onValueChange = { pathOrSni = it },
                        label = { Text("SNI یا مسیر وب‌سوکت (Path / Host)") },
                        placeholder = { Text("مثلاً: cdn.cloudflare.com یا /ws") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ParsTheme.palette.glowColor,
                            unfocusedBorderColor = ParsTheme.palette.border,
                            focusedTextColor = ParsTheme.palette.textPrimary,
                            unfocusedTextColor = ParsTheme.palette.textPrimary
                        )
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = ParsTheme.palette.errorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val p = portText.toIntOrNull()
                        if (address.isBlank()) {
                            errorMessage = "لطفاً آدرس سرور را وارد کنید"
                            return@Button
                        }
                        if (p == null || p !in 1..65535) {
                            errorMessage = "پورت باید بین ۱ تا ۶۵۵۳۵ باشد"
                            return@Button
                        }
                        if (uuidOrPassword.isBlank()) {
                            errorMessage = "لطفاً شناسه UUID یا کلید خصوصی را وارد کنید"
                            return@Button
                        }

                        onSave(
                            name,
                            selectedProtocol,
                            address,
                            p,
                            uuidOrPassword,
                            selectedSecurity,
                            selectedTransport,
                            pathOrSni
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ParsTheme.palette.primaryAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ذخیره و اعمال کانفیگ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
