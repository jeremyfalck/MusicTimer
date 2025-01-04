package com.jfalck.musictimer.presenter.ui.screen.main

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.ui.AdmobBanner
import com.jfalck.musictimer.presenter.ui.component.CenterAlignedTopAppBar
import com.jfalck.musictimer.presenter.ui.component.TimeSelectionSlider
import com.jfalck.musictimer.presenter.ui.theme.MusicTimerTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object MainScreen

@Composable
fun MainScreen(
    context: Context,
    isTimerRunning: Boolean,
    sliderPosition: Float,
    isPaidUser: Boolean,
    notifyTimeValueChanged: (Float) -> Unit,
    onTimerButtonClick: (Float, Boolean) -> Unit,
    onSettingsClicked: () -> Unit
) {

    val intValue = sliderPosition.toInt()

    val sliderText = context.resources.getQuantityString(
        R.plurals.timer_value_selected,
        intValue,
        intValue
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val snackBarCoroutineScope = rememberCoroutineScope()

    MainActivityContent(
        snackbarHostState = snackbarHostState,
        isPaidUser = isPaidUser,
        timerRunning = isTimerRunning,
        topAppBarTitle = context.getString(R.string.app_name),
        onSettingsClick = onSettingsClicked,
        sliderPosition = sliderPosition,
        sliderText = sliderText,
        onSliderValueChanged = notifyTimeValueChanged,
        onTimerButtonClick = { position: Float, timerRunning: Boolean ->
            onTimerButtonClick(position, timerRunning)
            if (!timerRunning) {
                snackBarCoroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.timer_start_toast,
                            sliderPosition.toInt()
                        ),
                        actionLabel = "OK"
                    )
                }
            }
        },
        buttonText = context.getString(if (isTimerRunning) R.string.stop_timer else R.string.start_timer)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    snackbarHostState: SnackbarHostState,
    isPaidUser: Boolean,
    timerRunning: Boolean,
    topAppBarTitle: String,
    onSettingsClick: () -> Unit = {},
    sliderPosition: Float,
    sliderText: String,
    onSliderValueChanged: (Float) -> Unit,
    onTimerButtonClick: (Float, Boolean) -> Unit,
    buttonText: String
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
                        showSettingsButton = true,
                        onSettingsClick = onSettingsClick,
                        scrollBehavior = scrollBehavior
                    )
                },
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                },
            ) { innerPadding ->
                MainActivitySubContent(
                    isPaidUser = isPaidUser,
                    innerPadding = innerPadding,
                    sliderPosition = sliderPosition,
                    sliderText = sliderText,
                    onSliderValueChanged = onSliderValueChanged,
                    timerRunning = timerRunning,
                    onTimerButtonClick = onTimerButtonClick,
                    buttonText = buttonText
                )
            }

        }
    }
}

@Composable
fun MainActivitySubContent(
    isPaidUser: Boolean,
    innerPadding: PaddingValues,
    sliderPosition: Float,
    sliderText: String,
    onSliderValueChanged: (Float) -> Unit,
    timerRunning: Boolean,
    onTimerButtonClick: (Float, Boolean) -> Unit,
    buttonText: String
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        TimeSelectionSlider(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 36.dp),
            value = sliderPosition,
            onValueChange = onSliderValueChanged,
            valueRange = 1F..90F,
            steps = 90,
        )

        Text(
            text = sliderText,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.secondary
        )


        Button(
            onClick = { onTimerButtonClick(sliderPosition, timerRunning) },
            modifier = Modifier.padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(buttonText)
        }

        Spacer(modifier = Modifier.weight(1f))
        if (!isPaidUser) {
            AdmobBanner(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityPreview() {
    MainActivityContent(
        SnackbarHostState(),
        true,
        timerRunning = false,
        topAppBarTitle = "MusicTimer",
        sliderPosition = 30f,
        sliderText = "30 minutes",
        onSliderValueChanged = { },
        onTimerButtonClick = { _, _ -> },
        buttonText = "Start timer"
    )
}