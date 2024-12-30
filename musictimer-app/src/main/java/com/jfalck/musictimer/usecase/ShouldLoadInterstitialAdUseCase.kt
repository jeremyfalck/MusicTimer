package com.jfalck.musictimer.usecase

import com.jfalck.musictimer_common.data.CacheManager

class ShouldLoadInterstitialAdUseCase(private val cacheManager: CacheManager) {

    suspend operator fun invoke(): Boolean =
        !cacheManager.getDevModeEnabled() &&
                cacheManager.getTimerLaunchCount() % 3 == 0

}