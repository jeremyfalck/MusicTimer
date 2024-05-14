package com.jfalck.musictimer.data

import kotlinx.coroutines.flow.Flow

interface ITimeValueRepository {

    fun getLastTimeValueSelected(): Flow<Int>
    suspend fun setLastTimeValueSelected(timeValue: Int)

}