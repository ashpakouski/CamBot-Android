package com.shpakovskiy.cambot.presentation.connectivity.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shpakovskiy.cambot.common.ExecutionStatus

@Composable
fun ActionCard(
    title: String,
    description: String? = null,
    onAction: (() -> Unit)? = null,
    actionButtonLabel: String? = null,
    executionStatus: ExecutionStatus = ExecutionStatus.AWAITING
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        // backgroundColor = MaterialTheme.colors.surface,
        // elevation = 3.dp,
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            StatusRow(propertyName = title, isActive = executionStatus == ExecutionStatus.FINISHED)
            description?.let { Text(text = it) }
            onAction?.let {
                Button(
                    onClick = it,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                ) {
                    actionButtonLabel?.let { label -> Text(text = label) }
                }
            }
        }
    }
}