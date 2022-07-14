package com.shpakovskiy.cambot.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import okio.IOException
import java.util.*

class BluetoothConnector {
    companion object {
        private const val TAG = "BluetoothConnector"
    }

    private var bluetoothSocket: BluetoothSocket? = null

    fun send(message: String) {
        bluetoothSocket?.outputStream?.write(message.toByteArray())
    }

    @SuppressLint("MissingPermission")
    fun connect(
        bluetoothAdapter: BluetoothAdapter,
        onConnected: () -> Unit,
        onConnectionFailed: () -> Unit
    ) {
        if (bluetoothSocket == null || (bluetoothSocket?.isConnected == false)) {
            bluetoothAdapter.bondedDevices?.forEach { device ->
                Log.d(TAG, "Bluetooth device: ${device.name}")

                if (device.name == "HC05") {
                    bluetoothAdapter.cancelDiscovery()

                    try {
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(
                            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                        )
                        bluetoothSocket?.let { socket ->
                            socket.connect()
                            onConnected()
                        }
                    } catch (e: IOException) {
                        onConnectionFailed()
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}