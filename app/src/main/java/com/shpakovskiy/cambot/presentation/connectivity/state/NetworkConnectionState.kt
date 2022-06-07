package com.shpakovskiy.cambot.presentation.connectivity.state

data class NetworkConnectionState(
    val localIpAddress: String = "",
    val isWebClientConnected: Boolean = false
)
