package com.jfalck.musictimer.usecase

import com.jfalck.musictimer_common.data.CacheManager
import kotlinx.coroutines.flow.Flow

class GetQuickSettingsTimeValueUseCase(private val cacheManager: CacheManager) {
    operator fun invoke(): Flow<Int> =
        cacheManager.getQuickSettingsTimeValueFlow()
}