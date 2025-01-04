package com.jfalck.musictimer.usecase

import com.android.billingclient.api.Purchase
import com.jfalck.musictimer.data.datasource.FirestoreDataSource
import com.jfalck.musictimer.data.mapper.PurchaseMapper

class SavePurchasesUseCase(
    private val firestoreDataSource: FirestoreDataSource,
    private val purchaseMapper: PurchaseMapper
) {
    operator fun invoke(purchases: List<Purchase>) =
        firestoreDataSource.savePurchases(purchases.map(purchaseMapper::map))
}