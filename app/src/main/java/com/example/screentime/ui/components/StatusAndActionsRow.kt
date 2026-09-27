package com.example.screentime.ui.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.screentime.ui.theme.BluePrimary
import com.example.screentime.ui.theme.DarkBorder
import com.example.screentime.ui.theme.DarkSurface
import com.example.screentime.ui.theme.DarkSurfaceVariant
import com.example.screentime.ui.theme.GreenActive
import com.example.screentime.ui.theme.OnRedErrorContainer
import com.example.screentime.ui.theme.RedError
import com.example.screentime.ui.theme.RedErrorContainer
import com.example.screentime.ui.theme.TextMuted
import com.example.screentime.ui.theme.TextPrimary
import com.example.screentime.ui.theme.TextSecondary

/**
 * Clean unified row just below app name:
 * Status Chip with dot (Left) and Buttons Together (Right: Reset & Pause/Resume).
 */
@Composable
fun StatusAndActionsRow(
    isTracking: Boolean,
    isScreenActive: Boolean,
    onReset: () -> Unit,
    onToggleTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status Chip with dot
        StatusDotChip(
            isTracking = isTracking,
            isScreenActive = isScreenActive
        )

        // Buttons Together
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Reset Button (Red)
            FilledTonalButton(
                onClick = onReset,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = RedErrorContainer,
                    contentColor = OnRedErrorContainer
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, RedError.copy(alpha = 0.25f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset",
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Reset",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Pause / Resume Button
            if (isTracking) {
                FilledTonalButton(
                    onClick = onToggleTracking,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DarkSurfaceVariant,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pause",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Button(
                    onClick = onToggleTracking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Resume",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Resume",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Minimalist status chip with a dot and text (Active or Paused).
 */
@Composable
fun StatusDotChip(
    isTracking: Boolean,
    isScreenActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            if (isTracking) {
                if (isScreenActive) {
                    PulsingGreenDot()
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GreenActive)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Active",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GreenActive
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(TextMuted)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Paused",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun PulsingGreenDot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotColor by infiniteTransition.animateColor(
        initialValue = GreenActive,
        targetValue = GreenActive.copy(alpha = 0.25f),
        animationSpec = infiniteRepeatable(
            animation = tween(750),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color"
    )

    Box(
        modifier = modifier
            .size(9.dp)
            .clip(CircleShape)
            .background(dotColor)
    )
}
