package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ParsPingApplication
import com.example.data.local.VpnConfigEntity
import com.example.data.network.PingResult
import com.example.data.network.PingTester
import com.example.data.network.SubscriptionValidator
import com.example.data.network.ValidationResult
import com.example.data.repository.FetchSubscriptionResult
import com.example.data.repository.VpnRepository
import com.example.ui.theme.BackgroundMode
import com.example.ui.theme.GlowIntensity
import com.example.ui.theme.ParsThemePalette
import com.example.ui.theme.ThemePreferences
import com.example.ui.theme.ThemePresets
import com.example.vpn.VpnConnectionInfo
import com.example.vpn.VpnManager
import com.example.vpn.VpnStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

enum class MainNavTab(val titleFa: String, val route: String) {
    HOME("خانه", "home"),
    CONFIGS("کانفیگ‌ها", "configs"),
    NETWORK("شبکه", "network"),
    SETTINGS("تنظیمات", "settings")
}

sealed class SubscriptionUiNotice {
    data class Success(val message: String) : SubscriptionUiNotice()
    data class Error(val message: String, val isRestricted: Boolean = false) : SubscriptionUiNotice()
}

data class VpnScreenUiState(
    val currentTab: MainNavTab = MainNavTab.HOME,
    val connectionInfo: VpnConnectionInfo = VpnConnectionInfo(),
    val configs: List<VpnConfigEntity> = emptyList(),
    val selectedConfig: VpnConfigEntity? = null,
    val selectedProtocolFilter: String = "ALL", // ALL, VLESS, VMESS, WIREGUARD, TROJAN
    val isAddSubscriptionOpen: Boolean = false,
    val isManualAddDialogOpen: Boolean = false,
    val isServerSelectorOpen: Boolean = false,
    val isThemeDialogOpen: Boolean = false,
    val isEditDialogOpen: Boolean = false,
    val editingConfig: VpnConfigEntity? = null,
    val isDeleteAllConfirmOpen: Boolean = false,
    val isSortedByPing: Boolean = false,
    val subscriptionUrlInput: String = "",
    val isAddingSubscription: Boolean = false,
    val subscriptionNotice: SubscriptionUiNotice? = null,
    val isAboutDialogOpen: Boolean = false,
    val isConnectionDialogOpen: Boolean = false,
    val isProtocolSelectorOpen: Boolean = false,
    val isTestingPingForId: Long? = null,
    val isPingingAll: Boolean = false,
    val toastMessage: String? = null
) {
    val filteredConfigs: List<VpnConfigEntity>
        get() = when (selectedProtocolFilter.uppercase()) {
            "ALL" -> configs
            "VLESS" -> configs.filter { it.protocol.equals("VLESS", ignoreCase = true) }
            "VMESS" -> configs.filter { it.protocol.equals("VMESS", ignoreCase = true) }
            "WIREGUARD" -> configs.filter { it.protocol.equals("WIREGUARD", ignoreCase = true) || it.protocol.equals("WG", ignoreCase = true) }
            "TROJAN" -> configs.filter { it.protocol.equals("TROJAN", ignoreCase = true) }
            else -> configs
        }
}

