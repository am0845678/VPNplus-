package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class BackgroundMode(val id: String, val titleFa: String, val color: Color, val surface: Color, val card: Color) {
    PURE_BLACK("pure_black", "مشکی عمیق (#050505)", Color(0xFF050505), Color(0xFF101116), Color(0xFF141620)),
    DARK_PANEL("dark_panel", "پنل تاریک گیمینگ", Color(0xFF0A0B10), Color(0xFF12141C), Color(0xFF181B26)),
    METALLIC("metallic", "فلزی سایبرپانک", Color(0xFF0D0E14), Color(0xFF151822), Color(0xFF1C2030))
}

enum class GlowIntensity(val id: String, val titleFa: String, val multiplier: Float) {
    LOW("low", "ملایم (۰.۵x)", 0.5f),
    MEDIUM("medium", "استاندارد (۱.۰x)", 1.0f),
    HIGH("high", "درخشان (۱.۵x)", 1.5f),
    ULTRA("ultra", "شدید (۲.۰x)", 2.0f)
}

data class ParsThemePalette(
    val id: String,
    val displayName: String,
    val primaryAccent: Color,
    val secondaryDark: Color,
    val glowColor: Color,
    val dimColor: Color,
    val background: Color = Color(0xFF050505),
    val surface: Color = Color(0xFF101116),
    val cardBg: Color = Color(0xFF141620),
    val cardBgElevated: Color = Color(0xFF1B1E2B),
    val border: Color = Color(0xFF262938),
    val textPrimary: Color = Color(0xFFFFFFFF),
    val textSecondary: Color = Color(0xFFA0A5B5),
    val textMuted: Color = Color(0xFF6E7585),
    val connectedGreen: Color = Color(0xFF00E676),
    val warningAmber: Color = Color(0xFFFFAB00),
    val errorRed: Color = Color(0xFFFF1744),
    val backgroundMode: BackgroundMode = BackgroundMode.PURE_BLACK,
    val glowIntensity: GlowIntensity = GlowIntensity.MEDIUM
)

object ThemePresets {
    // 1. قرمز گنگ (Default Pars Ping Cyberpunk: #050505 / #FF101F / #8B0015)
    val RedGang = ParsThemePalette(
        id = "red_gang",
        displayName = "قرمز گنگ (پارس پینگ)",
        primaryAccent = Color(0xFFFF101F),
        secondaryDark = Color(0xFF8B0015),
        glowColor = Color(0xFFFF2B3A),
        dimColor = Color(0xFF2E0308),
        background = Color(0xFF050505),
        surface = Color(0xFF101116),
        cardBg = Color(0xFF141620),
        cardBgElevated = Color(0xFF1B1E2B),
        border = Color(0xFF262938)
    )

    // 2. آبی نئونی
    val NeonBlue = ParsThemePalette(
        id = "neon_blue",
        displayName = "آبی نئونی",
        primaryAccent = Color(0xFF168CFF),
        secondaryDark = Color(0xFF0B4A8B),
        glowColor = Color(0xFF38B6FF),
        dimColor = Color(0xFF072140),
        background = Color(0xFF050505),
        surface = Color(0xFF101116),
        cardBg = Color(0xFF141620),
        cardBgElevated = Color(0xFF1B1E2B),
        border = Color(0xFF262938)
    )

    // 3. بنفش سایبر
    val CyberPurple = ParsThemePalette(
        id = "cyber_purple",
        displayName = "بنفش سایبر",
        primaryAccent = Color(0xFFB026FF),
        secondaryDark = Color(0xFF610B99),
        glowColor = Color(0xFFD466FF),
        dimColor = Color(0xFF29053F),
        background = Color(0xFF050505),
        surface = Color(0xFF101116),
        cardBg = Color(0xFF141620),
        cardBgElevated = Color(0xFF1B1E2B),
        border = Color(0xFF262938)
    )

    // 4. سبز نئونی
    val NeonGreen = ParsThemePalette(
        id = "neon_green",
        displayName = "سبز نئونی",
        primaryAccent = Color(0xFF00D084),
        secondaryDark = Color(0xFF006B43),
        glowColor = Color(0xFF00FF9D),
        dimColor = Color(0xFF033320),
        background = Color(0xFF050505),
        surface = Color(0xFF101116),
        cardBg = Color(0xFF141620),
        cardBgElevated = Color(0xFF1B1E2B),
        border = Color(0xFF262938)
    )

    // 5. نارنجی فایر
    val FireOrange = ParsThemePalette(
        id = "fire_orange",
        displayName = "نارنجی فایر",
        primaryAccent = Color(0xFFFF8500),
        secondaryDark = Color(0xFF8F4A00),
        glowColor = Color(0xFFFFA733),
        dimColor = Color(0xFF3F1F00),
        background = Color(0xFF050505),
        surface = Color(0xFF101116),
        cardBg = Color(0xFF141620),
        cardBgElevated = Color(0xFF1B1E2B),
        border = Color(0xFF262938)
    )

    val allPresets = listOf(RedGang, NeonBlue, CyberPurple, NeonGreen, FireOrange)

