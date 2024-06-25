package com.example.bluetooth.close

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import com.example.bluetooth.close.MainActivity.Companion.HOUR
import com.example.bluetooth.close.MainActivity.Companion.MINUTE
import com.example.bluetooth.close.ui.theme.BluetoothCloseTheme

/**
 * Created by bggRGjQaUbCoE on 2024/6/25
 */
class MainActivity : ComponentActivity() {

    private var requiredPermissions = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BluetoothCloseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }

        initRequiredPermissions()
        checkAndRequestPermissions()

    }

    private fun initRequiredPermissions() {
        requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)

        requiredPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if (SDK_INT >= 33) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (SDK_INT >= 34) {
            requiredPermissions.add(Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE)
        }
    }

    private fun checkAndRequestPermissions() {
        requiredPermissions.removeIf {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (requiredPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toTypedArray(), 1)
        }
    }

    companion object {
        const val TAG = "BluetoothConnection"
        const val HOUR = "HOUR"
        const val MINUTE = "MINUTE"
        const val PERMISSION = Manifest.permission.BLUETOOTH_CONNECT
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val pref = LocalContext.current.getSharedPreferences("settings", MODE_PRIVATE)
    val state = TimePickerState(
        initialHour = pref.getInt(HOUR, 0),
        initialMinute = pref.getInt(MINUTE, 30),
        is24Hour = is24HourFormat(LocalContext.current)
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.app_name))
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            FilledTonalButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text("Set Time")
            }
        }
    }
    if (showTimePicker) {
        TimePickerDialog(
            onCancel = { showTimePicker = false },
            onConfirm = {
                pref.edit().putInt(HOUR, state.hour).apply()
                pref.edit().putInt(MINUTE, with(state.minute) {
                    if (this == 0 && state.hour == 0) 1 else this
                }).apply()
                showTimePicker = false
            },
        ) {
            TimeInput(state = state)
        }
    }
}

@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier =
            Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    toggle()
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}