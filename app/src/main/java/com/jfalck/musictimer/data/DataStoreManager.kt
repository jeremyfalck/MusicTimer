package com.jfalck.musictimer.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jfalck.musictimer.data.DataStoreManager.Companion.DATASTORE_NAME
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class DataStoreManager(private val context: Context) {

    private val notificationIDKey = intPreferencesKey(NOTIFICATION_ID_KEY)
    private val lastTimeValueSelectedKey = intPreferencesKey(LAST_TIME_VALUE_SELECTED)

    suspend fun getNotificationId(): Int {
        val preferences = context.dataStore.data.first()
        return preferences[notificationIDKey] ?: 1
    }

    suspend fun incrementNotificationId() {
        context.dataStore.edit { preferences ->
            val currentId = preferences[notificationIDKey] ?: 1
            preferences[notificationIDKey] = currentId + 1
        }
    }

    suspend fun getLastTimeValueSelected(): Int {
        val preferences = context.dataStore.data.first()
        return preferences[lastTimeValueSelectedKey] ?: 0
    }

    suspend fun setLastTimeValueSelected(timeValue: Int) {
        context.dataStore.edit { preferences ->
            preferences[lastTimeValueSelectedKey] = timeValue
        }
    }

    companion object {
        const val DATASTORE_NAME = "MusicTimerDataStore"
        const val NOTIFICATION_ID_KEY = "notification_id"
        const val LAST_TIME_VALUE_SELECTED = "last_value_selected"
    }
}