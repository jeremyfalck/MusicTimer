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

    fun getQuickSettingsTimeValueFlow(): Flow<Int>
    suspend fun setQuickSettingsTimeValue(timeValue: Int)
    suspend fun getQuickSettingsTimeValue(): Int

    suspend fun getTimerLaunchCount(): Int
    suspend fun setTimerLaunchCount(count: Int)

    suspend fun isPaidUserFlow(): Flow<Boolean>
    suspend fun isPaidUser(): Boolean
    suspend fun setPaidUser(isPaid: Boolean)
}