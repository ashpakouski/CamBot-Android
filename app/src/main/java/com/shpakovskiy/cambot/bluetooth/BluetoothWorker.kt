package com.shpakovskiy.cambot.bluetooth

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okio.IOException

@ExperimentalPermissionsApi
@HiltWorker
class BluetoothWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val bluetoothConnector: BluetoothConnector
) : Worker(context, params) {

    override fun doWork(): Result {
        val stringCommand = inputData.getString("command")

        return try {
            stringCommand?.let {
                bluetoothConnector.send(it)
            }

            Result.success()
        } catch (e: IOException) {
            Result.failure()
        }
    }
}