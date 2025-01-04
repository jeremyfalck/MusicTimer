package com.jfalck.musictimer.presenter

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

class TextManager(private val appContext: Context) {

    fun getString(@StringRes stringRes: Int) = appContext.getString(stringRes)

    fun getString(@StringRes stringRes: Int, value: Int) = appContext.getString(stringRes, value)

    fun getQuantityString(@PluralsRes stringRes: Int, quantity: Int) =
        appContext.resources.getQuantityString(stringRes, quantity, quantity)

}