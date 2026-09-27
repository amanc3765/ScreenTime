package com.example.screentime.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentime.repository.SessionUiItem
import com.example.screentime.ui.SessionSortColumn
import com.example.screentime.ui.SortOrder
import com.example.screentime.ui.theme.BlueContainer
import com.example.screentime.ui.theme.BlueLight
import com.example.screentime.ui.theme.DarkBorder
import com.example.screentime.ui.theme.DarkSurface
import com.example.screentime.ui.theme.DarkSurfaceVariant
import com.example.screentime.ui.theme.GreenUsage
import com.example.screentime.ui.theme.GreenUsageContainer
import com.example.screentime.ui.theme.RedUsage
import com.example.screentime.ui.theme.RedUsageContainer
import com.example.screentime.ui.theme.TextMuted
import com.example.screentime.ui.theme.TextPrimary
import com.example.screentime.ui.theme.TextSecondary
import com.example.screentime.ui.theme.YellowUsage
import com.example.screentime.ui.theme.YellowUsageContainer

@Composable
fun SessionTableHeader(
    sortColumn: SessionSortColumn,
    sortOrder: SortOrder,
    onSortColumnClick: (SessionSortColumn) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index column (#)
            Text(
                text = "#",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                modifier = Modifier.weight(0.10f)
            )

            // START TIME Column
            SortableHeaderCell(
                title = "START",
                column = SessionSortColumn.START_TIME,
                currentSortColumn = sortColumn,
                sortOrder = sortOrder,
                onClick = { onSortColumnClick(SessionSortColumn.START_TIME) },
                modifier = Modifier.weight(0.32f),
                alignment = Alignment.Start
            )

            // END TIME Column
            SortableHeaderCell(
                title = "END",
                column = SessionSortColumn.END_TIME,
                currentSortColumn = sortColumn,
                sortOrder = sortOrder,
                onClick = { onSortColumnClick(SessionSortColumn.END_TIME) },
                modifier = Modifier.weight(0.32f),
                alignment = Alignment.Start
            )

            // DURATION Column
            SortableHeaderCell(
                title = "DURATION",
                column = SessionSortColumn.DURATION,
                currentSortColumn = sortColumn,
                sortOrder = sortOrder,
                onClick = { onSortColumnClick(SessionSortColumn.DURATION) },
                modifier = Modifier.weight(0.26f),
                alignment = Alignment.End
            )
        }
    }
}

@Composable
fun SortableHeaderCell(
    title: String,
    column: SessionSortColumn,
    currentSortColumn: SessionSortColumn,
    sortOrder: SortOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    val isSelected = column == currentSortColumn
    val contentColor = if (isSelected) BlueLight else TextMuted

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (alignment == Alignment.End) Arrangement.End else Arrangement.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(2.dp))
        if (isSelected) {
            Icon(
                imageVector = if (sortOrder == SortOrder.ASCENDING) {
                    Icons.Default.ArrowDropUp
                } else {
                    Icons.Default.ArrowDropDown
                },
                contentDescription = if (sortOrder == SortOrder.ASCENDING) "Ascending" else "Descending",
                tint = BlueLight,
                modifier = Modifier.size(16.dp)
            )
        } else {
            // Subtle placeholder indicator icon
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = TextMuted.copy(alpha = 0.35f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun SessionTableRow(
    session: SessionUiItem,
    modifier: Modifier = Modifier
) {
    val rowBackground = if (session.isActive) {
        BlueContainer.copy(alpha = 0.35f)
    } else {
        Color.Transparent
    }

    // Color code sessions based on duration:
    // < 5 minutes (< 300,000 ms)  -> Low usage: Green
    // 5 to 15 minutes (300,000 - 900,000 ms) -> Medium usage: Yellow
    // > 15 minutes (> 900,000 ms) -> High usage: Red
    val durationColor = when {
        session.rawDurationMillis < 5 * 60 * 1000L -> GreenUsage
        session.rawDurationMillis <= 15 * 60 * 1000L -> YellowUsage
        else -> RedUsage
    }

    val durationContainer = when {
        session.rawDurationMillis < 5 * 60 * 1000L -> GreenUsageContainer
        session.rawDurationMillis <= 15 * 60 * 1000L -> YellowUsageContainer
        else -> RedUsageContainer
    }

    Surface(
        color = rowBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sequence Number
            Text(
                text = "${session.sequenceNumber}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (session.isActive) BlueLight else TextMuted,
                modifier = Modifier.weight(0.10f)
            )

            // Start Time
            Text(
                text = session.startTimeFormatted,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.weight(0.32f)
            )

            // End Time or "In progress" badge
            Box(modifier = Modifier.weight(0.32f)) {
                if (session.isActive) {
                    Surface(
                        color = BlueLight.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, BlueLight.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "In progress",
                            style = MaterialTheme.typography.labelSmall,
                            color = BlueLight,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Text(
                        text = session.endTimeFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Duration color-coded: <5m Green, 5m-15m Yellow, >15m Red
            Row(
                modifier = Modifier.weight(0.26f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = durationContainer,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, durationColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = session.durationFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = durationColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySessionsView(modifier: Modifier = Modifier) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Smartphone,
                contentDescription = null,
                tint = TextMuted.copy(alpha = 0.4f),
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No sessions recorded",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sessions for this day will appear automatically as the screen turns on and off.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
