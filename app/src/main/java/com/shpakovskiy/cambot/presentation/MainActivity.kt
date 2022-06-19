package com.shpakovskiy.cambot.presentation

import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.shpakovskiy.cambot.data.LocalWebSocketServer
import com.shpakovskiy.cambot.presentation.connectivity.ConnectivityScreen
import com.shpakovskiy.cambot.presentation.connectivity.ConnectivityViewModel
import com.shpakovskiy.cambot.presentation.ui.theme.CamBotTheme
import com.shpakovskiy.cambot.service.CameraService
import java.net.InetSocketAddress

@ExperimentalPermissionsApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startService(Intent(this, CameraService::class.java))

        val inetSocketAddress = InetSocketAddress("192.168.100.2", 9999)
        val webSocketServer = LocalWebSocketServer(inetSocketAddress)
        webSocketServer.isReuseAddr = true

        /*
        val ipAddress = (applicationContext.getSystemService(WIFI_SERVICE) as WifiManager)
            .connectionInfo
            .ipAddress

        val textIpAddress = android.text.format.Formatter.formatIpAddress(ipAddress)

        Log.d("TAG123", "IP Address: $textIpAddress")
         */

        setContent {
            CamBotTheme {
                Column {
                    TopAppBar(title = { Text(text = "Camera Bot") })

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        val navController = rememberNavController()

                        NavHost(
                            navController = navController,
                            startDestination = Screen.ConnectivityScreen.route
                        ) {
                            composable(
                                route = Screen.ConnectivityScreen.route
                            ) {
                                ConnectivityScreen(
                                    navController = navController,
                                    viewModel = ConnectivityViewModel(
                                        application,
                                        webSocketServer
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}