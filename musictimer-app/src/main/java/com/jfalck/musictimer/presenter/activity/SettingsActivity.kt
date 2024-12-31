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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.jfalck.musictimer.presenter.ui.theme.MusicTimerTheme
import com.jfalck.musictimer_common.data.CacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


private const val TAG = "SettingsActivity"

class SettingsActivity : ComponentActivity() {

    private val dataStoreManager: CacheManager by inject()

    private var showQuickTimeSlider = mutableStateOf(false)

    val purchasesUpdatedListener: PurchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
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
            // Configure other settings.
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "billingResult is OK")

                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("product_id_example")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
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

            val isDebugEnabled = dataStoreManager.getDevModeEnabledFlow().collectAsState(false)
            val quickSettingsTimeValue =
                dataStoreManager.getQuickSettingsTimeValueFlow().collectAsState(0)
            val showDialog by remember { showQuickTimeSlider }

            SettingsActivityContent(
                topAppBarTitle = getString(R.string.settings),
                devModeTitle = getString(R.string.dev_mode),
                onDevModeChanged = { isChecked ->
                    CoroutineScope(Dispatchers.IO).launch {
                        dataStoreManager.setDevModeEnabled(isChecked)
                    }
                },
                isDebugEnabled = isDebugEnabled.value,
                quickTimeSettingTitle = getString(R.string.quick_time_settings),
                quickTimeSettingDescription = getString(R.string.quick_time_settings_desc),
                onQuickTimeClicked = { showQuickTimeSlider.value = !showDialog },
                quickTimeSettingsValue = quickSettingsTimeValue.value,
                showTimeQuickSettingDialog = showDialog,
                onQuickTimeValueSelected = { value ->
                    CoroutineScope(Dispatchers.IO).launch {
                        dataStoreManager.setQuickSettingsTimeValue(value.toInt())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsActivityContent(
    topAppBarTitle: String = "Preferences",
    devModeTitle: String = "Dev Mode Enabled",
    onDevModeChanged: (Boolean) -> Unit = {},
    isDebugEnabled: Boolean = false,
    quickTimeSettingTitle: String = "Quick Time Settings",
    quickTimeSettingDescription: String = "Set the default time for the quick time settings",
    onQuickTimeClicked: () -> Unit = {},
    quickTimeSettingsValue: Int = 20,
    showTimeQuickSettingDialog: Boolean = false,
    onQuickTimeValueSelected: (Float) -> Unit = {},
    onRemoveAdsClick: () -> Unit = {}
) {
    MusicTimerTheme(
        darkTheme = isSystemInDarkTheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {

            val scrollBehavior =
                TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

                topBar = {
                    CenterAlignedTopAppBar(
                        title = topAppBarTitle,
                        showSettingsButton = false,
                        scrollBehavior = scrollBehavior
                    )
                },
            ) { innerPadding ->
                SettingsActivitySubContent(
                    innerPadding,
                    devModeTitle,
                    onDevModeChanged,
                    isDebugEnabled,
                    quickTimeSettingTitle,
                    quickTimeSettingDescription,
                    onQuickTimeClicked,
                    quickTimeSettingsValue,
                    showTimeQuickSettingDialog,
                    onQuickTimeValueSelected,
                    onRemoveAdsClick
                )
            }
        }
    }
}


@Composable
private fun SettingsActivitySubContent(
    innerPadding: PaddingValues,
    devModeTitle: String,
    onDevModeChanged: (Boolean) -> Unit,
    isDebugEnabled: Boolean,
    quickTimeSettingTitle: String,
    quickTimeSettingDescription: String,
    onQuickTimeClicked: () -> Unit,
    quickSettingsTimeValue: Int,
    showTimeQuickSettingDialog: Boolean = false,
    onQuickTimeValueSelected: (Float) -> Unit = {},
    onRemoveAdsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(innerPadding)
    ) {
        DevModeOption(
            devModeTitle,
            onDevModeChanged,
            isDebugEnabled
        )
        QuickSettingTimeValueOption(
            quickTimeSettingTitle,
            quickTimeSettingDescription,
            onClick = onQuickTimeClicked,
            value = quickSettingsTimeValue
        )
        if (showTimeQuickSettingDialog) Slider(
            modifier = Modifier.padding(16.dp),
            value = quickSettingsTimeValue.toFloat(),
            valueRange = 1F..90F,
            onValueChange = onQuickTimeValueSelected,
            steps = 91,
            enabled = true
        )
        RemoveAds(onClick = onRemoveAdsClick)
    }
}

@Composable
fun DevModeOption(
    title: String,
    onDevModeChanged: (Boolean) -> Unit,
    isDebugEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.wrapContentSize(),
            color = MaterialTheme.colorScheme.primary,
            fontStyle = MaterialTheme.typography.titleMedium.fontStyle,
            fontSize = MaterialTheme.typography.titleMedium.fontSize
        )
        Switch(
            modifier = Modifier.wrapContentSize(),
            checked = isDebugEnabled,
            onCheckedChange = onDevModeChanged
        )
    }
}

@Composable
fun RemoveAds(onClick: () -> Unit = { }) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = CenterVertically,
    ) {
        Text(
            text = "Remove ads",
            modifier = Modifier.wrapContentSize(),
            color = MaterialTheme.colorScheme.primary,
            fontStyle = MaterialTheme.typography.titleMedium.fontStyle,
            fontSize = MaterialTheme.typography.titleMedium.fontSize
        )
    }
}

@Composable
fun QuickSettingTimeValueOption(
    quickTimeSettingTitle: String,
    quickTimeSettingDescription: String,
    onClick: () -> Unit,
    value: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
        ) {
            Text(
                text = quickTimeSettingTitle,
                modifier = Modifier.wrapContentSize(),
                color = MaterialTheme.colorScheme.primary,
                fontStyle = MaterialTheme.typography.titleMedium.fontStyle,
                fontSize = MaterialTheme.typography.titleMedium.fontSize
            )
            Text(
                modifier = Modifier.wrapContentSize(),
                text = "$value min",
                color = MaterialTheme.colorScheme.primary,
                fontStyle = MaterialTheme.typography.titleMedium.fontStyle,
                fontSize = MaterialTheme.typography.titleMedium.fontSize
            )
        }
        Text(
            text = quickTimeSettingDescription,
            modifier = Modifier.wrapContentSize(),
            color = MaterialTheme.colorScheme.primary,
            fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
            fontSize = MaterialTheme.typography.bodySmall.fontSize
        )
    }
}

@Preview
@Composable
fun SettingsActivityPreview() {
    SettingsActivityContent()
}