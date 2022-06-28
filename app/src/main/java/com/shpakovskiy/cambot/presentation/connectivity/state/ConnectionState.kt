package com.shpakovskiy.cambot.presentation.connectivity.state

data class ConnectionState(
    val requiredPermissionsGranted: Boolean = false,
    val isBluetoothTurnedOn: Boolean = false,
    val isBluetoothConnected: Boolean = false,
    val isWebSocketServerConnected: Boolean = false,
    val isWebServerConnected: Boolean = false,
    val localIpAddress: String? = null
)
