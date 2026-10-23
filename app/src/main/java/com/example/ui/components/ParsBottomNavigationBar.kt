package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ParsTheme
import com.example.ui.viewmodel.MainNavTab

@Composable
fun ParsBottomNavigationBar(
    currentTab: MainNavTab,
    onTabSelected: (MainNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(ParsTheme.palette.cardBg)
            .border(1.dp, ParsTheme.palette.border, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = MainNavTab.HOME.titleFa,
                isSelected = currentTab == MainNavTab.HOME,
                onClick = { onTabSelected(MainNavTab.HOME) },
                modifier = Modifier.weight(1f)
            )

            NavItem(
                icon = Icons.Default.Dns,
                label = MainNavTab.CONFIGS.titleFa,
                isSelected = currentTab == MainNavTab.CONFIGS,
                onClick = { onTabSelected(MainNavTab.CONFIGS) },
                modifier = Modifier.weight(1f)
            )

            NavItem(
                icon = Icons.Default.NetworkCheck,
                label = MainNavTab.NETWORK.titleFa,
                isSelected = currentTab == MainNavTab.NETWORK,
                onClick = { onTabSelected(MainNavTab.NETWORK) },
                modifier = Modifier.weight(1f)
            )

            NavItem(
                icon = Icons.Default.Settings,
                label = MainNavTab.SETTINGS.titleFa,
                isSelected = currentTab == MainNavTab.SETTINGS,
                onClick = { onTabSelected(MainNavTab.SETTINGS) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = ParsTheme.palette.glowColor
    val inactiveColor = ParsTheme.palette.textMuted

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            color = if (isSelected) activeColor else inactiveColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
