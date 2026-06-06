package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.example.data.*
import com.example.scheduling.ScheduleEngine
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun ConflictCenterScreen(viewModel: MainViewModel) {
    val liveConflicts by viewModel.liveConflicts.collectAsState()
    val leavesList by viewModel.leaveRequests.collectAsState()
    val substitutesList by viewModel.substitutes.collectAsState()
    val teachersList by viewModel.teachers.collectAsState()
    val cells by viewModel.timetableCells.collectAsState()
    val subjectsList by viewModel.subjects.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Clashes, 1: Leaves, 2: Substitutions Roster

    // Quick leave submission helper inside Admin panel
    var showApplyLeaveDialog by remember { mutableStateOf(false) }
    var selectedLeaveTeacherId by remember { mutableStateOf<Int?>(null) }
    var leaveNotes by remember { mutableStateOf("Academic Seminar Workshop") }
    var leaveScope by remember { mutableStateOf("All Week") }

    LaunchedEffect(teachersList) {
        if (teachersList.isNotEmpty() && selectedLeaveTeacherId == null) {
            selectedLeaveTeacherId = teachersList.first().id
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Core Banner Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(top = 16.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Conflict Resolution", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text("Manage overlaps, staff leave and substitution logs.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            if (activeTab == 1) {
                IconButton(onClick = { showApplyLeaveDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Leave Request", tint = AccentTealLight)
                }
            }
        }

        // Resolution Center Pivot Tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = DarkSurface,
            contentColor = AccentTealLight
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Active Clashes (${liveConflicts.size})", style = MaterialTheme.typography.bodySmall) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Faculty Leaves", style = MaterialTheme.typography.bodySmall) }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { activeTab = 2 },
                text = { Text("Substitutions", style = MaterialTheme.typography.bodySmall) }
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // TAP 0: ACTIVE SOLVER CONFLICT MONITORING
            if (activeTab == 0) {
                if (liveConflicts.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "All clean", tint = AccentEmerald, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No Active Scheduling Clashes!", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text("The current timetable complies perfectly with all hard resources limits.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(liveConflicts) { conflict ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AccentRose.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Clash alert", tint = AccentRose, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = conflict,
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // TAP 1: TEACHER LEAVE APPLICATIONS WITH AUTO SUBSTITUTES
            if (activeTab == 1) {
                if (leavesList.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "No leaves log", tint = TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No Faculty Leave Applications", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text("Apply for leaves below or in the Teacher specific panel to test automatic recalculation.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(leavesList) { leave ->
                        val teacher = teachersList.find { it.id == leave.teacherId }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(teacher?.name ?: "Unknown Teacher", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                        Text("Date: ${leave.startDate} to ${leave.endDate}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (leave.status) {
                                                    "Approved" -> AccentEmerald.copy(alpha = 0.2f)
                                                    "Rejected" -> AccentRose.copy(alpha = 0.2f)
                                                    else -> AccentAmber.copy(alpha = 0.2f)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = leave.status,
                                            color = when (leave.status) {
                                                "Approved" -> AccentEmerald
                                                "Rejected" -> AccentRose
                                                else -> AccentAmber
                                            },
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }

                                Text(
                                    text = "Reason: ${leave.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 10.dp)
                                )

                                if (leave.status == "Pending") {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.approveLeaveRequest(leave) },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1.2f)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Approve", modifier = Modifier.size(16.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve & Substitute", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                        }

                                        Button(
                                            onClick = { viewModel.rejectLeaveRequest(leave) },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(0.8f)
                                        ) {
                                            Text("Reject", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAP 2: SUBSTITUTIONS ROSTER
            if (activeTab == 2) {
                if (substitutesList.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "No substitutions", tint = TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Substitutions Roster Empty", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                Text("No substitution changes have been made recently. Approving a leave will generate these logs automatically.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(substitutesList) { sub ->
                        val subTeacher = teachersList.find { it.id == sub.substituteTeacherId }
                        val cell = cells.find { it.id == sub.cellId }
                        val origSubj = subjectsList.find { it.id == cell?.subjectId }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AccentViolet.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Substitute: ${subTeacher?.name ?: "Substitute Teacher"}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Assigned Class: ${origSubj?.name ?: "Lectures"} on ${cell?.day ?: "Monday"} Period ${(cell?.periodIndex ?: 0) + 1}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                IconButton(onClick = { viewModel.removeSubstitute(sub.cellId, sub.date) }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Rollback", tint = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Apply Quick Leave Dialog Form UI
        if (showApplyLeaveDialog) {
            AlertDialog(
                onDismissRequest = { showApplyLeaveDialog = false },
                title = { Text("Log Faculty Absence", color = TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Select Absent Faculty", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            teachersList.forEach { teacher ->
                                val isSel = selectedLeaveTeacherId == teacher.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) AccentViolet.copy(alpha = 0.2f) else DarkBg)
                                        .border(1.dp, if (isSel) AccentVioletLight else DarkBorder, RoundedCornerShape(8.dp))
                                        .clickable { selectedLeaveTeacherId = teacher.id }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(teacher.name.split(" ").lastOrNull() ?: teacher.name, color = if (isSel) TextPrimary else TextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Text("Select Leave Scope / Day", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            (listOf("All Week") + ScheduleEngine.WORKING_DAYS).forEach { scope ->
                                val isSel = leaveScope == scope
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) AccentTeal.copy(alpha = 0.2f) else DarkBg)
                                        .border(1.dp, if (isSel) AccentTealLight else DarkBorder, RoundedCornerShape(8.dp))
                                        .clickable { leaveScope = scope }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(scope, color = if (isSel) TextPrimary else TextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = leaveNotes,
                            onValueChange = { leaveNotes = it },
                            label = { Text("Leave Reason Notes", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentTealLight,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedLeaveTeacherId?.let { tid ->
                                viewModel.applyForLeave(tid, start = leaveScope, end = leaveScope, notes = leaveNotes)
                                showApplyLeaveDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                    ) {
                        Text("File Absence", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showApplyLeaveDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Cancel", color = TextPrimary)
                    }
                },
                containerColor = DarkSurface
            )
        }

        // Action continue checks
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { viewModel.navigateTo(Screen.Dashboard) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                Text("Schedules Dashboard", color = TextPrimary)
            }

            Button(
                onClick = { viewModel.navigateTo(Screen.TeacherPortal) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
            ) {
                Text("Teacher Portal", color = Color.White)
                Icon(Icons.Default.ArrowForward, contentDescription = "Continue", tint = Color.White)
            }
        }
    }
}
