package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddSubscriptionSheet
import com.example.ui.components.ConnectButton
import com.example.ui.components.DeleteAllConfirmDialog
import com.example.ui.components.DiagnosticsCard
import com.example.ui.components.EditConfigDialog
import com.example.ui.components.HeaderBar
import com.example.ui.components.ManualConfigDialog
import com.example.ui.components.ParsBottomNavigationBar
import com.example.ui.components.ServerCard
import com.example.ui.components.ServerSelectorSheet
import com.example.ui.components.StatusBadge
import com.example.ui.components.ThemeSelectorDialog
import com.example.ui.components.TrafficMetricsCard
import com.example.ui.theme.ParsTheme
import com.example.ui.viewmodel.MainNavTab
import com.example.ui.viewmodel.VpnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainVpnScreen(
    viewModel: VpnViewModel,
    onPrepareVpn: (android.content.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSplash by remember { mutableStateOf(true) }

    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importConfigFileFromUri(context, uri)
        }
    }

    // Handle toast messages
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // BackHandler for dialogs and bottom sheets
    val hasOpenOverlay = uiState.isAddSubscriptionOpen ||
            uiState.isServerSelectorOpen ||
            uiState.isThemeDialogOpen ||
            uiState.isEditDialogOpen ||
            uiState.isDeleteAllConfirmOpen ||
            uiState.isManualAddDialogOpen ||
            uiState.isAboutDialogOpen ||
            uiState.isConnectionDialogOpen ||
            uiState.isProtocolSelectorOpen ||
            uiState.currentTab != MainNavTab.HOME

    BackHandler(enabled = hasOpenOverlay) {
        when {
            uiState.isAboutDialogOpen -> viewModel.closeAboutDialog()
            uiState.isConnectionDialogOpen -> viewModel.closeConnectionDialog()
            uiState.isProtocolSelectorOpen -> viewModel.closeProtocolSelector()
            uiState.isDeleteAllConfirmOpen -> viewModel.closeDeleteAllConfirm()
            uiState.isEditDialogOpen -> viewModel.closeEditConfig()
            uiState.isThemeDialogOpen -> viewModel.closeThemeDialog()
            uiState.isManualAddDialogOpen -> viewModel.closeManualAddDialog()
            uiState.isAddSubscriptionOpen -> viewModel.closeAddSubscription()
            uiState.isServerSelectorOpen -> viewModel.closeServerSelector()
            uiState.currentTab != MainNavTab.HOME -> viewModel.selectTab(MainNavTab.HOME)
        }
    }

    // Force RTL layout for Persian UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (showSplash) {
            SplashScreen(
                onSplashFinished = { showSplash = false }
            )
        } else {
            Scaffold(
                modifier = modifier
                    .fillMaxSize()
                    .background(ParsTheme.palette.background),
                containerColor = ParsTheme.palette.background,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    ParsBottomNavigationBar(
                        currentTab = uiState.currentTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ParsTheme.palette.background,
                                    ParsTheme.palette.surface,
                                    ParsTheme.palette.dimColor,
                                    ParsTheme.palette.background
                                )
                            )
                        )
                ) {
                    when (uiState.currentTab) {
                        MainNavTab.HOME -> {
                            HomeScreenContent(
                                viewModel = viewModel,
                                uiState = uiState,
                                onPrepareVpn = onPrepareVpn,
                                onImportFileClick = {
                                    try {
                                        filePickerLauncher.launch(arrayOf("*/*"))
                                    } catch (_: Exception) {}
                                },
                                onClipboardClick = {
                                    val clipText = clipboardManager.getText()?.text
                                    viewModel.handleClipboardUrl(clipText)
                                },
                                onOpenConnectionDialog = { viewModel.openConnectionDialog() },
                                onOpenProtocolSelector = { viewModel.openProtocolSelector() }
                            )
                        }
                        MainNavTab.CONFIGS -> {
                            ConfigsScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onImportFileClick = {
                                    try {
                                        filePickerLauncher.launch(arrayOf("*/*"))
                                    } catch (_: Exception) {}
                                },
                                onConnectConfig = {
                                    viewModel.toggleVpn(context) { intent ->
                                        onPrepareVpn(intent)
                                    }
                                }
                            )
                        }
                        MainNavTab.NETWORK -> {
                            NetworkScreen(
                                viewModel = viewModel,
                                uiState = uiState
                            )
                        }
                        MainNavTab.SETTINGS -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                onOpenAbout = { viewModel.openAboutDialog() }
                            )
                        }
                    }

                    // Add Subscription Bottom Sheet
                    if (uiState.isAddSubscriptionOpen) {
                        AddSubscriptionSheet(
                            urlInput = uiState.subscriptionUrlInput,
                            isLoading = uiState.isAddingSubscription,
                            notice = uiState.subscriptionNotice,
                            configs = uiState.configs,
                            onUrlChange = { viewModel.onUrlInputChange(it) },
                            onSubmit = { viewModel.submitSubscription() },
                            onPasteClipboard = {
                                val text = clipboardManager.getText()?.text
                                if (!text.isNullOrBlank()) {
                                    viewModel.onUrlInputChange(text)
                                } else {
                                    Toast.makeText(context, "کلیپبورد خالی است", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onSelectConfig = { viewModel.selectConfig(it) },
                            onDeleteConfig = { viewModel.deleteConfig(it) },
                            onDismiss = { viewModel.closeAddSubscription() },
                            sheetState = addSheetState,
                            onAddSampleWireGuard = { viewModel.addSampleWireGuardConfigs() },
                            onImportConfigFile = { uri -> viewModel.importConfigFileFromUri(context, uri) }
                        )
                    }

                    // Server Selector Bottom Sheet
                    if (uiState.isServerSelectorOpen) {
                        ServerSelectorSheet(
                            configs = uiState.configs,
                            selectedConfig = uiState.selectedConfig,
                            isTestingPingForId = uiState.isTestingPingForId,
                            isPingingAll = uiState.isPingingAll,
                            isSortedByPing = uiState.isSortedByPing,
                            onSelectConfig = { viewModel.selectConfig(it) },
                            onTestPing = { viewModel.testPingForConfig(it) },
                            onPingAll = { viewModel.pingAllConfigs() },
                            onCancelPingAll = { viewModel.cancelPingAll() },
                            onToggleSort = { viewModel.toggleSortByPing() },
                            onEditConfig = { config -> viewModel.openEditConfig(config) },
                            onDeleteConfig = { viewModel.deleteConfig(it) },
                            onDeleteAllClick = { viewModel.openDeleteAllConfirm() },
                            onOpenAddSubscription = {
                                viewModel.closeServerSelector()
                                viewModel.openAddSubscription()
                            },
                            onImportFileClick = {
                                try {
                                    filePickerLauncher.launch(arrayOf("*/*"))
                                } catch (_: Exception) {}
                            },
                            onDismiss = { viewModel.closeServerSelector() },
                            sheetState = selectorSheetState
                        )
                    }

                    // Theme Customization Dialog ("تم دلخواه")
                    if (uiState.isThemeDialogOpen) {
                        ThemeSelectorDialog(
                            currentPalette = currentTheme,
                            onSelectPreset = { preset -> viewModel.setThemePreset(preset) },
                            onSelectCustomColor = { color -> viewModel.setCustomTheme(color) },
                            onUpdateBackgroundMode = { bg -> viewModel.updateBackgroundMode(bg) },
                            onUpdateGlowIntensity = { glow -> viewModel.updateGlowIntensity(glow) },
                            onRestoreDefault = { viewModel.restoreDefaultTheme() },
                            onDismiss = { viewModel.closeThemeDialog() }
                        )
                    }

                    // Manual Config Dialog
                    if (uiState.isManualAddDialogOpen) {
                        ManualConfigDialog(
                            onSave = { name, proto, addr, port, uuid, sec, net, sni ->
                                viewModel.saveManualConfig(name, proto, addr, port, uuid, sec, net, sni)
                            },
                            onDismiss = { viewModel.closeManualAddDialog() }
                        )
                    }

                    // Edit Configuration Dialog
                    if (uiState.isEditDialogOpen && uiState.editingConfig != null) {
                        EditConfigDialog(
                            config = uiState.editingConfig!!,
                            onSave = { updated -> viewModel.saveEditedConfig(updated) },
                            onDismiss = { viewModel.closeEditConfig() }
                        )
                    }

                    // Delete All Confirmation Dialog
                    if (uiState.isDeleteAllConfirmOpen) {
                        DeleteAllConfirmDialog(
                            count = uiState.configs.size,
                            onConfirm = { viewModel.confirmDeleteAllConfigs() },
                            onDismiss = { viewModel.closeDeleteAllConfirm() }
                        )
                    }

                    // Connection Progress / Status Dialog
                    if (uiState.isConnectionDialogOpen) {
                        ConnectionScreen(
                            connectionInfo = uiState.connectionInfo,
                            onToggleVpn = {
                                viewModel.toggleVpn(context) { intent ->
                                    onPrepareVpn(intent)
                                }
                            },
                            onDismiss = { viewModel.closeConnectionDialog() }
                        )
                    }

                    // Protocol Selection Dialog
                    if (uiState.isProtocolSelectorOpen) {
                        ProtocolSelectionDialog(
                            currentProtocol = uiState.selectedConfig?.protocol ?: "VLESS",
                            onProtocolSelected = { proto ->
                                viewModel.setProtocolFilter(proto)
                                viewModel.closeProtocolSelector()
                            },
                            onDismiss = { viewModel.closeProtocolSelector() }
                        )
                    }

                    // About Pars Ping Dialog
                    if (uiState.isAboutDialogOpen) {
                        AboutScreen(
                            onDismiss = { viewModel.closeAboutDialog() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreenContent(
    viewModel: VpnViewModel,
    uiState: com.example.ui.viewmodel.VpnScreenUiState,
    onPrepareVpn: (android.content.Intent) -> Unit,
    onImportFileClick: () -> Unit,
    onClipboardClick: () -> Unit,
    onOpenConnectionDialog: () -> Unit,
    onOpenProtocolSelector: () -> Unit
) {
    val context = LocalContext.current
    val selected = uiState.selectedConfig

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Bar (PARS PING crown logo, animated energy line, +, Clipboard, "تم دلخواه")
        HeaderBar(
            vpnStatus = uiState.connectionInfo.status,
            onAddClick = { viewModel.openAddSubscription() },
            onImportFileClick = onImportFileClick,
            onClipboardClick = onClipboardClick,
            onThemeClick = { viewModel.openThemeDialog() }
        )

        // Tactical Protocol & Routing Badge Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge()

            // Protocol Selector Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ParsTheme.palette.cardBg)
                    .border(1.dp, ParsTheme.palette.glowColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenProtocolSelector)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = ParsTheme.palette.glowColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = selected?.protocol?.uppercase() ?: "VLESS",
                        color = ParsTheme.palette.glowColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Sophisticated Cyberpunk Connect Button with Dynamic Ring & Timer
        ConnectButton(
            status = uiState.connectionInfo.status,
            formattedDuration = uiState.connectionInfo.formattedDuration,
            onClick = {
                viewModel.toggleVpn(context) { intent ->
                    onPrepareVpn(intent)
                }
            }
        )

        // Real-time Traffic metrics (Download, Upload, Core Protocol)
        TrafficMetricsCard(
            connectionInfo = uiState.connectionInfo,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Real Connection & Core Diagnostics Card (tap to open ConnectionScreen dialog)
        Box(modifier = Modifier.clickable(onClick = onOpenConnectionDialog)) {
            DiagnosticsCard(
                connectionInfo = uiState.connectionInfo,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Current Server Card with measured latency & ping test
        ServerCard(
            selectedConfig = uiState.selectedConfig,
            isTestingPing = (uiState.isTestingPingForId == uiState.selectedConfig?.id),
            onCardClick = { viewModel.openServerSelector() },
            onTestPingClick = {
                uiState.selectedConfig?.let { cfg ->
                    viewModel.testPingForConfig(cfg)
                }
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun ProtocolSelectionDialog(
    currentProtocol: String,
    onProtocolSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, ParsTheme.palette.glowColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = ParsTheme.palette.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = ParsTheme.palette.glowColor
                        )
                    }
                    Text(
                        text = "پروتکل‌های اجرایی پارس پینگ",
                        color = ParsTheme.palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                ProtocolSelectionScreen(
                    currentProtocol = currentProtocol,
                    onProtocolSelected = onProtocolSelected
                )
            }
        }
    }
}
