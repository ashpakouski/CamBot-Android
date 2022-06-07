package com.shpakovskiy.cambot.presentation.connectivity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.shpakovskiy.cambot.presentation.connectivity.state.BluetoothConnectionState
import com.shpakovskiy.cambot.presentation.connectivity.state.PermissionsState
import okio.IOException
import java.util.*

@ExperimentalPermissionsApi
class ConnectivityViewModel : ViewModel() {
    private val _permissionsState = mutableStateOf(PermissionsState())
    val permissionsState: State<PermissionsState> = _permissionsState

    private val _bluetoothConnectionState = mutableStateOf(BluetoothConnectionState())
    val bluetoothConnectionState: State<BluetoothConnectionState> = _bluetoothConnectionState

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null

    fun setPermissionsState(allGranted: Boolean) {
        _permissionsState.value = PermissionsState(allPermissionsGranted = allGranted)
    }

    fun setBluetoothTurnedOn(isTurnedOn: Boolean, bluetoothAdapter: BluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter
        _bluetoothConnectionState.value = BluetoothConnectionState(
            isBluetoothTurnedOn = isTurnedOn,
            isBluetoothConnected = _bluetoothConnectionState.value.isBluetoothConnected
        )

        if (isTurnedOn && _permissionsState.value.allPermissionsGranted) {
            establishBluetoothConnection()
        }
    }

    @SuppressLint("MissingPermission")
    private fun establishBluetoothConnection() {
        //CoroutineScope(Dispatchers.IO).launch {
        bluetoothAdapter?.bondedDevices?.forEach { device ->
            Log.d("TAG123", "Name: ${device.name}")

            if (device.name == "HC05") {
                bluetoothAdapter?.cancelDiscovery()

                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                    )
                    bluetoothSocket?.connect()

                    _bluetoothConnectionState.value = BluetoothConnectionState(
                        isBluetoothTurnedOn = true,
                        isBluetoothConnected = true
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        //}
    }
}