package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ParsPingCrown
import com.example.ui.theme.ParsTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = LinearEasing)
        )
        delay(150)
        onSplashFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashScan")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ParsTheme.palette.background),
        contentAlignment = Alignment.Center
    ) {
        val themeGlow = ParsTheme.palette.glowColor
        // Subtle Cyber Texture Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Red angular grid lines
            val linePaint = themeGlow.copy(alpha = 0.08f)
            for (i in 0..12) {
                val y = h * (i / 12f)
                drawLine(
                    color = linePaint,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(w, y),
                    strokeWidth = 1f
                )
            }

            // Diagonal tactical angle
            val diagPath = Path().apply {
                moveTo(0f, h * 0.2f)
                lineTo(w, h * 0.4f)
                lineTo(w, h * 0.405f)
                lineTo(0f, h * 0.205f)
                close()
            }
            drawPath(
                path = diagPath,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        themeGlow.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                )
            )
        }

        // Main Splash Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Pars Ping Crown Logo
            ParsPingCrown(
                modifier = Modifier.size(92.dp),
                primaryColor = ParsTheme.palette.primaryAccent,
                glowColor = ParsTheme.palette.glowColor,
                secondaryColor = ParsTheme.palette.secondaryDark
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Large Wordmark
            Text(
                text = "PARS PING",
                color = ParsTheme.palette.textPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "CYBERPUNK GAMING NETWORK",
                color = ParsTheme.palette.glowColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Slogan: "FASTER • SAFER • STRONGER"
            Text(
                text = "FASTER • SAFER • STRONGER",
                color = ParsTheme.palette.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.8.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Subtle Loading Bar
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(ParsTheme.palette.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ParsTheme.palette.secondaryDark,
                                    ParsTheme.palette.primaryAccent,
                                    ParsTheme.palette.glowColor
                                )
                            )
                        )
                )
            }
        }
    }
}
