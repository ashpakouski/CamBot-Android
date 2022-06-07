package com.shpakovskiy.cambot.presentation

sealed class Screen(val route: String) {
    object ConnectivityScreen: Screen("connectivity_screen")
}
