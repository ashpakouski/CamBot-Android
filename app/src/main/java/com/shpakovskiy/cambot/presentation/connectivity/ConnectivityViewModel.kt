package com.shpakovskiy.cambot.presentation.connectivity

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.shpakovskiy.cambot.bluetooth.BluetoothWorker
import com.shpakovskiy.cambot.data.LocalWebSocketServer
import com.shpakovskiy.cambot.data.MessageListener
import com.shpakovskiy.cambot.presentation.connectivity.state.BluetoothConnectionState
import com.shpakovskiy.cambot.presentation.connectivity.state.PermissionsState
import okio.IOException
import java.util.*

@ExperimentalPermissionsApi
class ConnectivityViewModel(
    application: Application,
    private val webSocketServer: LocalWebSocketServer
) : ViewModel(), MessageListener {
    private val _permissionsState = mutableStateOf(PermissionsState())
    val permissionsState: State<PermissionsState> = _permissionsState

    private val _bluetoothConnectionState = mutableStateOf(BluetoothConnectionState())
    val bluetoothConnectionState: State<BluetoothConnectionState> = _bluetoothConnectionState

    private val workManager = WorkManager.getInstance(application)

    private var bluetoothAdapter: BluetoothAdapter? = null

    //    private var bluetoothSocket: BluetoothSocket? = null
    // private var webSocketServer: LocalWebSocketServer? = null

    companion object {
        var bluetoothSocket: BluetoothSocket? = null
    }

    // private var connectThread: ConnectThread? = null

    init {
        startWebSocketServer()
    }

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
        Log.d("TAG123", "Establish connection!!!")

//        connectThread = ConnectThread(bluetoothAdapter)
//        connectThread!!.start()


        if (bluetoothSocket == null || (!bluetoothSocket!!.isConnected)) {
            bluetoothAdapter?.bondedDevices?.forEach { device ->
                Log.d("TAG123", "Name: ${device.name}")

                if (device.name == "HC05") {
                    bluetoothAdapter?.cancelDiscovery()

                    try {
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(
                            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                        )
                        bluetoothSocket?.let { socket ->
                            var attempts = 0

                            do {
                                //withContext(Dispatchers.Main) {
                                socket.connect()
                                //}
                                attempts++
                            } while (!socket.isConnected/* && attempts < 3*/)

                            Log.d("TAG123", "Bluetoooth was just connected")

                            socket.outputStream.write(1)
                            socket.outputStream.write("F".toByteArray())

                            _bluetoothConnectionState.value = BluetoothConnectionState(
                                isBluetoothTurnedOn = true,
                                isBluetoothConnected = true
                            )
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    }

    private fun startWebSocketServer() {
        try {
            webSocketServer.messageListener = this
            if (!webSocketServer.isStarted) {
                webSocketServer.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun oneStepForward() {
        try {
            bluetoothSocket?.outputStream?.write("F".toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var lastCommand = 0L

    override fun onTextMessageReceived(message: String) {
        val thisTime = System.currentTimeMillis()
        if (thisTime - lastCommand > 100) {
            lastCommand = thisTime
            sendBluetoothCommand(message)
        }
    }

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