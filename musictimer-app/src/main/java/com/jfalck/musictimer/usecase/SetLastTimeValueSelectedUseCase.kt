package com.jfalck.musictimer.usecase

import com.jfalck.musictimer.data.ITimeValueRepository

class SetLastTimeValueSelectedUseCase(private val timeValueRepository: ITimeValueRepository) {

    suspend operator fun invoke(value: Float) =
        timeValueRepository.setLastTimeValueSelected(value.toInt())

}