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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.shpakovskiy.cambot.common.ExecutionStatus
import com.shpakovskiy.cambot.common.REQUIRED_PERMISSIONS
import com.shpakovskiy.cambot.common.WEB_SERVER_PORT
import com.shpakovskiy.cambot.data.service.CameraService
import com.shpakovskiy.cambot.presentation.connectivity.component.ActionCard
import com.shpakovskiy.cambot.util.getDeviceIpAddress
import kotlinx.coroutines.flow.collectLatest

@Composable
@ExperimentalFoundationApi
fun ConnectivityScreen(
    viewModel: ConnectivityViewModel
) {
    val context = LocalContext.current
    val state = viewModel.state.value
    val bluetoothManager =
        LocalContext.current.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter
    val serverIpAddress = "${getDeviceIpAddress(context)}:$WEB_SERVER_PORT"

//    val appPermissionsState: MultiplePermissionsState = rememberMultiplePermissionsState(
//        REQUIRED_PERMISSIONS
//    )

//    viewModel.setPermissionsState(appPermissionsState.allPermissionsGranted)

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

    LaunchedEffect(key1 = true) {
        viewModel.contextOwnerEvent.collectLatest { event ->
            when (event) {
                is ConnectivityViewModel.ContextOwnerEvent.StartCameraService -> {
                    context.startService(Intent(context, CameraService::class.java))
                }
            }
        }
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
//        ActionCard(
//            title = "Grant permissions",
//            description = "You have to grant all required permissions to let the app work properly",
//            onAction = {
//                appPermissionsState.launchMultiplePermissionRequest()
//            },
//            actionButtonLabel = "Grant",
//            executionStatus = if (appPermissionsState.allPermissionsGranted) ExecutionStatus.FINISHED else ExecutionStatus.FAILED
//        )

        ActionCard(
            title = "Turn Bluetooth on",
            description = "Bluetooth has to be turned on",
            onAction = {
                bluetoothSwitcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            },
            actionButtonLabel = "Turn on",
            executionStatus = if (state.isBluetoothTurnedOn) ExecutionStatus.FINISHED else ExecutionStatus.FAILED
        )

        ActionCard(
            title = "Connect to car",
            description = "Establish bluetooth connection with on-car Bluetooth adapter",
            onAction = {
                viewModel.connectToRobot()
            },
            actionButtonLabel = "Connect",
            executionStatus = if (state.isBluetoothConnected) ExecutionStatus.FINISHED else ExecutionStatus.FAILED
        )

        ActionCard(
            title = "Start WebSocket server",
            description = "Embedded WebSocket server will be started automatically, when the car is connected",
//            onAction = {
//                viewModel.connectToRobot()
//            },
//            actionButtonLabel = "Start server",
            executionStatus = if (state.isWebSocketServerRunning) ExecutionStatus.FINISHED else ExecutionStatus.FAILED
        )

        ActionCard(
            title = "Start embedded Web server",
            description = "Embedded Web server will be started automatically, when the car is connected. Type the following address in browser to establish connection: $serverIpAddress",
//            onAction = {
//                viewModel.connectToRobot()
//            },
//            actionButtonLabel = "Start server",
            executionStatus = if (state.isWebServerRunning) ExecutionStatus.FINISHED else ExecutionStatus.FAILED
        )
    }
}