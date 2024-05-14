package com.jfalck.musictimer_common.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jfalck.musictimer_common.data.DataStoreManager.Companion.DATASTORE_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class DataStoreManager(private val context: Context): CacheManager {

    private val notificationIDKey = intPreferencesKey(NOTIFICATION_ID_KEY)
    private val lastTimeValueSelectedKey = intPreferencesKey(LAST_TIME_VALUE_SELECTED)
    private val devModeEnabledKey = booleanPreferencesKey(DEV_MODE_ENABLED_KEY)

    override suspend fun getNotificationId(): Int {
        val preferences = context.dataStore.data.first()
        return preferences[notificationIDKey] ?: 1
    }

    override suspend fun incrementNotificationId() {
        context.dataStore.edit { preferences ->
            val currentId = preferences[notificationIDKey] ?: 1
            preferences[notificationIDKey] = currentId + 1
        }
    }

    override fun getLastTimeValueSelected(): Flow<Int> =
        context.dataStore.data.map { preferences ->
            preferences[lastTimeValueSelectedKey] ?: 0
        }

    override suspend fun setLastTimeValueSelected(timeValue: Int) {
        context.dataStore.edit { preferences ->
            preferences[lastTimeValueSelectedKey] = timeValue
        }
    }

    override suspend fun getDevModeEnabled(): Boolean =
        context.dataStore.data.first()[devModeEnabledKey] ?: false

    override fun getDevModeEnabledFlow(): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[devModeEnabledKey] ?: false
        }

    override suspend fun setDevModeEnabled(devModeEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[devModeEnabledKey] = devModeEnabled
        }
    }

    companion object {
        const val DATASTORE_NAME = "MusicTimerDataStore"
        private const val NOTIFICATION_ID_KEY = "notification_id"
        private const val LAST_TIME_VALUE_SELECTED = "last_value_selected"
        private const val DEV_MODE_ENABLED_KEY = "dev_mode_enabled"
    }
}