package com.jfalck.musictimer_common.data

import kotlinx.coroutines.flow.Flow

interface CacheManager {

    suspend fun getNotificationId(): Int
    suspend fun incrementNotificationId()
    fun getLastTimeValueSelected(): Flow<Int>
    suspend fun setLastTimeValueSelected(timeValue: Int)
    suspend fun getDevModeEnabled(): Boolean
    fun getDevModeEnabledFlow(): Flow<Boolean>
    suspend fun setDevModeEnabled(devModeEnabled: Boolean)
}