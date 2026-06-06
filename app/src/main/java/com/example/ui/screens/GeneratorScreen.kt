package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun GeneratorScreen(viewModel: MainViewModel) {
    val score by viewModel.optimizationScore.collectAsState()
    val balance by viewModel.teacherBalanceScore.collectAsState()
    val utilization by viewModel.classroomUtilizationScore.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val softNotes by viewModel.softConstraintsNotes.collectAsState()
    val unassignedSubjs by viewModel.unassignedSubjects.collectAsState()

    // AI suggestion variables
    val isQueryingAi by viewModel.isQueryingAi.collectAsState()
    val aiResponse by viewModel.aiSuggestions.collectAsState()

    val cells by viewModel.timetableCells.collectAsState()

    // Breathing pulse for generator glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowScalar by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scalar"
    )

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
                .padding(top = 16.dp, bottom = 18.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("AI Scheduling Engine", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text("Compile lists with advanced constraint optimization.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            if (cells.isNotEmpty()) {
                Button(
                    onClick = { viewModel.navigateTo(Screen.Dashboard) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = "View dashboard", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Interactive Schedules", color = Color.White)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Health Meter Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Timetable Health Meter",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Real-time mathematical validation against university rules.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Optimization score
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { (score.toFloat() / 100f) },
                                        modifier = Modifier.size(85.dp),
                                        color = if (score >= 90) AccentEmerald else AccentAmber,
                                        strokeWidth = 7.dp,
                                        trackColor = DarkBorder
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$score%",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Health",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            // Instructor load balance progress
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { (balance.toFloat() / 100f) },
                                        modifier = Modifier.size(85.dp),
                                        color = AccentTealLight,
                                        strokeWidth = 7.dp,
                                        trackColor = DarkBorder
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$balance%",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Fairness",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            // Classroom resource utilization progress
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = { (utilization.toFloat() / 100f) },
                                        modifier = Modifier.size(85.dp),
                                        color = AccentVioletLight,
                                        strokeWidth = 7.dp,
                                        trackColor = DarkBorder
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$utilization%",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Resource",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Automated Generation Trigger Button Action
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.generateTimetable() },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .border(
                                width = if (isGenerating) 0.dp else 1.5.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(AccentTealLight, AccentVioletLight)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGenerating) DarkSurface else Color.Transparent,
                            disabledContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = AccentTealLight,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Assembling Conflict-Free Schedules...", color = TextPrimary)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Generator run toggle",
                                    tint = AccentTealLight,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (cells.isEmpty()) "Generate First Draft Schedule" else "One-Click AI Re-Generation",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 3. Engine Log Notes Section
            item {
                Text(
                    text = "Constraint Solver Diagnosis Logs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (softNotes.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No active timetable generated. Tap the button above to run our discrete scheduling optimization.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            } else {
                items(softNotes) { note ->
                    val isCrit = note.contains("⚠")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCrit) AccentRose.copy(alpha = 0.08f) else DarkSurface)
                            .border(1.dp, if (isCrit) AccentRose.copy(alpha = 0.3f) else DarkBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCrit) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = "Log icon",
                            tint = if (isCrit) AccentRose else AccentEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = note,
                            color = if (isCrit) TextPrimary else TextPrimary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 4. AI Schedule Analysis Screen
            if (cells.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "AI helper logo",
                                    tint = AccentVioletLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Schedule Analysis",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "Analyze timetable loads, compaction opportunities, or explain scheduling decisions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                            )

                            // AI Action trigger buttons row
                            Button(
                                onClick = { viewModel.queryAiSuggestions() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                                enabled = !isQueryingAi,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isQueryingAi) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating suggestions...", color = Color.White)
                                } else {
                                    Text("Run AI Compaction Analysis", color = Color.White)
                                }
                            }

                            // Display AI output response in a polished way
                            if (aiResponse.isNotBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DarkBg)
                                        .border(1.dp, AccentViolet.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(14.dp)
                                ) {
                                    Text(
                                        text = aiResponse,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action continuing bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { viewModel.navigateTo(Screen.SubjectAllocation) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                Text("Subject Allocations", color = TextPrimary)
            }

            if (cells.isNotEmpty()) {
                Button(
                    onClick = { viewModel.navigateTo(Screen.Dashboard) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("Interactive Schedules", color = Color.White)
                    Icon(Icons.Default.ArrowForward, contentDescription = "Continue", tint = Color.White)
                }
            } else {
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
    }
}
