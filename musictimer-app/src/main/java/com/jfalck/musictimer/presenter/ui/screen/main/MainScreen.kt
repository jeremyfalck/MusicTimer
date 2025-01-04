package com.jfalck.musictimer.presenter.ui.screen.main

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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.jfalck.musictimer.R
import com.jfalck.musictimer.presenter.Previews
import com.jfalck.musictimer.presenter.TextManager
import com.jfalck.musictimer.presenter.ui.AdmobBanner
import com.jfalck.musictimer.presenter.ui.component.CenterAlignedTopAppBar
import com.jfalck.musictimer.presenter.ui.component.TimeSelectionSlider
import com.jfalck.musictimer.presenter.ui.screen.settings.SettingsScreen
import com.jfalck.musictimer.presenter.ui.theme.MusicTimerTheme
import com.jfalck.musictimer.presenter.vibration.VibratorManager
import com.jfalck.musictimer.presenter.viewmodel.AdsViewModel
import com.jfalck.musictimer.presenter.viewmodel.TimerViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object MainScreen

@Composable
fun MainScreen(
    timerViewModel: TimerViewModel,
    adsViewModel: AdsViewModel,
    navController: NavHostController,
    vibratorManager: VibratorManager,
    textManager: TextManager,
    startMuteTimer: (Float) -> Unit,
    stopMuteTimer: () -> Unit

) {

    val sliderPosition by
    timerViewModel.timeValueSelected.collectAsState(initial = 1f).asFloatState()
    val intSliderValue = sliderPosition.toInt()

    val timerRunning by timerViewModel.isTimerRunning.collectAsState(initial = false)

    val isPaidUser = adsViewModel.isPaidUser.collectAsState(initial = false)

    val sliderText = textManager.getQuantityString(
        R.plurals.timer_value_selected,
        intSliderValue
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val snackBarCoroutineScope = rememberCoroutineScope()

    MainActivityContent(
        snackbarHostState = snackbarHostState,
        isPaidUser = isPaidUser.value,
        timerRunning = timerRunning,
        topAppBarTitle = textManager.getString(R.string.app_name),
        onSettingsClick = { navController.navigate(SettingsScreen) },
        sliderPosition = sliderPosition,
        sliderText = sliderText,
        onSliderValueChanged = {
            vibratorManager.vibrate()
            timerViewModel.setTimeValueSelected(it)
        },
        onTimerButtonClick = { position: Float, isTimerRunning: Boolean ->
            if (timerRunning) {
                stopMuteTimer()
            } else {
                startMuteTimer(position)
            }
            if (!isTimerRunning) {
                snackBarCoroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = textManager.getString(
                            R.string.timer_start_toast,
                            sliderPosition.toInt()
                        ),
                        actionLabel = "OK",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        },
        buttonText = textManager.getString(if (timerRunning) R.string.stop_timer else R.string.start_timer)
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
                        showBackButton = false,
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

@Composable
@Previews
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