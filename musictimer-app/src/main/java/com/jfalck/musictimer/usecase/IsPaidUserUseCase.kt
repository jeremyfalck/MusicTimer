package com.jfalck.musictimer.usecase

import com.jfalck.musictimer_common.data.CacheManager
import kotlinx.coroutines.flow.Flow

class IsPaidUserUseCase(private val cacheManager: CacheManager) {

    suspend operator fun invoke(): Flow<Boolean> {
        return cacheManager.isPaidUserFlow()
    }

}