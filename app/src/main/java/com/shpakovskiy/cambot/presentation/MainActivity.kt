package com.shpakovskiy.cambot.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.shpakovskiy.cambot.data.server.KtorHttpServer
import com.shpakovskiy.cambot.data.util.getDeviceIpAddress
import com.shpakovskiy.cambot.presentation.ui.theme.CamBotTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        Log.d("TAG123", "IP: ${getDeviceIpAddress(this)}")

        lifecycleScope.launch {
            KtorHttpServer(applicationContext).start(8081)
        }

        setContent {
            CamBotTheme {
                MainScreen()
            }
        }
    }
}

@Composable
private fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        viewModel.state?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
                    .size(200.dp)
                    .background(Color.Green)
                    .graphicsLayer {
                        rotationZ = 90f
                    }
            )
        }
    }
}