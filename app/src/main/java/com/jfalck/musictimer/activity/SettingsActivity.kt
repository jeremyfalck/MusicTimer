package com.jfalck.musictimer.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jfalck.musictimer.R
import com.jfalck.musictimer.data.DataStoreManager
import com.jfalck.musictimer.ui.component.CenterAlignedTopAppBar
import com.jfalck.musictimer.ui.theme.MusicTimerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SettingsActivity : ComponentActivity() {

    private val dataStoreManager: DataStoreManager by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    private fun initView() {
        setContent {

            val isDebugEnabled = dataStoreManager.getDevModeEnabledFlow().collectAsState(false)

            SettingsActivityContent(
                topAppBarTitle = getString(R.string.settings),
                onDevModeChanged = { isChecked ->
                    CoroutineScope(Dispatchers.IO).launch {
                        dataStoreManager.setDevModeEnabled(isChecked)
                    }
                },
                isDebugEnabled = isDebugEnabled.value
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsActivityContent(
    topAppBarTitle: String = "Preferences",
    onDevModeChanged: (Boolean) -> Unit = {},
    isDebugEnabled: Boolean = false
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
                    onDevModeChanged,
                    isDebugEnabled
                )
            }

        }
    }
}


@Composable
private fun SettingsActivitySubContent(
    innerPadding: PaddingValues,
    onDevModeChanged: (Boolean) -> Unit,
    isDebugEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .wrapContentHeight()
            .padding(innerPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically
        ) {
            Text(
                text = "Dev mode enabled",
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

}

@Preview
@Composable
fun SettingsActivityPreview() {
    SettingsActivityContent()
}