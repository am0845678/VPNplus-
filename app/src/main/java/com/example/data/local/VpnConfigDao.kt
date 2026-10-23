package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VpnConfigDao {
    @Query("SELECT * FROM vpn_configs ORDER BY isSelected DESC, id ASC")
    fun getAllConfigsFlow(): Flow<List<VpnConfigEntity>>

    @Query("SELECT * FROM vpn_configs WHERE isSelected = 1 LIMIT 1")
    fun getSelectedConfigFlow(): Flow<VpnConfigEntity?>

    @Query("SELECT * FROM vpn_configs WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedConfig(): VpnConfigEntity?

    @Query("SELECT * FROM vpn_configs WHERE rawUri = :rawUri LIMIT 1")
    suspend fun findByRawUri(rawUri: String): VpnConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: VpnConfigEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(configs: List<VpnConfigEntity>): List<Long>

    @Update
    suspend fun update(config: VpnConfigEntity)

    @Delete
    suspend fun delete(config: VpnConfigEntity)

    @Query("DELETE FROM vpn_configs")
    suspend fun clearAll()

    @Query("UPDATE vpn_configs SET isSelected = 0")
    suspend fun deselectAll()

    @Query("UPDATE vpn_configs SET isSelected = 1 WHERE id = :configId")
    suspend fun selectById(configId: Long)

    @Query("UPDATE vpn_configs SET latencyMs = :latency, lastChecked = :timestamp WHERE id = :configId")
    suspend fun updatePing(configId: Long, latency: Long?, timestamp: Long = System.currentTimeMillis())
}
