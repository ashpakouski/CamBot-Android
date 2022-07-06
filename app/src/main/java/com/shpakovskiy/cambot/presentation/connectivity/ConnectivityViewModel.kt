package com.shpakovskiy.cambot.presentation.connectivity

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.shpakovskiy.cambot.bluetooth.BluetoothConnector
import com.shpakovskiy.cambot.bluetooth.BluetoothWorker
import com.shpakovskiy.cambot.data.LocalWebServer
import com.shpakovskiy.cambot.data.LocalWebSocketServer
import com.shpakovskiy.cambot.data.MessageListener
import com.shpakovskiy.cambot.presentation.connectivity.state.ConnectionState
import com.shpakovskiy.cambot.presentation.connectivity.state.RobotConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalPermissionsApi
@HiltViewModel
class ConnectivityViewModel @Inject constructor(
    private val bluetoothConnector: BluetoothConnector,
    private val webSocketServer: LocalWebSocketServer,
    private val workManager: WorkManager,
    private val webServer: LocalWebServer
) : ViewModel() {
    private val _state = mutableStateOf(ConnectionState())
    val state: State<ConnectionState> = _state

    private var bluetoothAdapter: BluetoothAdapter? = null

    /*
    init {
        startWebSocketServer()
    }
     */

    fun setPermissionsState(allGranted: Boolean) {
        _state.value = _state.value.copy(
            requiredPermissionsGranted = allGranted
        )
    }

    fun setBluetoothTurnedOn(isTurnedOn: Boolean, bluetoothAdapter: BluetoothAdapter) {
        _state.value = _state.value.copy(
            isBluetoothTurnedOn = isTurnedOn
        )

        this.bluetoothAdapter = bluetoothAdapter
    }

    fun connectToRobot() {
        bluetoothAdapter?.let {
            bluetoothConnector.connect(
                it,
                onConnected = {
                    Log.d("TAG123", "Bluetooth connection succeeded")

                    _state.value = _state.value.copy(
                        isBluetoothConnected = true
                        // robotConnectionState = RobotConnectionState.CONNECTED
                    )
                },
                onConnectionFailed = {
                    Log.d("TAG123", "Bluetooth connection failed")

                    _state.value = _state.value.copy(
                        isBluetoothConnected = false
                        // robotConnectionState = RobotConnectionState.CONNECTION_FAILED
                    )
                }
            )
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

        CoroutineScope(Dispatchers.IO).launch {
            webServer.server.start(wait = true)
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