package com.jfalck.musictimer.presenter.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.jfalck.musictimer.billing.BillingConnectionListener
import com.jfalck.musictimer.billing.BillingManager
import com.jfalck.musictimer.billing.PurchasesRetrievedListener
import com.jfalck.musictimer.usecase.SavePurchasesUseCase
import com.jfalck.musictimer.usecase.SetIsPaidUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val TAG = "BillingViewModel"

class BillingViewModel(
    private val billingManager: BillingManager,
    private val setIsPaidUserUseCase: SetIsPaidUserUseCase,
    private val savePurchaseUseCase: SavePurchasesUseCase
) : ViewModel() {

    private val _isBillingSystemConnected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isBillingSystemConnected: StateFlow<Boolean> = _isBillingSystemConnected

    private val _purchases: MutableStateFlow<List<Purchase>> = MutableStateFlow(listOf())
    val purchases: StateFlow<List<Purchase>> = _purchases

    private val billingConnectionListener = object : BillingConnectionListener {
        override fun onConnectionEstablished() {
            Log.d(TAG, "onConnectionEstablished()")
            viewModelScope.launch {
                _isBillingSystemConnected.emit(true)
            }
        }

        override fun onConnectionFailed() {
            Log.d(TAG, "onConnectionFailed()")
            viewModelScope.launch {
                _isBillingSystemConnected.emit(false)
            }
        }

        override fun onConnectionLost() {
            Log.d(TAG, "onConnectionLost()")
            viewModelScope.launch {
                _isBillingSystemConnected.emit(false)
            }
        }
    }

    private val purchasesRetrievedListener = object : PurchasesRetrievedListener {
        override fun onPurchasesRetrieved(purchases: List<Purchase>) {
            Log.d(TAG, purchases.toString())
            viewModelScope.launch {
                _purchases.emit(purchases)
            }
            CoroutineScope(Dispatchers.IO).launch {
                async { savePurchaseUseCase.invoke(purchases) }
                async { setIsPaidUserUseCase(purchases) }
                async {
                    purchases.forEach {
                        billingManager.acknowledgePayment(it.purchaseToken)
                    }
                }
            }
        }

    }

    fun initBilling() {
        billingManager.initBillingClient(purchasesRetrievedListener)
        billingManager.startConnection(billingConnectionListener)
    }

    fun showBillingDialog(activity: Activity) = billingManager.showBillingDialog(activity)

    fun getPurchases() = billingManager.getPurchases(purchasesRetrievedListener)
}