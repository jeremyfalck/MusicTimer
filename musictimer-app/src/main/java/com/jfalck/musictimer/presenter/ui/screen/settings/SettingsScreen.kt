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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jfalck.musictimer.presenter.ui.component.CenterAlignedTopAppBar
import com.jfalck.musictimer.presenter.ui.component.TimeSelectionSlider
import com.jfalck.musictimer.presenter.ui.theme.MusicTimerTheme
import kotlinx.serialization.Serializable


@Serializable
object SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    topAppBarTitle: String = "Preferences",
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
@Composable
fun SettingsActivityPreview() {
    SettingsScreen(topAppBarTitle = "Preferences",
        quickTimeSettingTitle = "Quick Time Settings",
        quickTimeSettingDescription = "",
        onQuickTimeClicked = {},
        quickTimeSettingsValue = 20,
        showTimeQuickSettingDialog = false,
        onQuickTimeValueSelected = {},
        onRemoveAdsClick = {})
}