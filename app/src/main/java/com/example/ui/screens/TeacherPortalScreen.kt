package com.example.ui.screens

import java.util.Calendar
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.scheduling.ScheduleEngine
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun TeacherPortalScreen(viewModel: MainViewModel) {
    val teachersList by viewModel.teachers.collectAsState()
    val cells by viewModel.timetableCells.collectAsState()
    val subjectsList by viewModel.subjects.collectAsState()
    val classroomsList by viewModel.classrooms.collectAsState()
    val leavesList by viewModel.leaveRequests.collectAsState()
    val activeTeacher by viewModel.selectedPortalTeacher.collectAsState()
    val substitutesList by viewModel.substitutes.collectAsState()

    var showApplyLeaveDialog by remember { mutableStateOf(false) }
    var leaveNotes by remember { mutableStateOf("") }
    var leaveScope by remember { mutableStateOf("All Week") }
    var showFullTimeline by remember { mutableStateOf(false) }

    val todayDayName = remember {
        val calendar = Calendar.getInstance()
        calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US)
    }

    val periodTimes = listOf(
        "09:00 AM - 10:00 AM",
        "10:00 AM - 11:00 AM",
        "11:00 AM - 12:00 PM",
        "12:00 PM - 01:00 PM",
        "01:00 PM - 02:00 PM",
        "02:00 PM - 03:00 PM"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Teacher Portal Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(top = 16.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Teacher Companion Portal", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text("Access personal schedules, substitute duties and leave applications.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            if (activeTeacher != null) {
                IconButton(onClick = { showApplyLeaveDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Apply Leave", tint = AccentVioletLight)
                }
            }
        }

        // Horizontal scrolling selector of Teachers as simple "Log In" switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Logged In as:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            teachersList.forEach { teacher ->
                val isSel = activeTeacher?.id == teacher.id
                FilterChip(
                    selected = isSel,
                    onClick = { viewModel.selectPortalTeacher(teacher) },
                    label = { Text(teacher.name.split(" ").lastOrNull() ?: teacher.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentViolet,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (activeTeacher == null) {
            // Empty state if no teachers exist
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Please register teaching instructors inside the Admin panel first.", color = TextSecondary)
            }
        } else {
            val teacherId = activeTeacher!!.id
            val teacherCells = cells.filter { it.teacherId == teacherId }
            val pendingLeaves = leavesList.filter { it.teacherId == teacherId }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Statistics Summary Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Weekly Class Load Summary",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${teacherCells.size} periods active",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentVioletLight
                                    )
                                    Text(
                                        text = "Max Allowable Limit: ${activeTeacher!!.maxHoursPerWeek} hrs",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                // Linear progress bar representation
                                LinearProgressIndicator(
                                    progress = { (teacherCells.size.toFloat() / activeTeacher!!.maxHoursPerWeek.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .width(130.dp)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = AccentViolet,
                                    trackColor = DarkBorder
                                )
                            }
                        }
                    }
                }

                // Leaves notifications indicators
                if (pendingLeaves.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Absence Applications Logs", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Spacer(modifier = Modifier.height(6.dp))
                                pendingLeaves.forEach { leave ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(leave.notes, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (leave.status == "Approved") AccentEmerald.copy(alpha = 0.2f)
                                                    else AccentAmber.copy(alpha = 0.2f)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(leave.status, style = MaterialTheme.typography.labelSmall, color = if (leave.status == "Approved") AccentEmerald else AccentAmber)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Schedule List by Days
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Your Weekly Schedule",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (showFullTimeline) "Detailed chronological outlook with gaps" else "Compact active hours representation",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        // Toggle switch for compact vs chronology
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (!showFullTimeline) AccentViolet.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { showFullTimeline = false }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Compact",
                                    color = if (!showFullTimeline) AccentVioletLight else TextSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (showFullTimeline) AccentViolet.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { showFullTimeline = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Chronology",
                                    color = if (showFullTimeline) AccentVioletLight else TextSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (teacherCells.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "You are currently free this entire cycle! No active lecture slots are allocated to you in the master timetable.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    items(ScheduleEngine.WORKING_DAYS) { day ->
                        val dayCells = teacherCells.filter { it.day == day }.sortedBy { it.periodIndex }
                        val isToday = day.equals(todayDayName, ignoreCase = true)

                        if (showFullTimeline || dayCells.isNotEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isToday) DarkSurface.copy(alpha = 0.9f) else DarkSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        if (isToday) 1.5.dp else 1.dp,
                                        if (isToday) AccentVioletLight.copy(alpha = 0.6f) else DarkBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isToday) AccentVioletLight else TextPrimary
                                        )
                                        if (isToday) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(AccentViolet.copy(alpha = 0.25f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "TODAY 📍",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = AccentVioletLight,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (showFullTimeline) {
                                            // Render complete sequence
                                            (0 until ScheduleEngine.NUM_PERIODS).forEach { periodIdx ->
                                                val isLunch = periodIdx == ScheduleEngine.LUNCH_PERIOD
                                                if (isLunch) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(DarkBorder.copy(alpha = 0.2f))
                                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.Center,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Info, contentDescription = "Break", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("12:00 PM - 01:00 PM • Midday Lunch Break", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                                    }
                                                } else {
                                                    val cell = dayCells.find { it.periodIndex == periodIdx }
                                                    if (cell != null) {
                                                        val subject = subjectsList.find { it.id == cell.subjectId }
                                                        val room = classroomsList.find { it.id == cell.classroomId }
                                                        val isCoverDuty = substitutesList.any { it.cellId == cell.id && it.substituteTeacherId == teacherId }

                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(if (isCoverDuty) AccentAmber.copy(alpha = 0.12f) else DarkBg)
                                                                .border(
                                                                    width = if (isCoverDuty) 1.dp else 0.dp,
                                                                    color = if (isCoverDuty) AccentAmber.copy(alpha = 0.3f) else Color.Transparent,
                                                                    shape = RoundedCornerShape(8.dp)
                                                                )
                                                                .padding(10.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Text(
                                                                        text = "Period ${cell.periodIndex + 1} (${periodTimes[cell.periodIndex].split(" ").first()})",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = if (isCoverDuty) AccentAmber else AccentVioletLight,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                    if (isCoverDuty) {
                                                                        Spacer(modifier = Modifier.width(6.dp))
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .clip(RoundedCornerShape(3.dp))
                                                                                .background(AccentAmber.copy(alpha = 0.2f))
                                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                        ) {
                                                                            Text("COVER DUTY ⚠️", style = MaterialTheme.typography.labelSmall, color = AccentAmber, fontWeight = FontWeight.Bold)
                                                                        }
                                                                    }
                                                                }
                                                                Text(
                                                                    text = subject?.name ?: "Lecture slot",
                                                                    style = MaterialTheme.typography.bodyMedium,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = TextPrimary
                                                                )
                                                                Text(
                                                                    text = "Section: ${cell.section} • Location: ${room?.name ?: "Main Room"}",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = TextSecondary
                                                                )
                                                            }

                                                            if (subject?.isLab == true) {
                                                                Icon(Icons.Default.Build, contentDescription = "Lab icon", tint = AccentVioletLight)
                                                            }
                                                        }
                                                    } else {
                                                        // Free period representation
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(DarkBg.copy(alpha = 0.5f))
                                                                .border(1.dp, DarkBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                                .padding(10.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(Icons.Default.Info, contentDescription = "Free period", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(10.dp))
                                                            Column {
                                                                Text(
                                                                    text = "Period ${periodIdx + 1} (${periodTimes[periodIdx].split(" ").first()}) • Free Window ☕",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = TextSecondary,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                                Text(
                                                                    text = "No assigned lecture. Perfect for grading, preparation, or meetings.",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = TextSecondary
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            // Standard compact view
                                            dayCells.forEach { cell ->
                                                val subject = subjectsList.find { it.id == cell.subjectId }
                                                val room = classroomsList.find { it.id == cell.classroomId }
                                                val isCoverDuty = substitutesList.any { it.cellId == cell.id && it.substituteTeacherId == teacherId }

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isCoverDuty) AccentAmber.copy(alpha = 0.12f) else DarkBg)
                                                        .border(
                                                            width = if (isCoverDuty) 1.dp else 0.dp,
                                                            color = if (isCoverDuty) AccentAmber.copy(alpha = 0.3f) else Color.Transparent,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = "Period ${cell.periodIndex + 1} (${periodTimes[cell.periodIndex].split(" ").first()})",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = if (isCoverDuty) AccentAmber else AccentVioletLight,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            if (isCoverDuty) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(3.dp))
                                                                        .background(AccentAmber.copy(alpha = 0.2f))
                                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                ) {
                                                                    Text("COVER DUTY ⚠️", style = MaterialTheme.typography.labelSmall, color = AccentAmber, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }
                                                        Text(
                                                            text = subject?.name ?: "Lecture slot",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TextPrimary
                                                        )
                                                        Text(
                                                            text = "Section: ${cell.section} • Location: ${room?.name ?: "Main Room"}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = TextSecondary
                                                        )
                                                    }

                                                    if (subject?.isLab == true) {
                                                        Icon(Icons.Default.Build, contentDescription = "Lab icon", tint = AccentVioletLight)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Apply Absence Form Dialog
        if (showApplyLeaveDialog && activeTeacher != null) {
            AlertDialog(
                onDismissRequest = { showApplyLeaveDialog = false },
                title = { Text("Submit Absence Request", color = TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Applying as: ${activeTeacher!!.name}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text(
                            text = "Choose whether you are absent for the entire weekly cycle or for a specific day. Substitutes will only be drafted for the affected periods.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Text("Select Absence Period Scope", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            (listOf("All Week") + ScheduleEngine.WORKING_DAYS).forEach { scope ->
                                val isSel = leaveScope == scope
                                FilterChip(
                                    selected = isSel,
                                    onClick = { leaveScope = scope },
                                    label = { Text(scope, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentViolet,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = leaveNotes,
                            onValueChange = { leaveNotes = it },
                            placeholder = { Text("Family event, Medical, Research...", color = TextMuted) },
                            label = { Text("Absence Reason", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentVioletLight,
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
                            if (leaveNotes.isNotBlank()) {
                                viewModel.applyForLeave(activeTeacher!!.id, start = leaveScope, end = leaveScope, notes = leaveNotes)
                                leaveNotes = ""
                                showApplyLeaveDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentViolet)
                    ) {
                        Text("Apply Request", color = Color.White)
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
                onClick = { viewModel.navigateTo(Screen.ConflictCenter) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                Text("Resolutions", color = TextPrimary)
            }

            Button(
                onClick = { viewModel.navigateTo(Screen.Analytics) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
            ) {
                Text("Resource Analytics", color = Color.White)
                Icon(Icons.Default.ArrowForward, contentDescription = "Continue", tint = Color.White)
            }
        }
    }
}
