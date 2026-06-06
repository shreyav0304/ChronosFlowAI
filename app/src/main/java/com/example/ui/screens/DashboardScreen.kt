package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.*
import com.example.scheduling.ScheduleEngine
import com.example.ui.theme.*
import com.example.ui.viewmodel.DashboardViewType
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val cells by viewModel.timetableCells.collectAsState()
    val teachersList by viewModel.teachers.collectAsState()
    val subjectsList by viewModel.subjects.collectAsState()
    val classroomsList by viewModel.classrooms.collectAsState()
    val departmentsList by viewModel.departments.collectAsState()

    val viewType by viewModel.dashboardViewType.collectAsState()
    val activeSection by viewModel.selectedSectionFilter.collectAsState()
    val activeTeacherId by viewModel.selectedTeacherFilterId.collectAsState()
    val activeRoomId by viewModel.selectedRoomFilterId.collectAsState()

    val swappingCell by viewModel.swappingCell.collectAsState()
    val liveConflicts by viewModel.liveConflicts.collectAsState()

    var useGridView by remember { mutableStateOf(true) }

    // Render active selection lists
    val sectionsAvailable = listOf("Section A", "Section B")

    // Define mock period hours
    val periodTimes = listOf(
        "09:00 AM - 10:00 AM",
        "10:00 AM - 11:00 AM",
        "11:00 AM - 12:00 PM",
        "12:00 PM - 01:00 PM", // LUNCH PERIOD
        "01:00 PM - 02:00 PM",
        "02:00 PM - 03:00 PM"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Dashboard Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(top = 16.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Timetable Dashboard", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text("Double-tap any period block to swap hours.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            // Quick access to Conflict Resolution Center
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Compact Dynamically Mode Toggle
                IconButton(onClick = { ThemeState.isDarkMode = !ThemeState.isDarkMode }) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (ThemeState.isDarkMode) AccentViolet.copy(alpha = 0.2f) else AccentTeal.copy(alpha = 0.15f))
                            .border(1.dp, if (ThemeState.isDarkMode) AccentVioletLight else AccentTeal, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (ThemeState.isDarkMode) Icons.Default.Star else Icons.Default.Refresh,
                            contentDescription = "Toggle Theme Mode",
                            tint = if (ThemeState.isDarkMode) AccentAmber else AccentTeal,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                IconButton(onClick = { viewModel.setOnboardingActive(true) }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "System Guide Tour",
                        tint = AccentTealLight
                    )
                }

                IconButton(onClick = { viewModel.navigateTo(Screen.ConflictCenter) }) {
                    Box {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Conflicts center", tint = if (liveConflicts.isNotEmpty()) AccentRose else AccentEmerald)
                        if (liveConflicts.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(AccentRose)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }
        }

        // View Pivot Tabs Section
        TabRow(
            selectedTabIndex = viewType.ordinal,
            containerColor = DarkSurface,
            contentColor = AccentTealLight
        ) {
            Tab(
                selected = viewType == DashboardViewType.Section,
                onClick = { viewModel.setDashboardViewType(DashboardViewType.Section) },
                text = { Text("Sections/Classes", style = MaterialTheme.typography.bodySmall) }
            )
            Tab(
                selected = viewType == DashboardViewType.Teacher,
                onClick = { viewModel.setDashboardViewType(DashboardViewType.Teacher) },
                text = { Text("Instructors", style = MaterialTheme.typography.bodySmall) }
            )
            Tab(
                selected = viewType == DashboardViewType.Classroom,
                onClick = { viewModel.setDashboardViewType(DashboardViewType.Classroom) },
                text = { Text("Lecture/Labs", style = MaterialTheme.typography.bodySmall) }
            )
        }

        // Filters horizontal strip scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.List, contentDescription = "Filters icon", tint = TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))

            when (viewType) {
                DashboardViewType.Section -> {
                    sectionsAvailable.forEach { sec ->
                        val isSel = activeSection == sec
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.setSectionFilter(sec) },
                            label = { Text(sec) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                DashboardViewType.Teacher -> {
                    teachersList.forEach { teacher ->
                        val isSel = activeTeacherId == teacher.id
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.setTeacherFilter(teacher.id) },
                            label = { Text(teacher.name.split(" ").lastOrNull() ?: teacher.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentViolet,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                DashboardViewType.Classroom -> {
                    classroomsList.forEach { classroom ->
                        val isSel = activeRoomId == classroom.id
                        FilterChip(
                            selected = isSel,
                            onClick = { viewModel.setRoomFilter(classroom.id) },
                            label = { Text(classroom.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Active Swap Banner Indicator
        if (swappingCell != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentViolet.copy(alpha = 0.25f))
                    .border(1.dp, AccentVioletLight, RoundedCornerShape(0.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Interactive Swapping Mode: Selected ${subjectsList.find { it.id == swappingCell?.subjectId }?.name ?: "Current"}. Click another slot to swap hours.",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Cancel",
                        color = AccentRose,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .clickable { viewModel.generateTimetable() } // Simple reset trick
                            .padding(horizontal = 8.dp)
                    )
                }
            }
        }

        // Main Schedule Scroll Layout list. Each Day of week is a section
        if (cells.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty Schedule",
                        tint = AccentTealLight,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Timetable Schedules Found",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Deploy your academic roster or run the automated Constraint-Optimization solver to fill this grid.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.generateTimetable() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Solve")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Solve with AI", color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }

                        Button(
                            onClick = { viewModel.forceSeed() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Seed")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Load Demo", color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        } else {
            // View Layout Toggle Segment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Roster Visualizer",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )

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
                            .background(if (!useGridView) AccentTeal.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { useGridView = false }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "List",
                                tint = if (!useGridView) AccentTealLight else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Simple List",
                                color = if (!useGridView) AccentTealLight else TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (useGridView) AccentTeal.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { useGridView = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Grid",
                                tint = if (useGridView) AccentTealLight else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "2D Grid Matrix",
                                color = if (useGridView) AccentTealLight else TextSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (useGridView) {
                // Highly optimized 2D Grid Matrix (simulates CSS Grid behavior)
                // Double scroll direction container perfectly protects labels/text from overlaps
                val horizontalScrollState = rememberScrollState()
                val verticalScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScrollState)
                            .verticalScroll(verticalScrollState)
                            .padding(8.dp)
                    ) {
                        // --- 1. HEADERS ROW ---
                        Row(
                            modifier = Modifier
                                .background(DarkBg)
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Corner header Cell
                            Box(
                                modifier = Modifier
                                    .width(75.dp)
                                    .padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "TIME",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Weekday Titles
                            ScheduleEngine.WORKING_DAYS.forEach { dayName ->
                                Box(
                                    modifier = Modifier
                                        .width(130.dp)
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayName.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentTealLight,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
                        Spacer(modifier = Modifier.height(4.dp))

                        // --- 2. PERIOD ROWS ---
                        (0 until ScheduleEngine.NUM_PERIODS).forEach { periodIdx ->
                            val isLunch = periodIdx == ScheduleEngine.LUNCH_PERIOD

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Time label leftmost cell column
                                Column(
                                    modifier = Modifier
                                        .width(75.dp)
                                        .padding(horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Period ${periodIdx + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = periodTimes[periodIdx].split("-").first().trim(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }

                                if (isLunch) {
                                    // Stretch full width for standard lunch row
                                    Box(
                                        modifier = Modifier
                                            .width(130.dp * 5)
                                            .height(54.dp)
                                            .padding(horizontal = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkBorder.copy(alpha = 0.4f))
                                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Info, contentDescription = "Break", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Midday Lunch Break (12:00 PM - 01:00 PM)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    // Row cells mapped weekdays
                                    ScheduleEngine.WORKING_DAYS.forEach { dayName ->
                                        val matchedCell = cells.find { cell ->
                                            cell.day == dayName && cell.periodIndex == periodIdx && when (viewType) {
                                                DashboardViewType.Section -> cell.section == activeSection
                                                DashboardViewType.Teacher -> cell.teacherId == activeTeacherId
                                                DashboardViewType.Classroom -> cell.classroomId == activeRoomId
                                            }
                                        }

                                        if (matchedCell != null) {
                                            val activeSubj = subjectsList.find { it.id == matchedCell.subjectId }
                                            val activeTeacher = teachersList.find { it.id == matchedCell.teacherId }
                                            val activeRoom = classroomsList.find { it.id == matchedCell.classroomId }

                                            val isSwapper = swappingCell?.id == matchedCell.id
                                            val isAnySwapping = swappingCell != null

                                            // Spring animations for scale, alpha, background, and border
                                            val targetScale = if (isSwapper) 1.06f else (if (isAnySwapping) 0.92f else 1.0f)
                                            val scale by animateFloatAsState(
                                                targetValue = targetScale,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                ),
                                                label = "cell_scale"
                                            )

                                            val targetAlpha = if (isSwapper) 1.0f else (if (isAnySwapping) 0.65f else 1.0f)
                                            val alpha by animateFloatAsState(
                                                targetValue = targetAlpha,
                                                animationSpec = tween(durationMillis = 250),
                                                label = "cell_alpha"
                                            )

                                            val targetBgColor = if (isSwapper) AccentViolet.copy(alpha = 0.22f) else DarkBg
                                            val bgColor by animateColorAsState(
                                                targetValue = targetBgColor,
                                                animationSpec = tween(durationMillis = 200),
                                                label = "cell_bg"
                                            )

                                            val targetBorderColor = if (isSwapper) AccentVioletLight else DarkBorder
                                            val borderColor by animateColorAsState(
                                                targetValue = targetBorderColor,
                                                animationSpec = tween(durationMillis = 200),
                                                label = "cell_border"
                                            )

                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = bgColor
                                                ),
                                                modifier = Modifier
                                                    .width(130.dp)
                                                    .height(115.dp)
                                                    .padding(horizontal = 4.dp)
                                                    .graphicsLayer(
                                                        scaleX = scale,
                                                        scaleY = scale,
                                                        alpha = alpha
                                                    )
                                                    .border(
                                                        width = if (isSwapper) 2.dp else 1.dp,
                                                        color = borderColor,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable { viewModel.selectCellForSwap(matchedCell) }
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(8.dp),
                                                    verticalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = if (activeSubj?.isLab == true) "LAB" else "LECTURE",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = if (activeSubj?.isLab == true) AccentVioletLight else AccentTealLight,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                modifier = Modifier.weight(1f)
                                                            )

                                                            Icon(
                                                                imageVector = Icons.Default.Close,
                                                                contentDescription = "Clear slot",
                                                                tint = TextMuted,
                                                                modifier = Modifier
                                                                    .size(14.dp)
                                                                    .clickable { viewModel.deleteTimetableCellById(matchedCell.id) }
                                                            )
                                                        }

                                                        Spacer(modifier = Modifier.height(2.dp))

                                                        Text(
                                                            text = activeSubj?.name ?: "Lectures System",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TextPrimary,
                                                            maxLines = 2
                                                        )
                                                    }

                                                    Text(
                                                        text = when (viewType) {
                                                            DashboardViewType.Section -> "${activeTeacher?.name?.split(" ")?.lastOrNull() ?: "Faculty"} • R:${activeRoom?.name ?: "No Room"}"
                                                            DashboardViewType.Teacher -> "Sec:${matchedCell.section} • R:${activeRoom?.name ?: "No Room"}"
                                                            DashboardViewType.Classroom -> "Sec:${matchedCell.section} • ${activeTeacher?.name?.split(" ")?.lastOrNull() ?: "Faculty"}"
                                                        },
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TextSecondary,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        } else {
                                            // Empty scheduler vacant container cell
                                            val isSwapCandidate = swappingCell != null

                                             // Spring-based target scaling on candidate slot activation
                                             val targetScaleVacant = if (isSwapCandidate) 1.04f else 1.0f
                                             val scaleVacant by animateFloatAsState(
                                                 targetValue = targetScaleVacant,
                                                 animationSpec = spring(
                                                     dampingRatio = Spring.DampingRatioHighBouncy,
                                                     stiffness = Spring.StiffnessMedium
                                                 ),
                                                 label = "vacant_scale"
                                             )

                                             // Pulsing alpha selector indicator
                                             val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
                                             val pulseAlphaState = if (isSwapCandidate) {
                                                 infiniteTransition.animateFloat(
                                                     initialValue = 0.4f,
                                                     targetValue = 0.9f,
                                                     animationSpec = infiniteRepeatable(
                                                         animation = tween(1200, easing = LinearEasing),
                                                         repeatMode = RepeatMode.Reverse
                                                     ),
                                                     label = "pulse_val"
                                                 )
                                             } else {
                                                 remember { mutableStateOf(1.0f) }
                                             }
                                             val pulseAlpha = pulseAlphaState.value

                                             val targetVacantBg = if (isSwapCandidate) AccentViolet.copy(alpha = 0.08f) else Color.Transparent
                                             val vacantBgColor by animateColorAsState(
                                                 targetValue = targetVacantBg,
                                                 animationSpec = tween(250),
                                                 label = "vacant_bg"
                                             )

                                             val targetVacantBorderColor = if (isSwapCandidate) {
                                                 AccentVioletLight.copy(alpha = pulseAlpha)
                                             } else {
                                                 DarkBorder.copy(alpha = 0.8f)
                                             }
                                             val vacantBorderColor by animateColorAsState(
                                                 targetValue = targetVacantBorderColor,
                                                 animationSpec = tween(250),
                                                 label = "vacant_border"
                                             )

                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = vacantBgColor),
                                                modifier = Modifier
                                                    .width(130.dp)
                                                    .height(115.dp)
                                                    .padding(horizontal = 4.dp)
                                                    .graphicsLayer(
                                                        scaleX = scaleVacant,
                                                        scaleY = scaleVacant
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = vacantBorderColor,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable {
                                                        if (isSwapCandidate) {
                                                            viewModel.moveCellToVacantSlot(
                                                                cell = swappingCell!!,
                                                                targetDay = dayName,
                                                                targetPeriodIndex = periodIdx
                                                            )
                                                        }
                                                    }
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(8.dp),
                                                    verticalArrangement = Arrangement.Center,
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSwapCandidate) Icons.Default.CheckCircle else Icons.Default.Add,
                                                        contentDescription = "Empty",
                                                        tint = if (isSwapCandidate) AccentVioletLight else TextMuted,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = if (isSwapCandidate) "MOVE HERE" else "Vacant",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isSwapCandidate) AccentVioletLight else TextMuted,
                                                        fontWeight = if (isSwapCandidate) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkBorder))
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            } else {
                // Fallback traditional list, now heavily optimized for fluid wrapping without overlaying
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(ScheduleEngine.WORKING_DAYS) { day ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Day Heading bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.List, contentDescription = "Day icon", tint = AccentTealLight, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                // Sequence of Period Blocks (0 to 5)
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    (0 until ScheduleEngine.NUM_PERIODS).forEach { periodIndex ->
                                        val isLunch = periodIndex == ScheduleEngine.LUNCH_PERIOD

                                        if (isLunch) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(DarkBorder.copy(alpha = 0.3f))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Info, contentDescription = "Lunch break", tint = TextMuted, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "12:00 PM - 01:00 PM • Midday Lunch Break",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary
                                                )
                                            }
                                        } else {
                                            val matchedCell = cells.find { cell ->
                                                cell.day == day && cell.periodIndex == periodIndex && when (viewType) {
                                                    DashboardViewType.Section -> cell.section == activeSection
                                                    DashboardViewType.Teacher -> cell.teacherId == activeTeacherId
                                                    DashboardViewType.Classroom -> cell.classroomId == activeRoomId
                                                }
                                            }

                                            if (matchedCell != null) {
                                                val activeSubj = subjectsList.find { it.id == matchedCell.subjectId }
                                                val activeTeacher = teachersList.find { it.id == matchedCell.teacherId }
                                                val activeRoom = classroomsList.find { it.id == matchedCell.classroomId }

                                                val isSwapper = swappingCell?.id == matchedCell.id
                                                val isAnySwapping = swappingCell != null

                                                // Spring animations for scale, alpha, background, and border
                                                val targetScale = if (isSwapper) 1.04f else (if (isAnySwapping) 0.94f else 1.0f)
                                                val scale by animateFloatAsState(
                                                    targetValue = targetScale,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    ),
                                                    label = "list_cell_scale"
                                                )

                                                val targetAlpha = if (isSwapper) 1.0f else (if (isAnySwapping) 0.7f else 1.0f)
                                                val alpha by animateFloatAsState(
                                                    targetValue = targetAlpha,
                                                    animationSpec = tween(durationMillis = 250),
                                                    label = "list_cell_alpha"
                                                )

                                                val targetBgColor = if (isSwapper) AccentViolet.copy(alpha = 0.22f) else DarkBg
                                                val bgColor by animateColorAsState(
                                                    targetValue = targetBgColor,
                                                    animationSpec = tween(durationMillis = 200),
                                                    label = "list_cell_bg"
                                                )

                                                val targetBorderColor = if (isSwapper) AccentVioletLight else DarkBorder
                                                val borderColor by animateColorAsState(
                                                    targetValue = targetBorderColor,
                                                    animationSpec = tween(durationMillis = 200),
                                                    label = "list_cell_border"
                                                )

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .graphicsLayer(
                                                            scaleX = scale,
                                                            scaleY = scale,
                                                            alpha = alpha
                                                        )
                                                        .background(bgColor)
                                                        .border(
                                                            width = 1.2.dp,
                                                            color = borderColor,
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .clickable { viewModel.selectCellForSwap(matchedCell) }
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = "Period ${periodIndex + 1} (${periodTimes[periodIndex].split(" ").first()})",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = if (activeSubj?.isLab == true) AccentVioletLight else AccentTealLight
                                                            )
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            if (activeSubj?.isLab == true) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                        .background(AccentViolet.copy(alpha = 0.2f))
                                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                                ) {
                                                                    Text("LAB", color = AccentVioletLight, style = MaterialTheme.typography.labelSmall)
                                                                 }
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Text(
                                                            text = activeSubj?.name ?: "Lectures System",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TextPrimary
                                                        )

                                                        Spacer(modifier = Modifier.height(4.dp))

                                                        Text(
                                                            text = when (viewType) {
                                                                DashboardViewType.Section -> "Faculty: ${activeTeacher?.name ?: "Unassigned"} • Room: ${activeRoom?.name ?: "No Room"}"
                                                                DashboardViewType.Teacher -> "Class: ${matchedCell.section} • Room: ${activeRoom?.name ?: "No Room"}"
                                                                DashboardViewType.Classroom -> "Class: ${matchedCell.section} • Faculty: ${activeTeacher?.name ?: "Unassigned"}"
                                                            },
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = TextSecondary
                                                        )
                                                    }

                                                    IconButton(onClick = { viewModel.deleteTimetableCellById(matchedCell.id) }) {
                                                        Icon(Icons.Default.Close, contentDescription = "Clear slot", tint = TextMuted, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            } else {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color.Transparent)
                                                        .border(
                                                            width = 1.dp,
                                                            color = DarkBorder,
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(
                                                            text = "Period ${periodIndex + 1}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = TextMuted,
                                                            modifier = Modifier.width(60.dp)
                                                        )
                                                        Text(
                                                            text = "Open Schedule Block Space",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = TextMuted,
                                                            maxLines = 1
                                                        )
                                                    }

                                                    Icon(
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = "Unallocated slot",
                                                        tint = TextMuted,
                                                        modifier = Modifier.size(16.dp)
                                                    )
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

    // Action continue controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { viewModel.navigateTo(Screen.Generator) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                Text("AI Scheduler", color = TextPrimary)
            }

            Button(
                onClick = { viewModel.navigateTo(Screen.ConflictCenter) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
            ) {
                Text("Conflict Resolution", color = Color.White)
                Icon(Icons.Default.ArrowForward, contentDescription = "Continue", tint = Color.White)
            }
        }
    }
}
