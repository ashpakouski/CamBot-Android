package com.shpakovskiy.cambot.presentation.connectivity

import android.bluetooth.BluetoothAdapter
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.shpakovskiy.cambot.bluetooth.BluetoothConnector
import com.shpakovskiy.cambot.bluetooth.BluetoothWorker
import com.shpakovskiy.cambot.data.LocalWebSocketServer
import com.shpakovskiy.cambot.data.MessageListener
import com.shpakovskiy.cambot.presentation.connectivity.state.BluetoothConnectionState
import com.shpakovskiy.cambot.presentation.connectivity.state.PermissionsState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@ExperimentalPermissionsApi
@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    private val bluetoothConnector: BluetoothConnector,
    private val webSocketServer: LocalWebSocketServer,
    private val workManager: WorkManager
) : ViewModel() {
    private val _permissionsState = mutableStateOf(PermissionsState())
    val permissionsState: State<PermissionsState> = _permissionsState

    private val _bluetoothConnectionState = mutableStateOf(BluetoothConnectionState())
    val bluetoothConnectionState: State<BluetoothConnectionState> = _bluetoothConnectionState

    init {
        startWebSocketServer()
    }

    fun setPermissionsState(allGranted: Boolean) {
        _permissionsState.value = PermissionsState(allPermissionsGranted = allGranted)
    }

    fun setBluetoothTurnedOn(isTurnedOn: Boolean, bluetoothAdapter: BluetoothAdapter) {
        _bluetoothConnectionState.value = BluetoothConnectionState(
            isBluetoothTurnedOn = isTurnedOn,
            isBluetoothConnected = _bluetoothConnectionState.value.isBluetoothConnected
        )

        if (isTurnedOn && _permissionsState.value.allPermissionsGranted) {
            bluetoothConnector.connect(bluetoothAdapter) {
                _bluetoothConnectionState.value = BluetoothConnectionState(
                    isBluetoothTurnedOn = true,
                    isBluetoothConnected = true
                )
            }
        }
    }

    private fun startWebSocketServer() {
        try {
            webSocketServer.messageListener = object : MessageListener {
                override fun onTextMessageReceived(message: String) {
                    val thisTime = System.currentTimeMillis()
                    if (thisTime - lastCommand > 100) {
                        lastCommand = thisTime
                        sendBluetoothCommand(message)
                    }
                }
            }
            if (!webSocketServer.isStarted) {
                webSocketServer.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun oneStepForward() {
        try {
            bluetoothConnector.send("F")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var lastCommand = 0L

    private fun sendBluetoothCommand(command: String) {
        var continuation = workManager.beginUniqueWork(
            "BluetoothWork",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.from(BluetoothWorker::class.java)
        )

        val workRequestBuilder = OneTimeWorkRequestBuilder<BluetoothWorker>()
        val builder = Data.Builder()
        builder.putString("command", command)
        workRequestBuilder.setInputData(builder.build())
        continuation = continuation.then(workRequestBuilder.build())
        continuation.enqueue()
    }
}