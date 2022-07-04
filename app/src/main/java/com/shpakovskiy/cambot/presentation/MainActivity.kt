package com.shpakovskiy.cambot.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.shpakovskiy.cambot.data.LocalWebSocketServer
import com.shpakovskiy.cambot.presentation.connectivity.ConnectivityScreen
import com.shpakovskiy.cambot.presentation.connectivity.ConnectivityViewModel
import com.shpakovskiy.cambot.presentation.ui.theme.CamBotTheme
import com.shpakovskiy.cambot.service.CameraService
import dagger.hilt.android.AndroidEntryPoint
import java.net.InetSocketAddress

@ExperimentalPermissionsApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // startService(Intent(this, CameraService::class.java))

        setContent {
            CamBotTheme {
                Column {
                    TopAppBar(title = { Text(text = "Camera Bot") })

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        ConnectivityScreen(
                            viewModel = hiltViewModel()
                        )
                    }
                }
            }
        }
    }
}