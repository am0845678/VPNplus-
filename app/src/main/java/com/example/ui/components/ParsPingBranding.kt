package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ParsTheme

@Composable
fun ParsPingCrown(
    modifier: Modifier = Modifier,
    primaryColor: Color = ParsTheme.palette.primaryAccent,
    glowColor: Color = ParsTheme.palette.glowColor,
    secondaryColor: Color = ParsTheme.palette.secondaryDark
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CrownGlow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer glow path
        val glowPath = Path().apply {
            moveTo(w * 0.10f, h * 0.82f)
            lineTo(w * 0.05f, h * 0.28f)
            lineTo(w * 0.30f, h * 0.50f)
            lineTo(w * 0.50f, h * 0.12f)
            lineTo(w * 0.70f, h * 0.50f)
            lineTo(w * 0.95f, h * 0.28f)
            lineTo(w * 0.90f, h * 0.82f)
            lineTo(w * 0.50f, h * 0.92f)
            close()
        }

        drawPath(
            path = glowPath,
            brush = Brush.radialGradient(
                colors = listOf(glowColor.copy(alpha = 0.5f * pulse), Color.Transparent),
                radius = w * 0.7f
            )
        )

        // Main Cyber Crown polygon
        val crownPath = Path().apply {
            moveTo(w * 0.12f, h * 0.80f)
            lineTo(w * 0.08f, h * 0.32f)
            lineTo(w * 0.32f, h * 0.52f)
            lineTo(w * 0.50f, h * 0.18f)
            lineTo(w * 0.68f, h * 0.52f)
            lineTo(w * 0.92f, h * 0.32f)
            lineTo(w * 0.88f, h * 0.80f)
            lineTo(w * 0.50f, h * 0.88f)
            close()
        }

        drawPath(
            path = crownPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor, secondaryColor)
            ),
            style = Fill
        )

        // Sharp neon outline
        drawPath(
            path = crownPath,
            color = glowColor,
            style = Stroke(width = 2.5f * pulse)
        )

        // Center Cyber Gem diamond
        val diamond = Path().apply {
            moveTo(w * 0.50f, h * 0.44f)
            lineTo(w * 0.58f, h * 0.56f)
            lineTo(w * 0.50f, h * 0.68f)
            lineTo(w * 0.42f, h * 0.56f)
            close()
        }

        drawPath(
            path = diamond,
            color = Color.White,
            style = Fill
        )
    }
}

@Composable
fun ParsPingLogoHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ParsPingCrown(
            modifier = Modifier.size(34.dp),
            primaryColor = ParsTheme.palette.primaryAccent,
            glowColor = ParsTheme.palette.glowColor
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "PARS PING",
                color = ParsTheme.palette.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.5.sp
            )
            Text(
                text = "CYBERPUNK GAMING VPN",
                color = ParsTheme.palette.glowColor,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
        }
    }
}
