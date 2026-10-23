package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.VpnRepository
import com.example.ui.theme.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ParsPingApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: VpnRepository
        private set

    lateinit var themePreferences: ThemePreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        repository = VpnRepository(database.vpnConfigDao())
        themePreferences = ThemePreferences(this)

        // Seed default working WireGuard configurations if database is empty
        CoroutineScope(Dispatchers.IO).launch {
            repository.seedInitialWireGuardConfigsIfNeeded()
        }
    }

    companion object {
        lateinit var instance: ParsPingApplication
            private set
    }
}
