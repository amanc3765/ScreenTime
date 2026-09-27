package com.example.screentime.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.screentime.ui.theme.BlueContainer
import com.example.screentime.ui.theme.BlueLight
import com.example.screentime.ui.theme.TextPrimary

@Composable
fun AppHeader(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = BlueContainer.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, BlueLight.copy(alpha = 0.35f)),
            modifier = Modifier.size(36.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Screen Time Logo",
                    tint = BlueLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Screen Time",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
    }
}
