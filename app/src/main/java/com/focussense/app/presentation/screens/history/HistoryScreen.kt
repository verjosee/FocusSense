package com.focussense.app.presentation.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.focussense.app.domain.model.FocusSession
import com.focussense.app.domain.model.SessionStatus
import com.focussense.app.presentation.components.FocusIcons
import com.focussense.app.presentation.components.SessionItemCard
import com.focussense.app.presentation.viewmodel.HistoryUiState
import com.focussense.app.presentation.viewmodel.HistoryViewModel
import com.focussense.app.ui.theme.ErrorRed
import com.focussense.app.ui.theme.LocalExtendedColors
import com.focussense.app.ui.theme.SoftViolet

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    HistoryScreenContent(
        uiState = uiState,
        onFilterSelected = { viewModel.setFilter(it) },
        onEditNotes = { viewModel.openEditNotesDialog(it) },
        onNotesChanged = { viewModel.onNotesTextChanged(it) },
        onSaveNotes = { viewModel.saveEditedNotes() },
        onDismissEditNotes = { viewModel.dismissEditNotesDialog() },
        onDeleteSession = { viewModel.openDeleteConfirmation(it) },
        onConfirmDelete = { viewModel.confirmDeleteSession() },
        onDismissDelete = { viewModel.dismissDeleteConfirmation() },
        modifier = modifier
    )
}

@Composable
fun HistoryScreenContent(
    uiState: HistoryUiState,
    onFilterSelected: (SessionStatus?) -> Unit,
    onEditNotes: (FocusSession) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSaveNotes: () -> Unit,
    onDismissEditNotes: () -> Unit,
    onDeleteSession: (FocusSession) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = LocalExtendedColors.current.surfaceBorder

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Header
                Column {
                    Text(
                        text = "Session History",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Review, annotate, and manage your focus records",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedFilter == null,
                        onClick = { onFilterSelected(null) },
                        label = { Text("All (${uiState.sessions.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SoftViolet.copy(alpha = 0.2f),
                            selectedLabelColor = SoftViolet
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    FilterChip(
                        selected = uiState.selectedFilter == SessionStatus.COMPLETED,
                        onClick = { onFilterSelected(SessionStatus.COMPLETED) },
                        label = { Text("Completed") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SoftViolet.copy(alpha = 0.2f),
                            selectedLabelColor = SoftViolet
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    FilterChip(
                        selected = uiState.selectedFilter == SessionStatus.INTERRUPTED,
                        onClick = { onFilterSelected(SessionStatus.INTERRUPTED) },
                        label = { Text("Interrupted") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SoftViolet.copy(alpha = 0.2f),
                            selectedLabelColor = SoftViolet
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Sessions List or Empty State
            if (uiState.filteredSessions.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = FocusIcons.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (uiState.sessions.isEmpty()) "No focus sessions recorded yet" else "No sessions matching filter",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (uiState.sessions.isEmpty()) {
                                    "Complete your first focus session to build your study track record."
                                } else {
                                    "Select another filter to view your sessions."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.filteredSessions, key = { it.id }) { session ->
                    SessionItemCard(
                        session = session,
                        onEditNotes = onEditNotes,
                        onDelete = onDeleteSession
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Edit Notes Dialog
        if (uiState.sessionToEditNotes != null) {
            AlertDialog(
                onDismissRequest = onDismissEditNotes,
                shape = RoundedCornerShape(22.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = "Edit Session Notes",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    OutlinedTextField(
                        value = uiState.editedNotesText,
                        onValueChange = onNotesChanged,
                        label = { Text("Notes") },
                        placeholder = { Text("Reflect on what was achieved or learned...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 4
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onSaveNotes,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissEditNotes) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (uiState.sessionToDelete != null) {
            AlertDialog(
                onDismissRequest = onDismissDelete,
                shape = RoundedCornerShape(22.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Text(
                        text = "Delete Session?",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete this focus session record? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDelete) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
