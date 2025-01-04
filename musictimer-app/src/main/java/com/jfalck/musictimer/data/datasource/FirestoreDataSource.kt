package com.jfalck.musictimer.data.datasource

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jfalck.musictimer.data.model.PurchaseModel


private const val TAG = "FirestoreDataSource"

class FirestoreDataSource {

    private val db = Firebase.firestore

    private fun savePurchase(purchase: PurchaseModel) {
        val map = hashMapOf(
            "orderId" to purchase.orderId,
            "productId" to purchase.productId,
            "purchaseTime" to purchase.purchaseTime,
            "purchaseState" to purchase.purchaseState,
            "quantity" to purchase.quantity,
            "acknowledged" to purchase.acknowledged,
        )

        db.collection("purchases").document(purchase.purchaseToken)
            .set(map)
            .addOnSuccessListener {
                Log.d(
                    TAG,
                    "DocumentSnapshot with token ${purchase.purchaseToken} successfully written!"
                )
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun savePurchases(purchases: List<PurchaseModel>) {
        purchases.forEach(::savePurchase)
    }
}
