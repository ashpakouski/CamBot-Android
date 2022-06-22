package com.shpakovskiy.cambot.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.util.Log
import okio.IOException
import java.util.*

class BluetoothConnector {

    var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null

    fun send(message: String) {
        bluetoothSocket?.outputStream?.write(message.toByteArray())
    }

    @SuppressLint("MissingPermission")
    fun establishConnection() {
        Log.d("TAG123", "Establish connection!!!")

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

//                            _bluetoothConnectionState.value = BluetoothConnectionState(
//                                isBluetoothTurnedOn = true,
//                                isBluetoothConnected = true
//                            )
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    }
}