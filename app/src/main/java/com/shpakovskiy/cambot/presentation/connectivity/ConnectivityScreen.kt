package com.shpakovskiy.cambot.presentation.connectivity

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.shpakovskiy.cambot.common.ExecutionStatus
import com.shpakovskiy.cambot.common.REQUIRED_PERMISSIONS
import com.shpakovskiy.cambot.presentation.connectivity.component.ActionCard

@Composable
@ExperimentalPermissionsApi
@ExperimentalFoundationApi
fun ConnectivityScreen(
    viewModel: ConnectivityViewModel
) {
    val appPermissionsState: MultiplePermissionsState = rememberMultiplePermissionsState(
        REQUIRED_PERMISSIONS
    )

    viewModel.setPermissionsState(appPermissionsState.allPermissionsGranted)

    val bluetoothManager =
        LocalContext.current.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter

    viewModel.setBluetoothTurnedOn(
        isTurnedOn = bluetoothAdapter.isEnabled,
        bluetoothAdapter = bluetoothAdapter
    )

    val bluetoothSwitcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.setBluetoothTurnedOn(
            isTurnedOn = (it.resultCode == Activity.RESULT_OK),
            bluetoothAdapter = bluetoothManager.adapter
        )
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        ActionCard(
            title = "Grant permissions",
            description = "You have to grant all required permissions to let the app work properly",
            onAction = {
                appPermissionsState.launchMultiplePermissionRequest()
            },
            actionButtonLabel = "Grant",
            executionStatus = if (appPermissionsState.allPermissionsGranted) ExecutionStatus.FINISHED else ExecutionStatus.FAILED
        )

        ActionCard(
            title = "Turn Bluetooth on",
            description = "Bluetooth has to be turned on",
            onAction = {
                bluetoothSwitcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            },
            actionButtonLabel = "Turn on",
            executionStatus = if (viewModel.state.value.isBluetoothTurnedOn) ExecutionStatus.FINISHED else ExecutionStatus.FAILED
        )

        ActionCard(
            title = "Connect to car",
            description = "Establish bluetooth connection with on-car Bluetooth adapter",
            onAction = {
                viewModel.connectToRobot()
            },
            actionButtonLabel = "Connect",
            executionStatus = if (viewModel.state.value.isBluetoothConnected) ExecutionStatus.FINISHED else ExecutionStatus.FAILED
        )

        ActionCard(
            title = "Start WebSocket server",
            description = "Embedded WebSocket server allows accepts commands sent from browser",
            onAction = {
                viewModel.connectToRobot()
            },
            actionButtonLabel = "Start server",
            executionStatus = if (viewModel.state.value.isWebSocketServerRunning) ExecutionStatus.FINISHED else ExecutionStatus.FAILED
        )

        ActionCard(
            title = "Start embedded WebServer",
            description = "Embedded WebServer allows user to access car control page as a website",
            onAction = {
                viewModel.connectToRobot()
            },
            actionButtonLabel = "Start server",
            executionStatus = if (viewModel.state.value.isBluetoothConnected) ExecutionStatus.FINISHED else ExecutionStatus.FAILED
        )
    }
}