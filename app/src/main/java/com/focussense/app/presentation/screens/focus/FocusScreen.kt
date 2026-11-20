package com.focussense.app.presentation.screens.focus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focussense.app.domain.timer.TimerMode
import com.focussense.app.presentation.components.CircularProgressTimer
import com.focussense.app.presentation.components.FocusIcons
import com.focussense.app.presentation.viewmodel.FocusTimerUiState
import com.focussense.app.presentation.viewmodel.FocusTimerViewModel
import com.focussense.app.ui.theme.ErrorRed
import com.focussense.app.ui.theme.LocalExtendedColors
import com.focussense.app.ui.theme.SkyBlue
import com.focussense.app.ui.theme.SoftViolet
import com.focussense.app.ui.theme.SuccessGreen
import com.focussense.app.ui.theme.WarningAmber
import java.util.Locale

@Composable
fun FocusScreen(
    viewModel: FocusTimerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    FocusScreenContent(
        uiState = uiState,
        onSelectDuration = { viewModel.selectDuration(it) },
        onStart = { viewModel.startTimer() },
        onPause = { viewModel.pauseTimer() },
        onResume = { viewModel.resumeTimer() },
        onStop = { viewModel.stopTimer(saveInterrupted = true) },
        onReset = { viewModel.resetTimer() },
        onNotesChanged = { viewModel.onNotesChanged(it) },
        onSaveSession = { viewModel.savePendingSession() },
        onDiscardSession = { viewModel.discardPendingSession() },
        onDismissMotionAlert = { viewModel.dismissMotionAlert() },
        modifier = modifier
    )
}

@Composable
fun FocusScreenContent(
    uiState: FocusTimerUiState,
    onSelectDuration: (Int) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onReset: () -> Unit,
    onNotesChanged: (String) -> Unit,
    onSaveSession: () -> Unit,
    onDiscardSession: () -> Unit,
    onDismissMotionAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timerState = uiState.timerState
    val isRunning = timerState.isRunning
    val isPaused = timerState.isPaused
    val isIdle = timerState.isIdle
    val isCompleted = timerState.isCompleted

    val statusText = when (timerState.mode) {
        TimerMode.IDLE -> "Ready to Focus"
        TimerMode.RUNNING -> "Deep Focus"
        TimerMode.PAUSED -> "Session Paused"
        TimerMode.COMPLETED -> "Session Complete!"
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Motion Alert Banner
            AnimatedVisibility(
                visible = timerState.isMotionAlertActive,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(1.dp, WarningAmber, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = FocusIcons.Warning,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Device Movement Detected",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = WarningAmber
                                )
                                Text(
                                    text = "Try placing your phone down to avoid distractions.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismissMotionAlert,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = FocusIcons.Close,
                                contentDescription = "Dismiss",
                                tint = WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Duration Selector Chips (Active only in IDLE mode)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                uiState.availableDurations.forEach { duration ->
                    val isSelected = uiState.selectedDurationMinutes == duration
                    FilterChip(
                        selected = isSelected,
                        onClick = { if (isIdle) onSelectDuration(duration) },
                        label = {
                            Text(
                                text = "${duration}m",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        enabled = isIdle,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Circular Countdown Progress Timer
            CircularProgressTimer(
                progress = timerState.progress,
                timeDisplay = timerState.formattedRemainingTime,
                statusText = statusText,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Timer Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isIdle -> {
                        Button(
                            onClick = onStart,
                            modifier = Modifier
                                .height(56.dp)
                                .fillMaxWidth(0.7f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(
                                imageVector = FocusIcons.Play,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Focus",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    isRunning -> {
                        // Pause Button
                        Button(
                            onClick = onPause,
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftViolet),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(
                                imageVector = FocusIcons.Pause,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pause", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Stop Button
                        OutlinedButton(
                            onClick = onStop,
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(
                                imageVector = FocusIcons.Stop,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stop", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    isPaused -> {
                        // Resume Button
                        Button(
                            onClick = onResume,
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(
                                imageVector = FocusIcons.Play,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Resume", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Stop / Finish Button
                        OutlinedButton(
                            onClick = onStop,
                            modifier = Modifier
                                .height(56.dp)
                                .weight(1f),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(
                                imageVector = FocusIcons.Stop,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Finish", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    isCompleted -> {
                        Button(
                            onClick = onReset,
                            modifier = Modifier
                                .height(56.dp)
                                .fillMaxWidth(0.7f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(
                                imageVector = FocusIcons.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("New Session", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Realtime Sensor Info Card
            if (!isIdle) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Movements",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${timerState.movementCount}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (timerState.movementCount > 0) WarningAmber else SuccessGreen
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(30.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Ambient Light",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (timerState.currentLux != null) {
                                    String.format(Locale.US, "%.0f lx", timerState.currentLux)
                                } else {
                                    "Stable"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SkyBlue
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(30.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Elapsed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = timerState.formattedElapsedTime,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Completion / Stop Session Save Dialog with Notes
        if (uiState.showSaveDialog && uiState.pendingSession != null) {
            val session = uiState.pendingSession
            AlertDialog(
                onDismissRequest = onDiscardSession,
                shape = RoundedCornerShape(22.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = if (session.status.name == "COMPLETED") "Focus Session Completed!" else "Session Ended",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Focused for ${session.formattedDuration} with ${session.movementCount} movement distractions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = uiState.sessionNotes,
                            onValueChange = onNotesChanged,
                            label = { Text("Session Notes (optional)") },
                            placeholder = { Text("What did you accomplish?") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onSaveSession,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Session", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDiscardSession) {
                        Text("Discard", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}
