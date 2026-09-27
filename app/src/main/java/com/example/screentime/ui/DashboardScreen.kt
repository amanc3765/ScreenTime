package com.example.screentime.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.screentime.ui.components.AppHeader
import com.example.screentime.ui.components.DailySummaryCards
import com.example.screentime.ui.components.DateNavigationBar
import com.example.screentime.ui.components.EmptySessionsView
import com.example.screentime.ui.components.InterruptionBanner
import com.example.screentime.ui.components.NotificationPermissionBanner
import com.example.screentime.ui.components.ResetConfirmationDialog
import com.example.screentime.ui.components.SessionTableHeader
import com.example.screentime.ui.components.SessionTableRow
import com.example.screentime.ui.components.StatusAndActionsRow
import com.example.screentime.ui.theme.DarkBackground
import com.example.screentime.ui.theme.DarkBorder
import com.example.screentime.ui.theme.TextPrimary

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
            // App Header with Timer Logo
            item {
                AppHeader()
            }

            // Status Chip & Action Controls (Reset & Pause/Resume)
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

            // Session History Section Header
            item {
                Text(
                    text = "Session History",
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Session History Table Header with interactive sorting
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
        ResetConfirmationDialog(
            selectedDate = state.selectedDate,
            isToday = state.isToday,
            onConfirm = {
                viewModel.resetSelectedDay()
                showResetConfirmDialog = false
            },
            onDismiss = { showResetConfirmDialog = false }
        )
    }
}