class VpnViewModel(
    private val repository: VpnRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(VpnScreenUiState())
    val uiState: StateFlow<VpnScreenUiState> = _uiState.asStateFlow()

    val currentTheme: StateFlow<ParsThemePalette> = themePreferences.currentTheme

    private var pingAllJob: Job? = null

    init {
        // Collect DB configs
        viewModelScope.launch {
            repository.allConfigs.collect { list ->
                val displayList = if (_uiState.value.isSortedByPing) {
                    sortConfigs(list)
                } else {
                    list
                }
                _uiState.value = _uiState.value.copy(configs = displayList)
            }
        }
        // Collect selected config
        viewModelScope.launch {
            repository.selectedConfig.collect { selected ->
                _uiState.value = _uiState.value.copy(selectedConfig = selected)
            }
        }
        // Collect VPN Connection state
        viewModelScope.launch {
            VpnManager.connectionInfo.collect { info ->
                _uiState.value = _uiState.value.copy(connectionInfo = info)
            }
        }
    }

    private fun sortConfigs(list: List<VpnConfigEntity>): List<VpnConfigEntity> {
        return list.sortedWith(
            compareBy<VpnConfigEntity> { !it.isSelected }
                .thenBy { it.latencyMs ?: Long.MAX_VALUE }
        )
    }

    // NAVIGATION TABS
    fun selectTab(tab: MainNavTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    // PROTOCOL FILTER
    fun setProtocolFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedProtocolFilter = filter)
    }

    // THEME CONTROLS («تم دلخواه»)
    fun openThemeDialog() {
        _uiState.value = _uiState.value.copy(isThemeDialogOpen = true)
    }

    fun closeThemeDialog() {
        _uiState.value = _uiState.value.copy(isThemeDialogOpen = false)
    }

    fun setThemePreset(palette: ParsThemePalette) {
        themePreferences.setThemePreset(palette)
        showToast("پوسته «${palette.displayName}» اعمال شد")
    }

    fun setCustomTheme(accentColor: Color) {
        themePreferences.setCustomTheme(accentColor)
        showToast("رنگ سفارشی اعمال شد")
    }

    fun updateBackgroundMode(bgMode: BackgroundMode) {
        themePreferences.updateBackgroundMode(bgMode)
        showToast("پس‌زمینه «${bgMode.titleFa}» اعمال شد")
    }

    fun updateGlowIntensity(glow: GlowIntensity) {
        themePreferences.updateGlowIntensity(glow)
        showToast("میزان درخشش «${glow.titleFa}» تنظیم شد")
    }

    fun restoreDefaultTheme() {
        themePreferences.restoreDefaultTheme()
        showToast("پوسته اصلی پارس پینگ (قرمز گنگ) بازیابی شد")
    }

    // MANUAL CONFIG CONTROLS
    fun openManualAddDialog() {
        _uiState.value = _uiState.value.copy(isManualAddDialogOpen = true)
    }

    fun closeManualAddDialog() {
        _uiState.value = _uiState.value.copy(isManualAddDialogOpen = false)
    }

    fun saveManualConfig(
        name: String,
        protocol: String,
        address: String,
        port: Int,
        uuidOrPassword: String,
        security: String,
        networkType: String,
        pathOrSni: String
    ) {
        viewModelScope.launch {
            val entity = VpnConfigEntity(
                name = name.ifBlank { "کانفیگ دستی $protocol" },
                protocol = protocol,
                serverAddress = address.trim(),
                serverPort = port,
                rawUri = "",
                uuidOrPassword = uuidOrPassword.trim(),
                security = security,
                networkType = networkType,
                pathOrSni = pathOrSni.trim(),
                isSupported = true,
                isSelected = (_uiState.value.configs.isEmpty())
            )
            repository.updateConfig(entity)
            closeManualAddDialog()
            showToast("کانفیگ دستی با موفقیت ذخیره شد")
        }
    }

    // ABOUT DIALOG
    fun openAboutDialog() {
        _uiState.value = _uiState.value.copy(isAboutDialogOpen = true)
    }

    fun closeAboutDialog() {
        _uiState.value = _uiState.value.copy(isAboutDialogOpen = false)
    }

    // CONNECTION DETAILS DIALOG
    fun openConnectionDialog() {
        _uiState.value = _uiState.value.copy(isConnectionDialogOpen = true)
    }

    fun closeConnectionDialog() {
        _uiState.value = _uiState.value.copy(isConnectionDialogOpen = false)
    }

    // PROTOCOL SELECTOR DIALOG
    fun openProtocolSelector() {
        _uiState.value = _uiState.value.copy(isProtocolSelectorOpen = true)
    }

    fun closeProtocolSelector() {
        _uiState.value = _uiState.value.copy(isProtocolSelectorOpen = false)
    }

    // CONFIG EDIT CONTROLS
    fun openEditConfig(config: VpnConfigEntity) {
        _uiState.value = _uiState.value.copy(
            isEditDialogOpen = true,
            editingConfig = config
        )
    }

    fun closeEditConfig() {
        _uiState.value = _uiState.value.copy(
            isEditDialogOpen = false,
            editingConfig = null
        )
    }

    fun saveEditedConfig(updated: VpnConfigEntity) {
        viewModelScope.launch {
            repository.updateConfig(updated)
            closeEditConfig()
            showToast("کانفیگ بروزرسانی شد")
        }
    }

    // DELETE ALL CONTROLS
    fun openDeleteAllConfirm() {
        _uiState.value = _uiState.value.copy(isDeleteAllConfirmOpen = true)
    }

    fun closeDeleteAllConfirm() {
        _uiState.value = _uiState.value.copy(isDeleteAllConfirmOpen = false)
    }

    fun confirmDeleteAllConfigs() {
        viewModelScope.launch {
            repository.clearAllConfigs()
            closeDeleteAllConfirm()
            showToast("تمامی کانفیگ‌ها حذف شدند")
        }
    }

    // SORT CONTROLS
    fun toggleSortByPing() {
        val newSort = !_uiState.value.isSortedByPing
        _uiState.value = _uiState.value.copy(
            isSortedByPing = newSort,
            configs = if (newSort) sortConfigs(_uiState.value.configs) else _uiState.value.configs
        )
        showToast(if (newSort) "مرتب‌سازی بر اساس کمترین پینگ فعال شد" else "مرتب‌سازی عادی")
    }

    fun onUrlInputChange(input: String) {
        _uiState.value = _uiState.value.copy(
            subscriptionUrlInput = input,
            subscriptionNotice = null
        )
    }

    fun openAddSubscription(prefilled: String? = null) {
        _uiState.value = _uiState.value.copy(
            isAddSubscriptionOpen = true,
            subscriptionUrlInput = prefilled ?: _uiState.value.subscriptionUrlInput,
            subscriptionNotice = null
        )
    }

    fun closeAddSubscription() {
        _uiState.value = _uiState.value.copy(
            isAddSubscriptionOpen = false,
            subscriptionNotice = null
        )
    }

    fun openServerSelector() {
        _uiState.value = _uiState.value.copy(isServerSelectorOpen = true)
    }

    fun closeServerSelector() {
        _uiState.value = _uiState.value.copy(isServerSelectorOpen = false)
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun handleClipboardUrl(pastedText: String?) {
        if (pastedText.isNullOrBlank()) {
            showToast("کلیپبورد خالی است")
            return
        }

        val trimmed = pastedText.trim()

        // Check if direct VLESS, VMess, Trojan, or WireGuard link
        if (trimmed.startsWith("vless://", ignoreCase = true) ||
            trimmed.startsWith("vmess://", ignoreCase = true) ||
            trimmed.startsWith("trojan://", ignoreCase = true) ||
            trimmed.startsWith("wireguard://", ignoreCase = true) ||
            trimmed.startsWith("wg://", ignoreCase = true) ||
            trimmed.contains("[Interface]", ignoreCase = true)
        ) {
            _uiState.value = _uiState.value.copy(
                isAddSubscriptionOpen = true,
                subscriptionUrlInput = trimmed,
                subscriptionNotice = null
            )
            showToast("لینک پروتکل از کلیپبورد دریافت شد")
            return
        }

        val validation = SubscriptionValidator.validateUrl(pastedText)
        when (validation) {
            is ValidationResult.Valid -> {
                _uiState.value = _uiState.value.copy(
                    isAddSubscriptionOpen = true,
                    subscriptionUrlInput = validation.normalizedUrl,
                    subscriptionNotice = null
                )
                showToast("لینک اشتراک از کلیپبورد جایگذاری شد")
            }
            is ValidationResult.Invalid -> {
                _uiState.value = _uiState.value.copy(
                    isAddSubscriptionOpen = true,
                    subscriptionUrlInput = pastedText,
                    subscriptionNotice = SubscriptionUiNotice.Error(
                        message = validation.message,
                        isRestricted = (validation.message == SubscriptionValidator.RESTRICTED_APP_ERROR)
                    )
                )
            }
        }
    }

    fun submitSubscription() {
        val input = _uiState.value.subscriptionUrlInput.trim()
        if (input.isBlank()) {
            _uiState.value = _uiState.value.copy(
                subscriptionNotice = SubscriptionUiNotice.Error("لطفاً لینک کانفیگ یا اشتراک را وارد کنید")
            )
            return
        }

        // Direct config text or URI check (WireGuard, VMess, VLESS, Trojan, SS)
        val isDirectConfig = input.contains("[Interface]", ignoreCase = true) ||
                input.startsWith("wireguard://", ignoreCase = true) ||
                input.startsWith("wg://", ignoreCase = true) ||
                input.startsWith("vmess://", ignoreCase = true) ||
                input.startsWith("vless://", ignoreCase = true) ||
                input.startsWith("trojan://", ignoreCase = true) ||
                input.startsWith("ss://", ignoreCase = true)

        if (isDirectConfig) {
            _uiState.value = _uiState.value.copy(isAddingSubscription = true, subscriptionNotice = null)
            viewModelScope.launch {
                val result = repository.importDirectConfig(input)
                when (result) {
                    is FetchSubscriptionResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isAddingSubscription = false,
                            subscriptionUrlInput = "",
                            subscriptionNotice = SubscriptionUiNotice.Success(
                                "${result.countAdded} کانفیگ با موفقیت اضافه شد"
                            )
                        )
                        showToast("کانفیگ اضافه شد")
                    }
                    is FetchSubscriptionResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isAddingSubscription = false,
                            subscriptionNotice = SubscriptionUiNotice.Error(result.message)
                        )
                    }
                }
            }
            return
        }

        // Validate domain structure for HTTP subscription URLs
        val validation = SubscriptionValidator.validateUrl(input)
        if (validation is ValidationResult.Invalid) {
            _uiState.value = _uiState.value.copy(
                subscriptionNotice = SubscriptionUiNotice.Error(
                    message = validation.message,
                    isRestricted = (validation.message == SubscriptionValidator.RESTRICTED_APP_ERROR)
                )
            )
            return
        }

        val validUrl = (validation as ValidationResult.Valid).normalizedUrl
        _uiState.value = _uiState.value.copy(isAddingSubscription = true, subscriptionNotice = null)

        viewModelScope.launch {
            val result = repository.fetchAndSaveSubscription(validUrl)
            when (result) {
                is FetchSubscriptionResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isAddingSubscription = false,
                        subscriptionUrlInput = "",
                        subscriptionNotice = SubscriptionUiNotice.Success(
                            "${result.countAdded} کانفیگ جدید اضافه شد (مجموع: ${result.totalConfigs})"
                        )
                    )
                }
                is FetchSubscriptionResult.Error -> {
                    val isRestricted = (result.message == SubscriptionValidator.RESTRICTED_APP_ERROR)
                    _uiState.value = _uiState.value.copy(
                        isAddingSubscription = false,
                        subscriptionNotice = SubscriptionUiNotice.Error(
                            message = result.message,
                            isRestricted = isRestricted
                        )
                    )
                }
            }
        }
    }

    fun importConfigFileFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                var fileName: String? = null
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIdx)
                    }
                }

                val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                }

                if (content.isNullOrBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isAddSubscriptionOpen = true,
                        subscriptionNotice = SubscriptionUiNotice.Error("فایل انتخاب شده خالی است")
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isAddSubscriptionOpen = true,
                    isAddingSubscription = true,
                    subscriptionNotice = null
                )
                val result = repository.importWireGuardConfigFile(content, fileName)
                when (result) {
                    is FetchSubscriptionResult.Success -> {
                        val nameShown = fileName ?: "WireGuard"
                        _uiState.value = _uiState.value.copy(
                            isAddingSubscription = false,
                            subscriptionUrlInput = "",
                            subscriptionNotice = SubscriptionUiNotice.Success(
                                "کانفیگ ($nameShown) با موفقیت اضافه و انتخاب شد"
                            )
                        )
                        showToast("کانفیگ با موفقیت اضافه شد")
                    }
                    is FetchSubscriptionResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isAddingSubscription = false,
                            subscriptionNotice = SubscriptionUiNotice.Error(result.message)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAddSubscriptionOpen = true,
                    isAddingSubscription = false,
                    subscriptionNotice = SubscriptionUiNotice.Error(
                        "خطا در خواندن فایل: ${e.localizedMessage ?: "عدم دسترسی به فایل"}"
                    )
                )
            }
        }
    }

    fun selectConfig(configId: Long) {
        viewModelScope.launch {
            repository.selectConfig(configId)
            _uiState.value = _uiState.value.copy(isServerSelectorOpen = false)
            showToast("سرور انتخاب شد")
        }
    }

    fun deleteConfig(config: VpnConfigEntity) {
        viewModelScope.launch {
            repository.deleteConfig(config)
            showToast("کانفیگ حذف شد")
        }
    }

    fun testPingForConfig(config: VpnConfigEntity) {
        _uiState.value = _uiState.value.copy(isTestingPingForId = config.id)
        viewModelScope.launch {
            val res = PingTester.testTcpEndpoint(config.serverAddress, config.serverPort)
            repository.testPing(config)
            _uiState.value = _uiState.value.copy(isTestingPingForId = null)

            when (res) {
                is PingResult.Success -> {
                    showToast("پینگ ${config.name}: ${res.latencyMs} ms")
                }
                is PingResult.Timeout -> {
                    showToast("تایم‌اوت: سرور پاسخی نداد")
                }
                is PingResult.ConnectionRefused -> {
                    showToast("پورت سرور بسته است (Connection Refused)")
                }
                is PingResult.DnsError -> {
                    showToast("خطای DNS سرور")
                }
                is PingResult.Error -> {
                    showToast(res.message)
                }
            }
        }
    }

    fun cancelPingAll() {
        pingAllJob?.cancel()
        _uiState.value = _uiState.value.copy(isPingingAll = false, isTestingPingForId = null)
        showToast("تست پینگ همه لغو شد")
    }

    fun pingAllConfigs() {
        val currentConfigs = _uiState.value.configs
        if (currentConfigs.isEmpty()) {
            showToast("هیچ کانفیگی برای تست پینگ وجود ندارد")
            return
        }
        if (_uiState.value.isPingingAll) return

        _uiState.value = _uiState.value.copy(isPingingAll = true)
        pingAllJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val semaphore = Semaphore(4)
                currentConfigs.map { cfg ->
                    async {
                        semaphore.withPermit {
                            repository.testPing(cfg)
                        }
                    }
                }.awaitAll()
                showToast("تست پینگ سرورها تکمیل شد")
            } catch (_: Exception) {
            } finally {
                _uiState.value = _uiState.value.copy(
                    isPingingAll = false,
                    isTestingPingForId = null
                )
            }
        }
    }

    fun addSampleWireGuardConfigs() {
        viewModelScope.launch {
            val count = repository.addSampleWireGuardConfigs()
            showToast("$count کانفیگ نمونه WireGuard اضافه شد")
        }
    }

    fun toggleVpn(context: Context, onPrepareNeeded: (Intent) -> Unit) {
        val currentStatus = _uiState.value.connectionInfo.status

        if (currentStatus == VpnStatus.CONNECTED || currentStatus == VpnStatus.CONNECTING) {
            VpnManager.stopVpn(context)
            return
        }

        val selected = _uiState.value.selectedConfig
        if (selected == null) {
            showToast("ابتدا یک سرور انتخاب کنید یا کانفیگ جدید اضافه کنید")
            _uiState.value = _uiState.value.copy(isAddSubscriptionOpen = true)
            return
        }

        // Check Android VpnService.prepare permission
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent != null) {
            onPrepareNeeded(prepareIntent)
        } else {
            VpnManager.startVpn(context, selected)
        }
    }

    fun onVpnPermissionGranted(context: Context) {
        val selected = _uiState.value.selectedConfig
        if (selected != null) {
            VpnManager.startVpn(context, selected)
        }
    }

    private fun showToast(msg: String) {
        _uiState.value = _uiState.value.copy(toastMessage = msg)
    }

    class Factory(
        private val repository: VpnRepository,
        private val themePreferences: ThemePreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VpnViewModel(repository, themePreferences) as T
        }
    }
}
