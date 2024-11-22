package com.shpakovskiy.cambot.presentation

import android.bluetooth.BluetoothAdapter
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shpakovskiy.cambot.data.camera.CameraFrameFeedProvider
import com.shpakovskiy.cambot.data.server.WebSocketServer
import com.shpakovskiy.cambot.presentation.connectivity.state.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val cameraFeedProvider: CameraFrameFeedProvider,
    private val webSocketServer: WebSocketServer
    // private val bluetoothConnector: BluetoothConnector,
    // private val webSocketServer: LocalWebSocketServer,
    // private val workManager: WorkManager,
    // private val webServer: LocalWebServer
) : ViewModel() {
//    private val _state = mutableStateOf(ConnectionState())
//    val state: State<ConnectionState> = _state
//
//    private val _contextOwnerEvent = MutableSharedFlow<ContextOwnerEvent>()
//    val contextOwnerEvent = _contextOwnerEvent.asSharedFlow()
//
//    private var bluetoothAdapter: BluetoothAdapter? = null

    var state by mutableStateOf<Bitmap?>(null)

    private var lastBitmap: Bitmap? = null

    init {
        viewModelScope.launch {
            webSocketServer.start(9998)
        }

        viewModelScope.launch {
            delay(5000L)

            cameraFeedProvider.startCaptureSession().collect {
                lastBitmap?.recycle()

                state = it

                val byteStream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 25, byteStream)

                byteStream.use { stream ->
                    webSocketServer.broadcastBinary(stream.toByteArray())
                }

                lastBitmap = it
            }
        }
    }

//    fun setPermissionsState(allGranted: Boolean) {
//        _state.value = _state.value.copy(
//            requiredPermissionsGranted = allGranted
//        )
//    }
//
//    fun setBluetoothTurnedOn(isTurnedOn: Boolean, bluetoothAdapter: BluetoothAdapter) {
//        _state.value = _state.value.copy(
//            isBluetoothTurnedOn = isTurnedOn
//        )
//
//        this.bluetoothAdapter = bluetoothAdapter
//    }

    fun connectToRobot() {
//        bluetoothAdapter?.let {
//            bluetoothConnector.connect(
//                it,
//                onConnected = {
//                    Log.d("TAG123", "Bluetooth connection succeeded")
//
//                    _state.value = _state.value.copy(
//                        isBluetoothConnected = true
//                        // robotConnectionState = RobotConnectionState.CONNECTED
//                    )
//
//                    viewModelScope.launch {
//                        _contextOwnerEvent.emit(ContextOwnerEvent.StartCameraService)
//                    }
//
//                    startWebSocketServer()
//                },
//                onConnectionFailed = {
//                    Log.d("TAG123", "Bluetooth connection failed")
//
//                    _state.value = _state.value.copy(
//                        isBluetoothConnected = false
//                        // robotConnectionState = RobotConnectionState.CONNECTION_FAILED
//                    )
//                }
//            )
//        }
    }

    private fun startWebSocketServer() {
        try {
//            webSocketServer.messageListener = object : MessageListener {
//                override fun onTextMessageReceived(message: String) {
//                    sendBluetoothCommand(message)
//                }
//            }
//
//            if (!webSocketServer.isStarted) {
//                webSocketServer.start()
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

//        viewModelScope.launch(Dispatchers.IO) {
//            webServer.server.start(wait = true)
//        }

//        _state.value = _state.value.copy(
//            isWebSocketServerRunning = true,
//            isWebServerRunning = true
//        )
    }

    private fun sendBluetoothCommand(command: String) {
//        var continuation = workManager.beginUniqueWork(
//            "BluetoothWork",
//            ExistingWorkPolicy.REPLACE,
//            OneTimeWorkRequest.from(BluetoothWorker::class.java)
//        )
//
//        val workRequestBuilder = OneTimeWorkRequestBuilder<BluetoothWorker>()
//        val builder = Data.Builder()
//        builder.putString("command", command)
//        workRequestBuilder.setInputData(builder.build())
//        continuation = continuation.then(workRequestBuilder.build())
//        continuation.enqueue()
    }

    sealed class ContextOwnerEvent {
        object StartCameraService : ContextOwnerEvent()
    }
}