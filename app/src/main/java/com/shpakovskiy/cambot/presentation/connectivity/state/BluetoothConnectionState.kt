package com.shpakovskiy.cambot.presentation.connectivity.state

data class BluetoothConnectionState(
    val isBluetoothTurnedOn: Boolean = false,
    val isBluetoothConnected: Boolean = false
)