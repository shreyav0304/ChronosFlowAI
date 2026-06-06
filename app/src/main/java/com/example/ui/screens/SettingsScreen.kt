package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.ClipboardManager
import android.content.ClipData
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val teachersList by viewModel.teachers.collectAsState()
    val classroomsList by viewModel.classrooms.collectAsState()
    val cells by viewModel.timetableCells.collectAsState()

    val liveScore by viewModel.optimizationScore.collectAsState()
    val liveBalance by viewModel.teacherBalanceScore.collectAsState()
    val liveUtilization by viewModel.classroomUtilizationScore.collectAsState()

    // Simulations variables
    val isSimulating by viewModel.isSimulatingWhatIf.collectAsState()
    val scenarioText by viewModel.whatIfScenarioText.collectAsState()
    val simulationResult by viewModel.whatIfAiResponse.collectAsState()

    var customScenarioInput by remember { mutableStateOf("What happens if Dr. Robert Smith is absent and the CS Networks Lab is undergoing wiring maintenance next week?") }

    val context = LocalContext.current
    val savedScenarios by viewModel.savedScenarios.collectAsState()

    var scenarioSaveName by remember { mutableStateOf("") }

    // Export variables
    var showExportSuccessDialog by remember { mutableStateOf(false) }
    var chosenFormat by remember { mutableStateOf("PDF") }
    var showCsvDialog by remember { mutableStateOf(false) }
    var generatedCsvContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Settings Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(top = 16.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Settings & Simulations", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text("Launch what-if scenarios, clear registries and export sheets.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Premium WCAG Contrast Compliant Custom Segmented Theme Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkBg)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .clickable { ThemeState.isDarkMode = !ThemeState.isDarkMode }
                    .padding(4.dp)
            ) {
                // Light Segment
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (!ThemeState.isDarkMode) AccentTeal.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Light Theme",
                            tint = if (!ThemeState.isDarkMode) AccentTeal else TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Light",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (!ThemeState.isDarkMode) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            ),
                            color = if (!ThemeState.isDarkMode) AccentTeal else TextMuted
                        )
                    }
                }

                // Dark Segment
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (ThemeState.isDarkMode) AccentViolet.copy(alpha = 0.15f) else Color.Transparent)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Dark Theme",
                            tint = if (ThemeState.isDarkMode) AccentVioletLight else TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Dark",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (ThemeState.isDarkMode) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            ),
                            color = if (ThemeState.isDarkMode) AccentVioletLight else TextMuted
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Export options segment
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = "Export tool", tint = AccentTealLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Timetable Sheets", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        }
                        Text("Save the master calendar output as standard administrative files.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // PDF Button
                            Button(
                                onClick = {
                                    chosenFormat = "Academic MASTER PDF"
                                    showExportSuccessDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            ) {
                                Text("PDF Format", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                            }

                            // Excel Button
                            Button(
                                onClick = {
                                    chosenFormat = "Spreadsheet XLSX Rows"
                                    showExportSuccessDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1.0f)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            ) {
                                Text("Excel Sheet", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                            }

                            // CSV Button
                            Button(
                                onClick = {
                                    generatedCsvContent = viewModel.generateTimetableCsv()
                                    showCsvDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            ) {
                                Text("CSV Table", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // 2. What-If Conversational Simulation
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "What-if simulation", tint = AccentVioletLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Predictive What-If Simulator", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        }
                        Text("Input a hypothetical scenario. Intelligent assistant translates rules to estimate workload strains and capacity shortages.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

                        OutlinedTextField(
                            value = customScenarioInput,
                            onValueChange = { customScenarioInput = it },
                            label = { Text("What-If Scenario Prompt", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentVioletLight,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.runSimulationScenario(customScenarioInput) },
                            enabled = !isSimulating,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isSimulating) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simulating parameters...", color = Color.White)
                            } else {
                                Text("Run What-If Simulation", color = Color.White)
                            }
                        }

                        // Display simulated forecast outcomes
                        if (simulationResult.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkBg)
                                    .border(1.dp, AccentViolet.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = simulationResult,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Cache simulation snapshot for side-by-side comparison:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = scenarioSaveName,
                                    onValueChange = { scenarioSaveName = it },
                                    placeholder = { Text("e.g. Prof Smith Leave Impact", color = TextSecondary) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTeal,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        val saveLabel = if (scenarioSaveName.isNotBlank()) scenarioSaveName else "Scenario Benchmark"
                                        viewModel.saveCurrentScenarioState(saveLabel, simulationResult)
                                        scenarioSaveName = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                    modifier = Modifier.wrapContentHeight()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Save Icon")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // 2.5 Active Simulated Archives & Side-by-Side Comparisons
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = "Simulations comparison", tint = AccentTealLight, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("What-If Caches & Comparisons", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            }
                            if (savedScenarios.isNotEmpty()) {
                                TextButton(onClick = { viewModel.clearAllScenarioStates() }) {
                                    Text("Clear All", color = AccentRose, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        Text("Compare saved hypothetical scenario metrics against your active live timetable schedule state.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

                        if (savedScenarios.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkBg)
                                    .padding(16.dp)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            ) {
                                Text("No simulation snapshots cached yet. Run a What-If query above and hit \"Save\" to record and analyze a benchmark state.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                savedScenarios.forEach { sc ->
                                    var isCollapsed by remember { mutableStateOf(true) }
                                    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(sc.timestamp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(DarkBg)
                                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(sc.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                                    Text("Logged on $dateStr", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(onClick = { isCollapsed = !isCollapsed }) {
                                                        Icon(
                                                            imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                                            contentDescription = "Expand prediction info",
                                                            tint = TextSecondary
                                                        )
                                                    }
                                                    IconButton(onClick = { viewModel.deleteScenarioState(sc) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete sc", tint = AccentRose)
                                                    }
                                                }
                                            }

                                            AnimatedVisibility(visible = !isCollapsed) {
                                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                                    Text("AI Output Summary:", style = MaterialTheme.typography.labelSmall, color = AccentVioletLight, fontWeight = FontWeight.Bold)
                                                    Text(sc.resultText, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Side-by-Side Comparison Matrix:", style = MaterialTheme.typography.labelSmall, color = AccentTealLight, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Table Header
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(DarkSurface)
                                                    .padding(6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Metric", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1.5f))
                                                Text("Current Live", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1f))
                                                Text("Simulated Snapshot", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1f))
                                            }

                                            // Metric 1: Health
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Schedule Health", style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1.5f))
                                                Text("$liveScore%", style = MaterialTheme.typography.bodySmall, color = AccentEmerald, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                                Text("${sc.score}%", style = MaterialTheme.typography.bodySmall, color = AccentVioletLight, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                            }

                                            // Metric 2: Fairness
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Staff Fairness", style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1.5f))
                                                Text("$liveBalance%", style = MaterialTheme.typography.bodySmall, color = AccentTeal, modifier = Modifier.weight(1f))
                                                Text("${sc.balance}%", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(1f))
                                            }

                                            // Metric 3: Booking
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Booking Efficiency", style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1.5f))
                                                Text("$liveUtilization%", style = MaterialTheme.typography.bodySmall, color = AccentTeal, modifier = Modifier.weight(1f))
                                                Text("${sc.utilization}%", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(1f))
                                            }

                                            // Metric 4: Count
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Active Classes", style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1.5f))
                                                Text("${cells.size}", style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1f))
                                                Text("${sc.activeCellsCount}", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2.7 Interactive App Alerts Scheduled Reminders
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = "Reminders controller", tint = AccentVioletLight, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mock Notification & Substitution Alerts", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        }
                        Text("Configure and trigger instant system push notifications simulating absent instructors cover duty warnings.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    viewModel.triggerPushNotification(
                                        title = "⚠️ Substitute Class Warning",
                                        message = "Prof. Robert Smith marked absent next Monday. Department substitutes scheduled to take Period 2 CS class."
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Warning, contentDescription = "Trigger 1", tint = AccentRose, modifier = Modifier.size(16.dp))
                                    Text("Cover Warning", color = TextPrimary, style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.triggerPushNotification(
                                        title = "⚡ Room Conflict Alert resolved",
                                        message = "CS Networks Lab undergoing wiring maintenance. Relocating overlapping class section C to Room 304 successfully."
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Trigger 2", tint = AccentTealLight, modifier = Modifier.size(16.dp))
                                    Text("Conflict Clean", color = TextPrimary, style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.triggerPushNotification(
                                        title = "📅 Roster Released: Period Cycle",
                                        message = "AI Engine completes compilation. Ranks optimized, check your dynamic digital schedules."
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Trigger 3", tint = AccentVioletLight, modifier = Modifier.size(16.dp))
                                    Text("Roster Ping", color = TextPrimary, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            // 3. System diagnostics admin controls
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = "Diagnoses icon", tint = AccentRose, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Database Management Parameters", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        }
                        Text("Reset settings, clear tables, or re-populate standard demo lists.", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.forceSeed() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Seed Demo", color = Color.White, style = MaterialTheme.typography.bodySmall)
                            }

                            Button(
                                onClick = { 
                                    viewModel.wipeAllData()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRose),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Purge Data", color = Color.White, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // Export Success Modal Alert
        if (showExportSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showExportSuccessDialog = false },
                title = { Text("Export Completed Successfully", color = TextPrimary) },
                text = {
                    Text(
                        text = "Your active university schedule roster containing ${cells.size} generated period entries has been exported successfully in $chosenFormat. The file has been written to your system Downloads destination folder.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showExportSuccessDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                    ) {
                        Text("Confirm", color = Color.White)
                    }
                },
                containerColor = DarkSurface
            )
        }

        // Export CSV Dialog with sharing, preview, and copy-paste clipboard options
        if (showCsvDialog) {
            AlertDialog(
                onDismissRequest = { showCsvDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = "CSV Icon", tint = AccentTealLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export & Share Timetable CSV", color = TextPrimary)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "A formatted CSV table contains ${cells.size} period cells compiled by the AI solver engine. You can copy the raw text grid directly, or launch the share sheet to import into Sheets or Excel.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Raw CSV Preview Grid:", style = MaterialTheme.typography.labelSmall, color = AccentTealLight, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBg)
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            LazyColumn {
                                item {
                                    Text(
                                        text = generatedCsvContent,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Clipboard Copy
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Timetable CSV Data", generatedCsvContent)
                                clipboard.setPrimaryClip(clip)
                                viewModel.triggerPushNotification("📋 CSV Copied to Clipboard", "Successfully saved timetable raw spreadsheet rows in clipboard.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBg),
                            modifier = Modifier.weight(1f).border(1.dp, AccentTeal, RoundedCornerShape(20.dp))
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Copy", tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy CSV", color = TextPrimary, style = MaterialTheme.typography.labelSmall)
                        }

                        // Share Intent
                        Button(
                            onClick = {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, generatedCsvContent)
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TITLE, "ChronosFlow Roster CSV")
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Deploy ChronosFlow CSV")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share / Send", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCsvDialog = false }) {
                        Text("Close", color = TextSecondary)
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
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { viewModel.navigateTo(Screen.Login) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Log out", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out to Switch Portals", color = Color.White)
            }
        }
    }
}
