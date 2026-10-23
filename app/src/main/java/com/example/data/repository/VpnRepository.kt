package com.example.data.repository

import com.example.data.local.VpnConfigDao
import com.example.data.local.VpnConfigEntity
import com.example.data.network.PingTester
import com.example.data.network.SubscriptionParser
import com.example.data.network.SubscriptionValidator
import com.example.data.network.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.Request

sealed class FetchSubscriptionResult {
    data class Success(val countAdded: Int, val totalConfigs: Int) : FetchSubscriptionResult()
    data class Error(val message: String) : FetchSubscriptionResult()
}

class VpnRepository(private val dao: VpnConfigDao) {

    val allConfigs: Flow<List<VpnConfigEntity>> = dao.getAllConfigsFlow()
    val selectedConfig: Flow<VpnConfigEntity?> = dao.getSelectedConfigFlow()

    suspend fun getSelectedConfig(): VpnConfigEntity? = dao.getSelectedConfig()

    suspend fun selectConfig(configId: Long) {
        dao.deselectAll()
        dao.selectById(configId)
    }

    suspend fun deleteConfig(config: VpnConfigEntity) {
        val wasSelected = config.isSelected
        dao.delete(config)
        if (wasSelected) {
            val remaining = dao.getSelectedConfig()
            if (remaining == null) {
                // If other configs exist, select the first available one
                val all = dao.getAllConfigsFlow().firstOrNull() ?: emptyList()
                if (all.isNotEmpty()) {
                    dao.selectById(all.first().id)
                }
            }
        }
    }

    suspend fun updateConfig(config: VpnConfigEntity) {
        dao.update(config)
    }

    suspend fun clearAllConfigs() {
        dao.clearAll()
    }

    suspend fun testPing(config: VpnConfigEntity): Long? {
        val latency = PingTester.measureTcpLatency(config.serverAddress, config.serverPort)
        dao.updatePing(config.id, latency)
        return latency
    }

    suspend fun seedInitialWireGuardConfigsIfNeeded() = withContext(Dispatchers.IO) {
        val existing = dao.getAllConfigsFlow().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val samples = SubscriptionParser.getSampleWireGuardConfigs()
            for (sample in samples) {
                dao.insert(sample)
            }
        }
    }

    suspend fun addSampleWireGuardConfigs(): Int = withContext(Dispatchers.IO) {
        val samples = SubscriptionParser.getSampleWireGuardConfigs()
        var added = 0
        for (sample in samples) {
            val existing = dao.findByRawUri(sample.rawUri)
            if (existing == null) {
                dao.insert(sample)
                added++
            }
        }
        val current = dao.getSelectedConfig()
        if (current == null) {
            val all = dao.getAllConfigsFlow().firstOrNull() ?: emptyList()
            if (all.isNotEmpty()) {
                dao.selectById(all.first().id)
            }
        }
        added
    }

    suspend fun importDirectConfig(text: String): FetchSubscriptionResult = withContext(Dispatchers.IO) {
        val parsedList = SubscriptionParser.parseSubscription(text)
        if (parsedList.isEmpty()) {
            return@withContext FetchSubscriptionResult.Error("کانفیگ WireGuard نامعتبر است یا ساختار درستی ندارد")
        }

        var addedCount = 0
        val currentSelected = dao.getSelectedConfig()

        for ((index, item) in parsedList.withIndex()) {
            val existing = dao.findByRawUri(item.rawUri)
            if (existing == null) {
                val shouldSelect = (currentSelected == null && index == 0)
                dao.insert(item.copy(isSelected = shouldSelect))
                addedCount++
            }
        }

        FetchSubscriptionResult.Success(
            countAdded = addedCount,
            totalConfigs = parsedList.size
        )
    }

    suspend fun importWireGuardConfigFile(content: String, fileName: String? = null): FetchSubscriptionResult = withContext(Dispatchers.IO) {
        val cleanContent = content.trim()
        if (cleanContent.isBlank()) {
            return@withContext FetchSubscriptionResult.Error("محتوای فایل انتخاب شده خالی است")
        }

        val baseName = fileName?.substringBeforeLast(".")?.ifBlank { null }
        val customName = if (baseName != null) "Pars Ping WireGuard ($baseName)" else null

        var wgConfig = SubscriptionParser.parseWireGuardConf(cleanContent, customName)
        if (wgConfig == null) {
            val list = SubscriptionParser.parseSubscription(cleanContent)
            if (list.isNotEmpty()) {
                wgConfig = list.first()
            }
        }

        if (wgConfig == null) {
            return@withContext FetchSubscriptionResult.Error("ساختار فایل نامعتبر است. فایل کانفیگ وایرگارد باید حاوی بخش‌های [Interface] و [Peer] باشد")
        }

        val existing = dao.findByRawUri(wgConfig.rawUri)
        val targetId = if (existing != null) {
            dao.update(wgConfig.copy(id = existing.id, isSelected = true))
            existing.id
        } else {
            dao.insert(wgConfig.copy(isSelected = true))
        }
        dao.selectById(targetId)

        FetchSubscriptionResult.Success(
            countAdded = 1,
            totalConfigs = 1
        )
    }

    suspend fun fetchAndSaveSubscription(url: String): FetchSubscriptionResult {
        return withContext(Dispatchers.IO) {
            val validation = SubscriptionValidator.validateUrl(url)
            if (validation !is ValidationResult.Valid) {
                return@withContext FetchSubscriptionResult.Error(
                    (validation as ValidationResult.Invalid).message
                )
            }

            val validUrl = validation.normalizedUrl
            val client = SubscriptionValidator.createSafeHttpClient()
            val request = Request.Builder()
                .url(validUrl)
                .header("User-Agent", "ParsPing/1.0 (Android; Mobile)")
                .get()
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext FetchSubscriptionResult.Error("خطای سرور: کد پاسخ ${response.code}")
                    }
                    val body = response.body?.string()
                    if (body.isNullOrBlank()) {
                        return@withContext FetchSubscriptionResult.Error("اطلاعات اشتراک از سرور دریافت نشد")
                    }

                    val parsedList = SubscriptionParser.parseSubscription(body)
                    if (parsedList.isEmpty()) {
                        return@withContext FetchSubscriptionResult.Error("هیچ کانفیگ معتبری در لینک اشتراک یافت نشد")
                    }

                    var addedCount = 0
                    val currentSelected = dao.getSelectedConfig()

                    for ((index, item) in parsedList.withIndex()) {
                        val existing = dao.findByRawUri(item.rawUri)
                        if (existing == null) {
                            val shouldSelect = (currentSelected == null && index == 0)
                            dao.insert(item.copy(isSelected = shouldSelect))
                            addedCount++
                        }
                    }

                    FetchSubscriptionResult.Success(
                        countAdded = addedCount,
                        totalConfigs = parsedList.size
                    )
                }
            } catch (e: Exception) {
                val errorMsg = if (e.message == SubscriptionValidator.RESTRICTED_APP_ERROR) {
                    SubscriptionValidator.RESTRICTED_APP_ERROR
                } else {
                    "خطا در برقراری ارتباط با سرور: ${e.localizedMessage ?: "عدم پاسخگویی"}"
                }
                FetchSubscriptionResult.Error(errorMsg)
            }
        }
    }
}
