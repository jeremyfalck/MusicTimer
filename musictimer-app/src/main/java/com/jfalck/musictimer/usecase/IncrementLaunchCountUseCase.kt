package com.jfalck.musictimer.usecase

import com.jfalck.musictimer_common.data.CacheManager

class IncrementLaunchCountUseCase(private val cacheManager: CacheManager) {

    suspend operator fun invoke() {
        cacheManager.setTimerLaunchCount(cacheManager.getTimerLaunchCount() + 1)
    }

}