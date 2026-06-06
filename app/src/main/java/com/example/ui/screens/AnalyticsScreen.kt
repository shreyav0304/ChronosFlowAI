package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun AnalyticsScreen(viewModel: MainViewModel) {
    val teachersList by viewModel.teachers.collectAsState()
    val classroomsList by viewModel.classrooms.collectAsState()
    val cells by viewModel.timetableCells.collectAsState()
    val subjectsList by viewModel.subjects.collectAsState()

    val score by viewModel.optimizationScore.collectAsState()
    val balance by viewModel.teacherBalanceScore.collectAsState()
    val utilization by viewModel.classroomUtilizationScore.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Analytics Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(top = 16.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Resource Analytics", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text("Analyze workloads, lab bookings and resource metrics.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Efficiency gauge card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "Speed profile", tint = AccentTealLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Overall Operational Efficiency", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Efficiency Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkBg),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text("$score%", style = MaterialTheme.typography.titleLarge, color = AccentEmerald, fontWeight = FontWeight.Bold)
                                    Text("Schedule Health", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkBg),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text("$balance%", style = MaterialTheme.typography.titleLarge, color = AccentTealLight, fontWeight = FontWeight.Bold)
                                    Text("Staff Fairness", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkBg),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text("$utilization%", style = MaterialTheme.typography.titleLarge, color = AccentVioletLight, fontWeight = FontWeight.Bold)
                                    Text("Booking Rate", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Instructor Workload distribution chart
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.List, contentDescription = "Load distribution", tint = AccentVioletLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Teacher Workload Balance", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        }
                        Text("Periods per week allocated. Evaluates load fairness.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(bottom = 14.dp))

                        if (teachersList.isEmpty()) {
                            Text("No staff registered.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                teachersList.forEach { teacher ->
                                    val teacherLoad = cells.count { it.teacherId == teacher.id }
                                    val loadRatio = (teacherLoad.toFloat() / teacher.maxHoursPerWeek.toFloat()).coerceIn(0f, 1f)

                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(teacher.name, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                            Text("$teacherLoad / ${teacher.maxHoursPerWeek} hrs", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { loadRatio },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = if (teacherLoad <= teacher.maxHoursPerWeek) AccentVioletLight else AccentRose,
                                            trackColor = DarkBorder
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Lab/Room utilization status index reports
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = "Resource booking", tint = AccentTealLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Classroom & Lab Utilization", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        }
                        Text("Usage rate of lecture rooms and tech labs.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(bottom = 14.dp))

                        if (classroomsList.isEmpty()) {
                            Text("No resources configured.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                classroomsList.forEach { room ->
                                    // Total available units per day * week
                                    val totalRoomCaps = 5 * 5 // 5 days * 5 periods excluding lunch
                                    val roomAllocatedCount = cells.count { it.classroomId == room.id }
                                    val utilRatio = (roomAllocatedCount.toFloat() / totalRoomCaps.toFloat()).coerceIn(0f, 1f)

                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (room.isLab) Icons.Default.Build else Icons.Default.Place,
                                                    contentDescription = "Indicator",
                                                    tint = if (room.isLab) AccentVioletLight else AccentTealLight,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(room.name, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                            }
                                            Text("${(utilRatio * 100).toInt()}% occupancy", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { utilRatio },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = if (room.isLab) AccentViolet else AccentTeal,
                                            trackColor = DarkBorder
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
                onClick = { viewModel.navigateTo(Screen.TeacherPortal) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                Text("Teacher Portal", color = TextPrimary)
            }

            Button(
                onClick = { viewModel.navigateTo(Screen.Settings) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
            ) {
                Text("Settings & Export", color = Color.White)
                Icon(Icons.Default.ArrowForward, contentDescription = "Continue", tint = Color.White)
            }
        }
    }
}
