package com.example.screentime.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.screentime.repository.SessionUiItem
import com.example.screentime.ui.theme.BlueContainer
import com.example.screentime.ui.theme.BlueLight
import com.example.screentime.ui.theme.BluePrimary
import com.example.screentime.ui.theme.DarkBackground
import com.example.screentime.ui.theme.DarkBorder
import com.example.screentime.ui.theme.DarkSurface
import com.example.screentime.ui.theme.DarkSurfaceVariant
import com.example.screentime.ui.theme.GreenActive
import com.example.screentime.ui.theme.GreenUsage
import com.example.screentime.ui.theme.GreenUsageContainer
import com.example.screentime.ui.theme.OnRedErrorContainer
import com.example.screentime.ui.theme.RedError
import com.example.screentime.ui.theme.RedErrorContainer
import com.example.screentime.ui.theme.RedUsage
import com.example.screentime.ui.theme.RedUsageContainer
import com.example.screentime.ui.theme.TextMuted
import com.example.screentime.ui.theme.TextPrimary
import com.example.screentime.ui.theme.TextSecondary
import com.example.screentime.ui.theme.YellowUsage
import com.example.screentime.ui.theme.YellowUsageContainer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ScreenTimeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // App Header: Minimalist Title with Timer Logo Badge
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
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

            // Just below app name: Row with Status Chip (Left) and Buttons Together (Right)
            item {
                StatusAndActionsRow(
                    isTracking = state.isTrackingEnabled,
                    isScreenActive = state.activeSession != null,
                    onReset = { showResetConfirmDialog = true },
                    onToggleTracking = { viewModel.toggleTracking() }
                )
            }

            // Notification Permission Banner for Android 13+
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    NotificationPermissionBanner(
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                }
            }

            // Interruption Alert Banner
            if (state.isInterrupted) {
                item {
                    InterruptionBanner(
                        message = state.interruptionMessage
                            ?: "Monitoring was interrupted. Some session records may be incomplete.",
                        onDismiss = { viewModel.dismissInterruption() }
                    )
                }
            }

            // Date Navigation Bar
            item {
                DateNavigationBar(
                    selectedDate = state.selectedDate,
                    isToday = state.isToday,
                    isNextEnabled = state.isNextDayEnabled,
                    onPreviousDay = { viewModel.onPreviousDay() },
                    onNextDay = { viewModel.onNextDay() },
                    onToday = { viewModel.onToday() }
                )
            }

            // Daily Summary Cards (Total Sessions & Total Screen Time)
            item {
                DailySummaryCards(
                    totalSessions = state.daySummary?.totalSessionsCount ?: 0,
                    totalScreenTime = state.daySummary?.formattedTotalDuration ?: "00m 00s"
                )
            }

            // Session History Header
            item {
                Text(
                    text = "Session History",
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Session History Table Header
            item {
                SessionTableHeader(
                    sortColumn = state.sortColumn,
                    sortOrder = state.sortOrder,
                    onSortColumnClick = { viewModel.onSortColumnClick(it) }
                )
            }

            // Session History Table Rows or Empty State
            val sessions = state.sortedSessions
            if (sessions.isEmpty()) {
                item {
                    EmptySessionsView()
                }
            } else {
                items(
                    items = sessions,
                    key = { it.id }
                ) { session ->
                    SessionTableRow(session = session)
                    HorizontalDivider(
                        color = DarkBorder.copy(alpha = 0.6f),
                        thickness = 0.5.dp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showResetConfirmDialog) {
        val dateLabel = if (state.isToday) "today" else "the selected day (${state.selectedDate})"
        AlertDialog(
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            onDismissRequest = { showResetConfirmDialog = false },
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
                    onClick = {
                        viewModel.resetSelectedDay()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedError,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Reset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            }
        )
    }
}

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
                .padding(horizontal = 14.dp, vertical = 8.dp),
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
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
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
