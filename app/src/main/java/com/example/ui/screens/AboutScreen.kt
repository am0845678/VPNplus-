package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ParsPingCrown
import com.example.ui.theme.ParsTheme

@Composable
fun AboutScreen(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val telegramChannelUrl = "https://t.me/parsping"

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
                    .padding(20.dp),
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
                        text = "درباره پارس پینگ // ABOUT",
                        color = ParsTheme.palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Crown logo
                ParsPingCrown(
                    modifier = Modifier.size(64.dp),
                    primaryColor = ParsTheme.palette.primaryAccent,
                    glowColor = ParsTheme.palette.glowColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "PARS PING VPN",
                    color = ParsTheme.palette.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "نسخه ۱.۰ (Official Release)",
                    color = ParsTheme.palette.glowColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "پارس پینگ یک ابزار تخصصی و حرفه‌ای جهت کاهش پینگ، دور زدن محدودیت‌های شبکه و تضمین پایداری در بازی‌های آنلاین و استریم است. این اپلیکیشن با طراحی سایبرپانک و هسته‌های پیشرفته VLESS، VMess و WireGuard ارائه شده است.",
                    color = ParsTheme.palette.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Telegram Channel Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ParsTheme.palette.cardBg)
                        .border(1.dp, Color(0xFF24A1DE).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramChannelUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF24A1DE).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = Color(0xFF24A1DE),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "کانال رسمی تلگرام",
                                    color = ParsTheme.palette.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@parsping",
                                    color = Color(0xFF24A1DE),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = "عضویت",
                            color = Color(0xFF24A1DE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Support & Contact Row
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ParsTheme.palette.cardBg)
                        .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            tint = ParsTheme.palette.glowColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "پشتیبانی و ارتباط با ما",
                                color = ParsTheme.palette.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ارسال گزارش باگ و پیشنهادات از طریق کانال رسمی تلگرام",
                                color = ParsTheme.palette.textMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
