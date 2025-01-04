package com.jfalck.musictimer.usecase

import com.jfalck.musictimer_common.data.CacheManager

class SetQuickSettingsTimeValueUseCase(private val cacheManager: CacheManager) {
    suspend operator fun invoke(time: Int) =
        cacheManager.setQuickSettingsTimeValue(time)
}