package com.shpakovskiy.cambot.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.shpakovskiy.cambot.data.server.KtorHttpServer
import com.shpakovskiy.cambot.presentation.ui.theme.CamBotTheme
import com.shpakovskiy.cambot.util.getDeviceIpAddress
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("TAG123", "IP: ${getDeviceIpAddress(this)}")

        lifecycleScope.launch {
            KtorHttpServer(applicationContext).start(8081)
        }

        setContent {
            CamBotTheme {
                Column {
                    // TopAppBar(title = { Text(text = "Camera Bot") })

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
//                        ConnectivityScreen(
//                            viewModel = hiltViewModel()
//                        )
                    }
                }
            }
        }
    }
}