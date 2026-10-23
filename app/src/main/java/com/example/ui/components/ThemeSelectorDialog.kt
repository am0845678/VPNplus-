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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BackgroundMode
import com.example.ui.theme.GlowIntensity
import com.example.ui.theme.ParsTheme
import com.example.ui.theme.ParsThemePalette
import com.example.ui.theme.ThemePresets

@Composable
fun ThemeSelectorDialog(
    currentPalette: ParsThemePalette,
    onSelectPreset: (ParsThemePalette) -> Unit,
    onSelectCustomColor: (Color) -> Unit,
    onUpdateBackgroundMode: (BackgroundMode) -> Unit = {},
    onUpdateGlowIntensity: (GlowIntensity) -> Unit = {},
    onRestoreDefault: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var isCustomPickerOpen by remember { mutableStateOf(false) }

    // RGB Slider state for custom picker
    var redValue by remember {
        mutableFloatStateOf(currentPalette.primaryAccent.red)
    }
    var greenValue by remember {
        mutableFloatStateOf(currentPalette.primaryAccent.green)
    }
    var blueValue by remember {
        mutableFloatStateOf(currentPalette.primaryAccent.blue)
    }

    val previewCustomColor = remember(redValue, greenValue, blueValue) {
        Color(redValue, greenValue, blueValue)
    }

    val quickColors = listOf(
        Color(0xFFFF0033), // Red Gang
        Color(0xFF168CFF), // Neon Blue
        Color(0xFFB026FF), // Cyber Purple
        Color(0xFF00D084), // Neon Green
        Color(0xFFFF8500), // Fire Orange
        Color(0xFFFF007F), // Neon Magenta
        Color(0xFF00E5FF), // Cyber Cyan
        Color(0xFFFFD600), // Electric Yellow
        Color(0xFF76FF03), // Acid Lime
        Color(0xFFFF5252)  // Coral Red
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.2.dp, ParsTheme.palette.glowColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .testTag("theme_selector_dialog"),
            color = ParsTheme.palette.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "تم دلخواه // CUSTOM THEME",
                            color = ParsTheme.palette.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = ParsTheme.palette.glowColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Visual Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(currentPalette.background)
                        .border(1.dp, currentPalette.border, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "پیش‌نمایش استایل و رنگ انتخابی",
                                color = ParsTheme.palette.textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(currentPalette.primaryAccent.copy(alpha = 0.2f))
                                    .border(1.dp, currentPalette.glowColor, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = currentPalette.displayName,
                                    color = currentPalette.glowColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Mini Neon Button Preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                currentPalette.glowColor,
                                                currentPalette.secondaryDark,
                                                currentPalette.background
                                            )
                                        )
                                    )
                                    .border(1.5.dp, currentPalette.glowColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(currentPalette.cardBg)
                                    .border(1.2.dp, currentPalette.primaryAccent, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "PARS PING CYBERPUNK",
                                    color = currentPalette.glowColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets Title
                Text(
                    text = "پوسته‌های از پیش‌تعریف‌شده:",
                    color = ParsTheme.palette.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 5 Presets List
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ThemePresets.allPresets.forEach { preset ->
                        val isSelected = currentPalette.id == preset.id

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) preset.primaryAccent.copy(alpha = 0.15f) else ParsTheme.palette.cardBg)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) preset.glowColor else ParsTheme.palette.border,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    isCustomPickerOpen = false
                                    onSelectPreset(preset)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("theme_preset_${preset.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(preset.primaryAccent)
                                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = preset.displayName,
                                        color = ParsTheme.palette.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    if (preset.id == ThemePresets.RedGang.id) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(پیش‌فرض)",
                                            color = ParsTheme.palette.textMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "انتخاب شده",
                                        tint = preset.glowColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Background Mode Selection
                Text(
                    text = "سبک رنگ پس‌زمینه:",
                    color = ParsTheme.palette.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BackgroundMode.entries.forEach { mode ->
                        val isSelected = currentPalette.backgroundMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) currentPalette.primaryAccent.copy(alpha = 0.15f) else ParsTheme.palette.cardBg)
                                .border(
                                    1.dp,
                                    if (isSelected) currentPalette.glowColor else ParsTheme.palette.border,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onUpdateBackgroundMode(mode) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.titleFa,
                                color = if (isSelected) currentPalette.glowColor else ParsTheme.palette.textPrimary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Glow Intensity Selection
                Text(
                    text = "شدت نور نئونی و درخشش:",
                    color = ParsTheme.palette.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlowIntensity.entries.forEach { glow ->
                        val isSelected = currentPalette.glowIntensity == glow
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) currentPalette.primaryAccent.copy(alpha = 0.15f) else ParsTheme.palette.cardBg)
                                .border(
                                    1.dp,
                                    if (isSelected) currentPalette.glowColor else ParsTheme.palette.border,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onUpdateGlowIntensity(glow) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = glow.titleFa,
                                color = if (isSelected) currentPalette.glowColor else ParsTheme.palette.textPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Accent Color Toggle
                Button(
                    onClick = { isCustomPickerOpen = !isCustomPickerOpen },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ParsTheme.palette.cardBgElevated,
                        contentColor = ParsTheme.palette.textPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ParsTheme.palette.border)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = ParsTheme.palette.glowColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCustomPickerOpen) "بستن پالت سفارشی" else "انتخاب رنگ سفارشی (RGB / Accent)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Custom RGB Color Picker Section
                if (isCustomPickerOpen) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ParsTheme.palette.cardBg)
                            .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "رنگ‌های سریع:",
                            color = ParsTheme.palette.textMuted,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            quickColors.forEach { col ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        .clickable {
                                            redValue = col.red
                                            greenValue = col.green
                                            blueValue = col.blue
                                            onSelectCustomColor(col)
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sliders R, G, B
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "R", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Slider(
                                value = redValue,
                                onValueChange = { redValue = it },
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Red,
                                    activeTrackColor = Color.Red
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "G", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Slider(
                                value = greenValue,
                                onValueChange = { greenValue = it },
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Green,
                                    activeTrackColor = Color.Green
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "B", color = Color.Blue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Slider(
                                value = blueValue,
                                onValueChange = { blueValue = it },
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = Color.Blue,
                                    activeTrackColor = Color.Blue
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                onSelectCustomColor(previewCustomColor)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = previewCustomColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "اعمال رنگ سفارشی", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Restore Default Pars Ping Theme Button
                OutlinedButton(
                    onClick = onRestoreDefault,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF0033).copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF0033)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بازگردانی تم اصلی (قرمز گنگ)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
