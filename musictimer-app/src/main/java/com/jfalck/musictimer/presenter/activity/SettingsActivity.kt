package com.jfalck.musictimer.presenter.activity

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.ui.component.CenterAlignedTopAppBar
import com.jfalck.musictimer.presenter.ui.component.TimeSelectionSlider
import com.jfalck.musictimer.presenter.ui.screen.settings.SettingsScreen
import com.jfalck.musictimer.presenter.ui.theme.MusicTimerTheme
import com.jfalck.musictimer.presenter.viewmodel.TimerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val TAG = "SettingsActivity"

class SettingsActivity : ComponentActivity() {

    private val timerViewModel: TimerViewModel by viewModel()

    private var showQuickTimeSlider = mutableStateOf(false)

    private val purchasesUpdatedListener: PurchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            Log.d(TAG, "billingResult: $billingResult")
            Log.d(TAG, "purchases: $purchases")
        }

    private var billingClient: BillingClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun showBillingDialog() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "billingResult is OK")

                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("ad_free_plan")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    )
                    Log.d(TAG, "productList loaded: $productList")
                    val params = QueryProductDetailsParams.newBuilder()
                    params.setProductList(productList)

                    CoroutineScope(Dispatchers.IO).launch {
                        val productDetailResult: ProductDetailsResult? =
                            billingClient?.queryProductDetails(params.build())
                        Log.d(TAG, "products found: $productDetailResult")

                        productDetailResult?.productDetailsList?.firstOrNull()
                            ?.let { productDetails ->
                                val productDetailsParamsList = listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .build()
                                )

                                val billingFlowParams = BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(productDetailsParamsList)
                                    .build()

                                CoroutineScope(Dispatchers.Main).launch {
                                    billingClient?.launchBillingFlow(
                                        this@SettingsActivity,
                                        billingFlowParams
                                    )
                                }
                            }
                    }

                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing Service is disconnected")
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })

    }

    private fun initView() {

        val vibrator = getSystemService(Vibrator::class.java)

        setContent {

            val quickSettingsTimeValue =
                timerViewModel.quickSettingsTimeValueSelected.collectAsState()
            val showDialog by remember { showQuickTimeSlider }

            SettingsScreen(
                topAppBarTitle = getString(R.string.settings),
                quickTimeSettingTitle = getString(R.string.quick_time_settings),
                quickTimeSettingDescription = getString(R.string.quick_time_settings_desc),
                onQuickTimeClicked = { showQuickTimeSlider.value = !showDialog },
                quickTimeSettingsValue = quickSettingsTimeValue.value,
                showTimeQuickSettingDialog = showDialog,
                onQuickTimeValueSelected = { value ->
                    CoroutineScope(Dispatchers.IO).launch {
                        timerViewModel.setQuickSettingsTimeValue(value.toInt())
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    }
                },
                onRemoveAdsClick = { showBillingDialog() }
            )
        }
    }
}

