package com.jfalck.musictimer.presenter.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jfalck.musictimer.usecase.IsPaidUserUseCase
import com.jfalck.musictimer.usecase.ShouldLoadInterstitialAdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "AdsViewModel"

class AdsViewModel(
    private val shouldShowInterstitialAdUseCase: ShouldLoadInterstitialAdUseCase,
    private val isPaidUserUseCase: IsPaidUserUseCase,
) : ViewModel() {

    private val _isPaidUser: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isPaidUser: StateFlow<Boolean> = _isPaidUser

    private val _shouldLoadInterstitialAd: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val shouldLoadInterstitialAd: StateFlow<Boolean> = _shouldLoadInterstitialAd

    private val _shouldShowInterstitialAd: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val shouldShowInterstitialAd: StateFlow<Boolean> = _shouldShowInterstitialAd

    var isInterstitialAdLoaded: Boolean = false

    init {
        viewModelScope.launch {
            isPaidUserUseCase().collect {
                Log.d(TAG, "isPaidUser: $it")
                _isPaidUser.emit(it)
            }
        }
    }


    fun updateAdState() {
        manageInterstitialAd()
    }

    private fun manageInterstitialAd() {
        viewModelScope.launch {
            isPaidUser.collectLatest { isPaidUser ->
                Log.d(TAG, "is Paid User: $isPaidUser")
                if (isPaidUser) {
                    _shouldLoadInterstitialAd.value = false
                    _shouldShowInterstitialAd.value = false
                } else {
                    val shouldLoadAd = !isInterstitialAdLoaded
                    Log.d(TAG, "should load ad: $shouldLoadAd")
                    _shouldLoadInterstitialAd.value = shouldLoadAd
                    val shouldShowAd = shouldShowInterstitialAdUseCase()
                    Log.d(TAG, "should show ad: $shouldShowAd")
                    _shouldShowInterstitialAd.value = shouldShowAd
                }
            }

        }
    }
}