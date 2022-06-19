package com.shpakovskiy.cambot.bluetooth

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.shpakovskiy.cambot.presentation.connectivity.ConnectivityViewModel

@ExperimentalPermissionsApi
class BluetoothWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val stringCommand = inputData.getString("command")

        stringCommand?.let {
            ConnectivityViewModel.bluetoothSocket?.outputStream?.write(it.toByteArray())
        }

        return Result.success()
    }
}