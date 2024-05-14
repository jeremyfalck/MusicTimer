package com.jfalck.musictimer.usecase

import com.jfalck.musictimer.data.ITimeValueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetLastTimeValueSelectedUseCase(private val timeValueRepository: ITimeValueRepository) {

    operator fun invoke(): Flow<Float> =
        timeValueRepository.getLastTimeValueSelected().map { it.toFloat() }

}