package com.jfalck.musictimer

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

private const val TAG = "AdManager"

class AdManager(private val appContext: Context) {

    private var interstitialAd: InterstitialAd? = null

    fun loadInterstititalAd(onAdLoaded: () -> Unit) {
        InterstitialAd.load(
            appContext,
            BuildConfig.ADMOB_INTERSTITIAL_BANNER_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.toString())
                    interstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    this@AdManager.interstitialAd = interstitialAd
                    onAdLoaded()
                }
            })
    }

    fun showIntesistialAd(activity: Activity) =
        interstitialAd?.show(activity)
}
