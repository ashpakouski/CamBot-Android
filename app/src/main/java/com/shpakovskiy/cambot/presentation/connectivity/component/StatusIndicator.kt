package com.shpakovskiy.cambot.presentation.connectivity.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun StatusRow(propertyName: String, isActive: Boolean) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = propertyName,
            style = MaterialTheme.typography.h6,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1F)
        )
        StatusIndicator(
            isActive = isActive
        )
    }
}

@Composable
fun StatusIndicator(isActive: Boolean) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(25.dp), onDraw = {
            drawCircle(color = if (isActive) Color.Green else Color.Red, alpha = 0.4f)
        })
        Canvas(modifier = Modifier.size(15.dp), onDraw = {
            drawCircle(color = if (isActive) Color.Green else Color.Red, alpha = 0.8f)
        })
    }
}