    fun createPalette(
        accentColor: Color,
        bgMode: BackgroundMode = BackgroundMode.PURE_BLACK,
        glowMode: GlowIntensity = GlowIntensity.MEDIUM,
        name: String = "رنگ سفارشی",
        id: String = "custom_${Integer.toHexString(accentColor.toArgb())}"
    ): ParsThemePalette {
        val argb = accentColor.toArgb()
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF

        val secR = (r * 0.55f).toInt().coerceIn(0, 255)
        val secG = (g * 0.55f).toInt().coerceIn(0, 255)
        val secB = (b * 0.55f).toInt().coerceIn(0, 255)
        val secondary = Color(secR, secG, secB)

        val glowR = ((r * (1.0f + 0.2f * glowMode.multiplier)).toInt()).coerceIn(0, 255)
        val glowG = ((g * (1.0f + 0.2f * glowMode.multiplier)).toInt()).coerceIn(0, 255)
        val glowB = ((b * (1.0f + 0.2f * glowMode.multiplier)).toInt()).coerceIn(0, 255)
        val glow = Color(glowR, glowG, glowB)

        val dimR = (r * 0.20f).toInt().coerceIn(0, 255)
        val dimG = (g * 0.20f).toInt().coerceIn(0, 255)
        val dimB = (b * 0.20f).toInt().coerceIn(0, 255)
        val dim = Color(dimR, dimG, dimB)

        return ParsThemePalette(
            id = id,
            displayName = name,
            primaryAccent = accentColor,
            secondaryDark = secondary,
            glowColor = glow,
            dimColor = dim,
            background = bgMode.color,
            surface = bgMode.surface,
            cardBg = bgMode.card,
            cardBgElevated = bgMode.card,
            border = Color(0xFF262938),
            backgroundMode = bgMode,
            glowIntensity = glowMode
        )
    }
}

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pars_ping_theme_prefs", Context.MODE_PRIVATE)

    private val _currentTheme = MutableStateFlow(loadInitialTheme())
    val currentTheme: StateFlow<ParsThemePalette> = _currentTheme.asStateFlow()

    private fun loadInitialTheme(): ParsThemePalette {
        val themeId = prefs.getString("theme_id", ThemePresets.RedGang.id) ?: ThemePresets.RedGang.id
        val bgModeId = prefs.getString("bg_mode", BackgroundMode.PURE_BLACK.id) ?: BackgroundMode.PURE_BLACK.id
        val bgMode = BackgroundMode.entries.find { it.id == bgModeId } ?: BackgroundMode.PURE_BLACK
        val glowId = prefs.getString("glow_intensity", GlowIntensity.MEDIUM.id) ?: GlowIntensity.MEDIUM.id
        val glowMode = GlowIntensity.entries.find { it.id == glowId } ?: GlowIntensity.MEDIUM

        val preset = ThemePresets.allPresets.find { it.id == themeId }
        if (preset != null) {
            return ThemePresets.createPalette(
                accentColor = preset.primaryAccent,
                bgMode = bgMode,
                glowMode = glowMode,
                name = preset.displayName,
                id = preset.id
            )
        }

        val customColorArgb = prefs.getInt("custom_color_argb", 0)
        if (customColorArgb != 0) {
            return ThemePresets.createPalette(
                accentColor = Color(customColorArgb),
                bgMode = bgMode,
                glowMode = glowMode,
                name = "رنگ سفارشی",
                id = themeId
            )
        }

        return ThemePresets.RedGang
    }

    fun setThemePreset(palette: ParsThemePalette, bgMode: BackgroundMode = _currentTheme.value.backgroundMode, glow: GlowIntensity = _currentTheme.value.glowIntensity) {
        val configured = ThemePresets.createPalette(
            accentColor = palette.primaryAccent,
            bgMode = bgMode,
            glowMode = glow,
            name = palette.displayName,
            id = palette.id
        )
        prefs.edit()
            .putString("theme_id", palette.id)
            .putString("bg_mode", bgMode.id)
            .putString("glow_intensity", glow.id)
            .remove("custom_color_argb")
            .apply()
        _currentTheme.value = configured
    }

    fun setCustomTheme(
        accentColor: Color,
        bgMode: BackgroundMode = _currentTheme.value.backgroundMode,
        glow: GlowIntensity = _currentTheme.value.glowIntensity
    ) {
        val configured = ThemePresets.createPalette(
            accentColor = accentColor,
            bgMode = bgMode,
            glowMode = glow,
            name = "رنگ سفارشی"
        )
        prefs.edit()
            .putString("theme_id", configured.id)
            .putString("bg_mode", bgMode.id)
            .putString("glow_intensity", glow.id)
            .putInt("custom_color_argb", accentColor.toArgb())
            .apply()
        _currentTheme.value = configured
    }

    fun updateBackgroundMode(bgMode: BackgroundMode) {
        val current = _currentTheme.value
        val updated = ThemePresets.createPalette(
            accentColor = current.primaryAccent,
            bgMode = bgMode,
            glowMode = current.glowIntensity,
            name = current.displayName,
            id = current.id
        )
        prefs.edit().putString("bg_mode", bgMode.id).apply()
        _currentTheme.value = updated
    }

    fun updateGlowIntensity(glow: GlowIntensity) {
        val current = _currentTheme.value
        val updated = ThemePresets.createPalette(
            accentColor = current.primaryAccent,
            bgMode = current.backgroundMode,
            glowMode = glow,
            name = current.displayName,
            id = current.id
        )
        prefs.edit().putString("glow_intensity", glow.id).apply()
        _currentTheme.value = updated
    }

    fun restoreDefaultTheme() {
        prefs.edit().clear().apply()
        _currentTheme.value = ThemePresets.RedGang
    }
}
