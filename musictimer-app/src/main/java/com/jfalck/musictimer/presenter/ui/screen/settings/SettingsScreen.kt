package com.jfalck.musictimer.presenter.ui.screen.settings

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.TextManager
import com.jfalck.musictimer.presenter.ui.component.CenterAlignedTopAppBar
import com.jfalck.musictimer.presenter.ui.component.TimeSelectionSlider
import com.jfalck.musictimer.presenter.ui.theme.MusicTimerTheme
import com.jfalck.musictimer.presenter.vibration.VibratorManager
import com.jfalck.musictimer.presenter.viewmodel.TimerViewModel
import kotlinx.serialization.Serializable


@Serializable
object SettingsScreen

@Composable
fun SettingsScreen(
    timerViewModel: TimerViewModel,
    onRemoveAdsClick: () -> Unit = {},
    vibratorManager: VibratorManager,
    textManager: TextManager
) {

    val quickSettingsTimeValue =
        timerViewModel.quickSettingsTimeValueSelected.collectAsState()

    SettingsContent(
        quickSettingsTimeValue = quickSettingsTimeValue.value,
        onRemoveAdsClick = onRemoveAdsClick,
        appBarTitle = textManager.getString(R.string.settings),
        quickTimeSettingTitle = textManager.getString(R.string.quick_time_settings),
        quickTimeSettingDescription = textManager.getString(R.string.quick_time_settings_desc),
        onQuickTimeValueSelected = { value ->
            timerViewModel.setQuickSettingsTimeValue(value.toInt())
            vibratorManager.vibrate()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    quickSettingsTimeValue: Int,
    onRemoveAdsClick: () -> Unit,
    appBarTitle: String,
    quickTimeSettingTitle: String,
    quickTimeSettingDescription: String,
    onQuickTimeValueSelected: (Float) -> Unit,
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

            var showQuickTimeSlider by remember { mutableStateOf(false) }

            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    CenterAlignedTopAppBar(
                        title = appBarTitle,
                        showSettingsButton = false,
                        scrollBehavior = scrollBehavior
                    )
                },
            ) { innerPadding ->
                SettingsActivitySubContent(
                    innerPadding = innerPadding,
                    quickTimeSettingTitle = quickTimeSettingTitle,
                    quickTimeSettingDescription = quickTimeSettingDescription,
                    onQuickTimeClicked = { showQuickTimeSlider = !showQuickTimeSlider },
                    quickSettingsTimeValue = quickSettingsTimeValue,
                    showTimeQuickSettingDialog = showQuickTimeSlider,
                    onQuickTimeValueSelected = onQuickTimeValueSelected,
                    onRemoveAdsClick = onRemoveAdsClick
                )
            }
        }
    }
}


@Composable
private fun SettingsActivitySubContent(
    innerPadding: PaddingValues,
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
        QuickSettingTimeValueOption(
            quickTimeSettingTitle,
            quickTimeSettingDescription,
            onClick = onQuickTimeClicked,
            value = quickSettingsTimeValue
        )
        if (showTimeQuickSettingDialog) TimeSelectionSlider(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp),
            value = quickSettingsTimeValue.toFloat(),
            valueRange = 1F..90F,
            onValueChange = onQuickTimeValueSelected,
            steps = 90,
        )
        RemoveAds(onClick = onRemoveAdsClick)
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
@PreviewDynamicColors
@PreviewScreenSizes
@PreviewLightDark
@Composable
fun SettingsActivityPreview() {
    SettingsContent(
        quickSettingsTimeValue = 50,
        onRemoveAdsClick = {},
        appBarTitle = "Preferences",
        quickTimeSettingTitle = "Minuteur pour les raccourcis",
        quickTimeSettingDescription = "Utilisé par le widget et dans l'accès rapide",
        onQuickTimeValueSelected = {}
    )
}
