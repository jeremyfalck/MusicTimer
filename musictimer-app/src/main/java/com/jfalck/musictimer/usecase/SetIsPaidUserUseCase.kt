package com.jfalck.musictimer.usecase

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.jfalck.musictimer.billing.Products.AD_FREE_PLAN
import com.jfalck.musictimer_common.data.CacheManager

class SetIsPaidUserUseCase(private val cacheManager: CacheManager) {
    suspend operator fun invoke(purchases: List<Purchase>) {
        val hasOneAdFreePurchaseBeenAcknowledged: Boolean =
            purchases.any { purchase ->
                purchase.purchaseState == PurchaseState.PURCHASED &&
                        purchase.products.any { it == AD_FREE_PLAN }
            }
        cacheManager.setPaidUser(hasOneAdFreePurchaseBeenAcknowledged)
    }
}