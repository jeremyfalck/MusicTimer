package com.jfalck.musictimer.data

import com.jfalck.musictimer_common.data.CacheManager
import kotlinx.coroutines.flow.Flow

class TimeValueRepository(private val cacheManager: CacheManager): ITimeValueRepository {
    override fun getLastTimeValueSelected(): Flow<Int> =
        cacheManager.getLastTimeValueSelected()

    override suspend fun setLastTimeValueSelected(timeValue: Int) =
        cacheManager.setLastTimeValueSelected(timeValue)
}