package com.example.screentime.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.screentime.ui.theme.BlueContainer
import com.example.screentime.ui.theme.BlueLight
import com.example.screentime.ui.theme.DarkSurface
import com.example.screentime.ui.theme.OnRedErrorContainer
import com.example.screentime.ui.theme.RedError
import com.example.screentime.ui.theme.RedErrorContainer
import com.example.screentime.ui.theme.TextPrimary
import com.example.screentime.ui.theme.TextSecondary
import java.time.LocalDate

@Composable
fun ResetConfirmationDialog(
    selectedDate: LocalDate,
    isToday: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateLabel = if (isToday) "today" else "the selected day ($selectedDate)"
    AlertDialog(
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.RestartAlt,
                contentDescription = null,
                tint = RedError,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(text = "Reset Day Sessions?", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text = "This will clear all recorded screen time sessions for $dateLabel. This action cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedError,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Reset", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun NotificationPermissionBanner(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, BlueLight.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = BlueLight,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notification Permission Needed",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Required for background monitoring service to remain active.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FilledTonalButton(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = BlueContainer,
                        contentColor = BlueLight
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(text = "Grant Permission", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun InterruptionBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = RedErrorContainer,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, RedError.copy(alpha = 0.3f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = OnRedErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Monitoring Interrupted",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnRedErrorContainer
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnRedErrorContainer.copy(alpha = 0.8f)
                )
            }
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Dismiss",
                    color = OnRedErrorContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
