package com.shpakovskiy.cambot.presentation.connectivity

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
@ExperimentalPermissionsApi
fun ConnectivityScreen(
    navController: NavController,
    viewModel: ConnectivityViewModel
) {
    Column {
        LocationPermissionCard(viewModel = viewModel)
        BluetoothConnectionCard(viewModel = viewModel)
        DeviceConnectionCard(viewModel = viewModel)
    }
}

@Composable
@ExperimentalPermissionsApi
fun LocationPermissionCard(viewModel: ConnectivityViewModel) {
    val locationPermissionState: MultiplePermissionsState = rememberMultiplePermissionsState(
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.CAMERA
            )
        else
            listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN
            )
    )

    viewModel.setPermissionsState(locationPermissionState.allPermissionsGranted)

    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 3.dp,
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusRow(
                //propertyName = "Required permissions (Location, Camera and Nearby devices access)",
                propertyName = "Permissions",
                isActive = viewModel.permissionsState.value.allPermissionsGranted
            )

            if (!viewModel.permissionsState.value.allPermissionsGranted) {
                Button(
                    onClick = {
                        locationPermissionState.launchMultiplePermissionRequest()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp, 0.dp, 16.dp, 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Grant permission")
                }
            }
        }
    }
}

@Composable
@ExperimentalPermissionsApi
fun BluetoothConnectionCard(viewModel: ConnectivityViewModel) {
    val bluetoothManager =
        LocalContext.current.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    viewModel.setBluetoothTurnedOn(bluetoothManager.adapter.isEnabled, bluetoothManager.adapter)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.setBluetoothTurnedOn(
            isTurnedOn = it.resultCode == Activity.RESULT_OK,
            bluetoothAdapter = bluetoothManager.adapter
        )
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 3.dp,
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusRow(
                propertyName = "Bluetooth",
                isActive = viewModel.bluetoothConnectionState.value.isBluetoothTurnedOn
            )

            if (!viewModel.bluetoothConnectionState.value.isBluetoothTurnedOn) {
                Button(
                    onClick = {
                        launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp, 0.dp, 16.dp, 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Turn on")
                }
            }
        }
    }
}

@Composable
@ExperimentalPermissionsApi
fun DeviceConnectionCard(viewModel: ConnectivityViewModel) {
    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 3.dp,
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusRow(
                propertyName = "Robot connected",
                isActive = viewModel.bluetoothConnectionState.value.isBluetoothConnected
            )
        }
    }
}

@Composable
fun StatusRow(propertyName: String, isActive: Boolean) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = propertyName,
            style = MaterialTheme.typography.h6,
            overflow = TextOverflow.Ellipsis
        )
        StatusIndicator(
            isActive = isActive
        )
    }
}

@Composable
fun StatusIndicator(isActive: Boolean) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(25.dp), onDraw = {
            drawCircle(color = if (isActive) Color.Green else Color.Red, alpha = 0.4f)
        })
        Canvas(modifier = Modifier.size(15.dp), onDraw = {
            drawCircle(color = if (isActive) Color.Green else Color.Red, alpha = 0.8f)
        })
    }
}
