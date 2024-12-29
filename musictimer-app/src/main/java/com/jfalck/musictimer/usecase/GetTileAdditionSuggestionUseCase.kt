package com.jfalck.musictimer.usecase

import com.jfalck.musictimer_common.data.CacheManager

class GetTileAdditionSuggestionUseCase(private val cacheManager: CacheManager) {

    suspend operator fun invoke(): Boolean {
        val timerLaunchCount = cacheManager.getTimerLaunchCount()
        return timerLaunchCount == 1
    }
}