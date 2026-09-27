package com.example.screentime.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentime.ui.theme.BlueContainer
import com.example.screentime.ui.theme.BlueLight
import com.example.screentime.ui.theme.DarkBorder
import com.example.screentime.ui.theme.DarkSurface
import com.example.screentime.ui.theme.TextMuted
import com.example.screentime.ui.theme.TextPrimary
import com.example.screentime.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DateNavigationBar(
    selectedDate: LocalDate,
    isToday: Boolean,
    isNextEnabled: Boolean,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(selectedDate) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        when (selectedDate) {
            today -> "Today, " + selectedDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))
            yesterday -> "Yesterday, " + selectedDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))
            else -> selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US))
        }
    }

    Surface(
        color = DarkSurface,
        border = BorderStroke(1.dp, DarkBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPreviousDay) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Day",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (!isToday) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = BlueContainer,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    ) {
                        TextButton(
                            onClick = onToday,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BlueLight
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onNextDay,
                enabled = isNextEnabled
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Day",
                    tint = if (isNextEnabled) TextSecondary else TextMuted.copy(alpha = 0.3f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DailySummaryCards(
    totalSessions: Int,
    totalScreenTime: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Total Sessions Card
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "SESSIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format(Locale.US, "%02d", totalSessions),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "today",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Total Screen Time Card
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, BlueLight.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "SCREEN TIME",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BlueLight,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = totalScreenTime,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = BlueLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "active usage",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
