package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.scheduling.ScheduleEngine
import com.example.ui.theme.*
import com.example.ui.viewmodel.DashboardViewType
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

enum class SidebarTab {
    Subjects,
    Professors,
    LectureRooms,
    AiSuggester
}

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
    val optimizationScore by viewModel.optimizationScore.collectAsState()

    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    val schedulePresets by viewModel.schedulePresets.collectAsState()

    // Screen Layout States
    var showMobileSidebar by remember { mutableStateOf(false) }
    var useGridView by remember { mutableStateOf(true) }
    val sectionsAvailable = listOf("Section A", "Section B")

    // Dialog state for presets
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetSaveName by remember { mutableStateOf("") }

    // Drag-and-drop state variables
    var activeDraggingSubject by remember { mutableStateOf<Subject?>(null) }
    var activeDraggingTeacher by remember { mutableStateOf<Teacher?>(null) }
    var activeDraggingRoom by remember { mutableStateOf<Classroom?>(null) }
    var dragGlobalPosition by remember { mutableStateOf(Offset.Zero) }
    var dragCurrentOffset by remember { mutableStateOf(Offset.Zero) }
    var isCurrentlyDragging by remember { mutableStateOf(false) }
    val gridSlotsCoordinates = remember { mutableStateMapOf<String, LayoutCoordinates>() }
    var hoveredSlotKey by remember { mutableStateOf<String?>(null) }

    // Brush Paint Tool state (For instant canvas Painting schedule entries)
    var paintSubject by remember { mutableStateOf<Subject?>(null) }
    var paintTeacher by remember { mutableStateOf<Teacher?>(null) }
    var paintRoom by remember { mutableStateOf<Classroom?>(null) }
    var paintSection by remember { mutableStateOf("Section A") }

    // Dialog state for clicked vacant cells
    var showAssignDialog by remember { mutableStateOf(false) }
    var dialogSelectedDay by remember { mutableStateOf("") }
    var dialogSelectedPeriod by remember { mutableStateOf(0) }

    // Target local assignment state builders for the Manual dialog
    var dialogSubject by remember { mutableStateOf<Subject?>(null) }
    var dialogTeacher by remember { mutableStateOf<Teacher?>(null) }
    var dialogRoom by remember { mutableStateOf<Classroom?>(null) }
    var dialogSection by remember { mutableStateOf("") }

    val periodTimes = listOf(
        "09:00 AM - 10:00 AM",
        "10:00 AM - 11:00 AM",
        "11:00 AM - 12:00 PM",
        "12:00 PM - 01:00 PM", // LUNCH PERIOD
        "01:00 PM - 02:00 PM",
        "02:00 PM - 03:00 PM"
    )

    // Side-by-side or sliding drawer container based on window size
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        val isWideScreen = maxWidth >= 860.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Header Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(top = 14.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Timetable Workspace",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Real-time master scheduling dashboard & resource board.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Interactive Action Utilities Strip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isWideScreen) {
                        Button(
                            onClick = { showMobileSidebar = !showMobileSidebar },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (paintSubject != null) AccentViolet.copy(alpha = 0.2f) else AccentTeal.copy(alpha = 0.15f),
                                contentColor = if (paintSubject != null) AccentVioletLight else AccentTealLight
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (paintSubject != null) AccentVioletLight else AccentTeal),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = if (paintSubject != null) Icons.Default.Build else Icons.Default.Menu,
                                contentDescription = "Active brush panel",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (paintSubject != null) "Paint Brush ON" else "Resources",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    // Dynamically Toggle theme modes
                    IconButton(
                        onClick = { ThemeState.isDarkMode = !ThemeState.isDarkMode },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkBg)
                            .border(1.dp, DarkBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (ThemeState.isDarkMode) Icons.Default.Star else Icons.Outlined.Star,
                            contentDescription = "Toggle theme mode",
                            tint = if (ThemeState.isDarkMode) AccentAmber else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Quick reset / Seed DB template trigger
                    IconButton(
                        onClick = { viewModel.forceSeed() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkBg)
                            .border(1.dp, DarkBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Load layout presets",
                            tint = AccentTealLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Outer Workspace Scaffold Layout
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Wide Screen Mode: Sidebar permanently visible on the Left
                if (isWideScreen) {
                    ResourceManagementSidebar(
                        modifier = Modifier
                            .width(310.dp)
                            .fillMaxHeight()
                            .border(1.dp, DarkBorder),
                        viewModel = viewModel,
                        subjectsList = subjectsList,
                        teachersList = teachersList,
                        classroomsList = classroomsList,
                        liveConflicts = liveConflicts,
                        optimizationScore = optimizationScore,
                        activeCellsCount = cells.size,
                        paintSubject = paintSubject,
                        paintTeacher = paintTeacher,
                        paintRoom = paintRoom,
                        paintSection = paintSection,
                        onPaintSubjectChange = { paintSubject = it },
                        onPaintTeacherChange = { paintTeacher = it },
                        onPaintRoomChange = { paintRoom = it },
                        onPaintSectionChange = { paintSection = it },
                        onClearPaintBrush = {
                            paintSubject = null
                            paintTeacher = null
                            paintRoom = null
                        },
                        onDragStart = { item, tab, coords, offset ->
                            val startInWindow = coords.positionInWindow()
                            dragGlobalPosition = startInWindow + offset
                            isCurrentlyDragging = true
                            when (tab) {
                                SidebarTab.Subjects -> {
                                    activeDraggingSubject = item as Subject
                                    activeDraggingTeacher = null
                                    activeDraggingRoom = null
                                }
                                SidebarTab.Professors -> {
                                    activeDraggingSubject = null
                                    activeDraggingTeacher = item as Teacher
                                    activeDraggingRoom = null
                                }
                                SidebarTab.LectureRooms -> {
                                    activeDraggingSubject = null
                                    activeDraggingTeacher = null
                                    activeDraggingRoom = item as Classroom
                                }
                                else -> {}
                            }
                        },
                        onDrag = { dragAmount ->
                            dragCurrentOffset = dragCurrentOffset + dragAmount
                            dragGlobalPosition = dragGlobalPosition + dragAmount

                            // Find hovered slot
                            val hoverSlot = gridSlotsCoordinates.entries.find { entry ->
                                val coords = entry.value
                                if (coords.isAttached) {
                                    val size = coords.size
                                    val posInWindow = coords.positionInWindow()
                                    val bounds = Rect(
                                        left = posInWindow.x,
                                        top = posInWindow.y,
                                        right = posInWindow.x + size.width,
                                        bottom = posInWindow.y + size.height
                                    )
                                    bounds.contains(dragGlobalPosition)
                                } else {
                                    false
                                }
                            }
                            hoveredSlotKey = hoverSlot?.key
                        },
                        onDragEnd = {
                            val dropSlot = gridSlotsCoordinates.entries.find { entry ->
                                val coords = entry.value
                                if (coords.isAttached) {
                                    val size = coords.size
                                    val posInWindow = coords.positionInWindow()
                                    val bounds = Rect(
                                        left = posInWindow.x,
                                        top = posInWindow.y,
                                        right = posInWindow.x + size.width,
                                        bottom = posInWindow.y + size.height
                                    )
                                    bounds.contains(dragGlobalPosition)
                                } else {
                                    false
                                }
                            }
                            if (dropSlot != null) {
                                val parts = dropSlot.key.split("-")
                                val dropDay = parts[0]
                                val dropPeriodIdx = parts[1].toInt()

                                val cellAtSlot = cells.find { cell ->
                                    cell.day == dropDay && cell.periodIndex == dropPeriodIdx && when (viewType) {
                                        DashboardViewType.Section -> cell.section == activeSection
                                        DashboardViewType.Teacher -> cell.teacherId == activeTeacherId
                                        DashboardViewType.Classroom -> cell.classroomId == activeRoomId
                                    }
                                }

                                if (cellAtSlot != null) {
                                    // Drop onto active block
                                    when {
                                        activeDraggingSubject != null -> {
                                            viewModel.addManualCell(
                                                section = cellAtSlot.section,
                                                subjectId = activeDraggingSubject!!.id,
                                                teacherId = cellAtSlot.teacherId,
                                                classroomId = cellAtSlot.classroomId,
                                                day = dropDay,
                                                periodIndex = dropPeriodIdx
                                            )
                                        }
                                        activeDraggingTeacher != null -> {
                                            viewModel.addManualCell(
                                                section = cellAtSlot.section,
                                                subjectId = cellAtSlot.subjectId,
                                                teacherId = activeDraggingTeacher!!.id,
                                                classroomId = cellAtSlot.classroomId,
                                                day = dropDay,
                                                periodIndex = dropPeriodIdx
                                            )
                                        }
                                        activeDraggingRoom != null -> {
                                            viewModel.addManualCell(
                                                section = cellAtSlot.section,
                                                subjectId = cellAtSlot.subjectId,
                                                teacherId = cellAtSlot.teacherId,
                                                classroomId = activeDraggingRoom!!.id,
                                                day = dropDay,
                                                periodIndex = dropPeriodIdx
                                            )
                                        }
                                    }
                                } else {
                                    // Drop onto empty vacant block
                                    if (activeDraggingSubject != null) {
                                        val defaultTeacherId = activeDraggingSubject!!.teacherId ?: teachersList.firstOrNull()?.id ?: 1
                                        val defaultClassroomId = classroomsList.firstOrNull()?.id ?: 1
                                        viewModel.addManualCell(
                                            section = if (viewType == DashboardViewType.Section) activeSection else "Section A",
                                            subjectId = activeDraggingSubject!!.id,
                                            teacherId = defaultTeacherId,
                                            classroomId = defaultClassroomId,
                                            day = dropDay,
                                            periodIndex = dropPeriodIdx
                                        )
                                    }
                                }
                            }
                            // Clean up
                            isCurrentlyDragging = false
                            activeDraggingSubject = null
                            activeDraggingTeacher = null
                            activeDraggingRoom = null
                            dragCurrentOffset = Offset.Zero
                            hoveredSlotKey = null
                        }
                    )
                }

                // Center/Main Area: Timetable Canvas View Grid Row
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(14.dp)
                ) {
                    // Header Area of Canvas View: Filter categories and switches
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // View Selector Category
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                .padding(2.dp)
                        ) {
                            val views = listOf(
                                DashboardViewType.Section to "Class",
                                DashboardViewType.Teacher to "Instructor",
                                DashboardViewType.Classroom to "Room"
                            )
                            views.forEach { (type, label) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (viewType == type) AccentTeal.copy(alpha = 0.15f) else Color.Transparent)
                                        .clickable { viewModel.setDashboardViewType(type) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (viewType == type) AccentTealLight else TextSecondary
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Undo & Redo Actions Row
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.undo() },
                                    enabled = canUndo,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkSurface,
                                        contentColor = TextPrimary,
                                        disabledContainerColor = DarkSurface.copy(alpha = 0.3f),
                                        disabledContentColor = TextMuted
                                    ),
                                    border = BorderStroke(1.dp, if (canUndo) AccentTeal.copy(alpha = 0.5f) else DarkBorder),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Undo recent action",
                                        tint = if (canUndo) AccentTealLight else TextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Undo", style = MaterialTheme.typography.labelSmall, fontSize = 10.5.sp, color = if (canUndo) TextPrimary else TextMuted)
                                }

                                Button(
                                    onClick = { viewModel.redo() },
                                    enabled = canRedo,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkSurface,
                                        contentColor = TextPrimary,
                                        disabledContainerColor = DarkSurface.copy(alpha = 0.3f),
                                        disabledContentColor = TextMuted
                                    ),
                                    border = BorderStroke(1.dp, if (canRedo) AccentTeal.copy(alpha = 0.5f) else DarkBorder),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Redo", style = MaterialTheme.typography.labelSmall, fontSize = 10.5.sp, color = if (canRedo) TextPrimary else TextMuted)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Redo action",
                                        tint = if (canRedo) AccentTealLight else TextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            // Grid vs List mode switches
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                    .padding(2.dp)
                            ) {
                                IconButton(
                                    onClick = { useGridView = true },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (useGridView) AccentTeal.copy(alpha = 0.12f) else Color.Transparent)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "matrix grid view",
                                        tint = if (useGridView) AccentTealLight else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { useGridView = false },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (!useGridView) AccentTeal.copy(alpha = 0.12f) else Color.Transparent)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = "list view roster",
                                        tint = if (!useGridView) AccentTealLight else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Dynamically display filter parameters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .background(DarkSurface, RoundedCornerShape(10.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 6.dp)
                        )

                        when (viewType) {
                            DashboardViewType.Section -> {
                                sectionsAvailable.forEach { sec ->
                                    val isSelected = activeSection == sec
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setSectionFilter(sec) },
                                        label = { Text(sec) }
                                    )
                                }
                            }
                            DashboardViewType.Teacher -> {
                                teachersList.forEach { instructor ->
                                    val isSelected = activeTeacherId == instructor.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setTeacherFilter(instructor.id) },
                                        label = { Text(instructor.name) }
                                    )
                                }
                            }
                            DashboardViewType.Classroom -> {
                                classroomsList.forEach { rm ->
                                    val isSelected = activeRoomId == rm.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setRoomFilter(rm.id) },
                                        label = { Text(rm.name) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notice Banner if paint brush is active
                    if (paintSubject != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AccentViolet.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AccentVioletLight.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = "paintbrush icon",
                                        tint = AccentVioletLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Paintbrush mode active: Tap empty 'Vacant' slot to manual schedule ${paintSubject?.name} • ${paintTeacher?.name} • ${paintRoom?.name} for $paintSection",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(onClick = {
                                    paintSubject = null
                                    paintTeacher = null
                                    paintRoom = null
                                }) {
                                    Text("Clear Brushtip", color = AccentRose, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    // Notice Banner if swap mode option is active
                    if (swappingCell != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AccentAmber.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Swapping Hours Mode: Click another filled block to swap period positions, or empty block to transfer.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { viewModel.generateTimetable() }) {
                                    Text("Cancel Swap", color = AccentRose, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    // --- PRESETS COMPONENT ---
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("presets_container")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Presets",
                                        tint = AccentAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Schedule Configurations & Presets",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                
                                Button(
                                    onClick = { showSavePresetDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal.copy(alpha = 0.15f), contentColor = AccentTealLight),
                                    border = BorderStroke(1.dp, AccentTeal),
                                    modifier = Modifier.height(28.dp).testTag("save_as_preset_button"),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Icon", modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save Active Layout", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (schedulePresets.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkBg, RoundedCornerShape(8.dp))
                                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "No saved layout presets yet. Click \"Save Active Layout\" above to save the current timetable configuration and restore it instantly later.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    schedulePresets.forEach { preset ->
                                        Row(
                                            modifier = Modifier
                                                .background(DarkBg, RoundedCornerShape(10.dp))
                                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .testTag("preset_item_${preset.id}"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    text = preset.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${preset.activeCellsCount} classes • Health: ${preset.score}%",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            Button(
                                                onClick = { viewModel.loadPreset(preset) },
                                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                                modifier = Modifier.height(26.dp).testTag("load_preset_${preset.id}"),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Load layout preset", tint = Color.White, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Load", style = MaterialTheme.typography.labelSmall, fontSize = 9.5.sp, color = Color.White)
                                            }

                                            IconButton(
                                                onClick = { viewModel.deletePreset(preset) },
                                                modifier = Modifier.size(24.dp).testTag("delete_preset_${preset.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete preset",
                                                    tint = AccentRose,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Schedule Body Render
                    if (cells.isEmpty()) {
                        EmptyScheduleWorkspace(
                            onSolve = { viewModel.generateTimetable() },
                            onSeed = { viewModel.forceSeed() }
                        )
                    } else {
                        if (useGridView) {
                            MasterCalendarGridMatrix(
                                cells = cells,
                                subjectsList = subjectsList,
                                teachersList = teachersList,
                                classroomsList = classroomsList,
                                viewType = viewType,
                                activeSection = activeSection,
                                activeTeacherId = activeTeacherId,
                                activeRoomId = activeRoomId,
                                swappingCell = swappingCell,
                                paintSubject = paintSubject,
                                periodTimes = periodTimes,
                                onSelectCellSwap = { viewModel.selectCellForSwap(it) },
                                onDeleteCell = { viewModel.deleteTimetableCellById(it) },
                                onGridSlotClicked = { day, periodIdx ->
                                    if (paintSubject != null && paintTeacher != null && paintRoom != null) {
                                        // Execute paintbrush manual fill
                                        viewModel.addManualCell(
                                            section = paintSection,
                                            subjectId = paintSubject!!.id,
                                            teacherId = paintTeacher!!.id,
                                            classroomId = paintRoom!!.id,
                                            day = day,
                                            periodIndex = periodIdx
                                        )
                                    } else {
                                        // Open dialog input manually
                                        dialogSelectedDay = day
                                        dialogSelectedPeriod = periodIdx
                                        dialogSection = when (viewType) {
                                            DashboardViewType.Section -> activeSection
                                            else -> sectionsAvailable.first()
                                        }
                                        dialogSubject = null
                                        dialogTeacher = null
                                        dialogRoom = null
                                        showAssignDialog = true
                                    }
                                },
                                isCurrentlyDragging = isCurrentlyDragging,
                                hoveredSlotKey = hoveredSlotKey,
                                gridSlotsCoordinates = gridSlotsCoordinates
                            )
                        } else {
                            MasterRosterListView(
                                cells = cells,
                                subjectsList = subjectsList,
                                teachersList = teachersList,
                                classroomsList = classroomsList,
                                viewType = viewType,
                                activeSection = activeSection,
                                activeTeacherId = activeTeacherId,
                                activeRoomId = activeRoomId,
                                swappingCell = swappingCell,
                                periodTimes = periodTimes,
                                onSelectCellSwap = { viewModel.selectCellForSwap(it) },
                                onDeleteCell = { viewModel.deleteTimetableCellById(it) }
                            )
                        }
                    }
                }
            }

            // Bottom navigation screen jumper bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.navigateTo(Screen.Generator) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = TextPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Solver Console", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = { viewModel.navigateTo(Screen.ConflictCenter) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clash Resolution Office", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = "Go forward", tint = Color.White)
                }
            }
        }
    }

    // Sliding Bottom sheet dialog representation for mobile layout sidebar
    if (showMobileSidebar) {
        Dialog(onDismissRequest = { showMobileSidebar = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBg),
                border = BorderStroke(1.2.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Resource Manager Board",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(onClick = { showMobileSidebar = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Board", tint = TextPrimary)
                        }
                    }

                    ResourceManagementSidebar(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        viewModel = viewModel,
                        subjectsList = subjectsList,
                        teachersList = teachersList,
                        classroomsList = classroomsList,
                        liveConflicts = liveConflicts,
                        optimizationScore = optimizationScore,
                        activeCellsCount = cells.size,
                        paintSubject = paintSubject,
                        paintTeacher = paintTeacher,
                        paintRoom = paintRoom,
                        paintSection = paintSection,
                        onPaintSubjectChange = { paintSubject = it },
                        onPaintTeacherChange = { paintTeacher = it },
                        onPaintRoomChange = { paintRoom = it },
                        onPaintSectionChange = { paintSection = it },
                        onClearPaintBrush = {
                            paintSubject = null
                            paintTeacher = null
                            paintRoom = null
                        },
                        onDragStart = { item, tab, coords, offset ->
                            val startInWindow = coords.positionInWindow()
                            dragGlobalPosition = startInWindow + offset
                            isCurrentlyDragging = true
                            when (tab) {
                                SidebarTab.Subjects -> {
                                    activeDraggingSubject = item as Subject
                                    activeDraggingTeacher = null
                                    activeDraggingRoom = null
                                }
                                SidebarTab.Professors -> {
                                    activeDraggingSubject = null
                                    activeDraggingTeacher = item as Teacher
                                    activeDraggingRoom = null
                                }
                                SidebarTab.LectureRooms -> {
                                    activeDraggingSubject = null
                                    activeDraggingTeacher = null
                                    activeDraggingRoom = item as Classroom
                                }
                                else -> {}
                            }
                        },
                        onDrag = { dragAmount ->
                            dragCurrentOffset = dragCurrentOffset + dragAmount
                            dragGlobalPosition = dragGlobalPosition + dragAmount

                            // Find hovered slot
                            val hoverSlot = gridSlotsCoordinates.entries.find { entry ->
                                val coords = entry.value
                                if (coords.isAttached) {
                                    val size = coords.size
                                    val posInWindow = coords.positionInWindow()
                                    val bounds = Rect(
                                        left = posInWindow.x,
                                        top = posInWindow.y,
                                        right = posInWindow.x + size.width,
                                        bottom = posInWindow.y + size.height
                                    )
                                    bounds.contains(dragGlobalPosition)
                                } else {
                                    false
                                }
                            }
                            hoveredSlotKey = hoverSlot?.key
                        },
                        onDragEnd = {
                            val dropSlot = gridSlotsCoordinates.entries.find { entry ->
                                val coords = entry.value
                                if (coords.isAttached) {
                                    val size = coords.size
                                    val posInWindow = coords.positionInWindow()
                                    val bounds = Rect(
                                        left = posInWindow.x,
                                        top = posInWindow.y,
                                        right = posInWindow.x + size.width,
                                        bottom = posInWindow.y + size.height
                                    )
                                    bounds.contains(dragGlobalPosition)
                                } else {
                                    false
                                }
                            }
                            if (dropSlot != null) {
                                val parts = dropSlot.key.split("-")
                                val dropDay = parts[0]
                                val dropPeriodIdx = parts[1].toInt()

                                val cellAtSlot = cells.find { cell ->
                                    cell.day == dropDay && cell.periodIndex == dropPeriodIdx && when (viewType) {
                                        DashboardViewType.Section -> cell.section == activeSection
                                        DashboardViewType.Teacher -> cell.teacherId == activeTeacherId
                                        DashboardViewType.Classroom -> cell.classroomId == activeRoomId
                                    }
                                }

                                if (cellAtSlot != null) {
                                    // Drop onto active block
                                    when {
                                        activeDraggingSubject != null -> {
                                            viewModel.addManualCell(
                                                section = cellAtSlot.section,
                                                subjectId = activeDraggingSubject!!.id,
                                                teacherId = cellAtSlot.teacherId,
                                                classroomId = cellAtSlot.classroomId,
                                                day = dropDay,
                                                periodIndex = dropPeriodIdx
                                            )
                                        }
                                        activeDraggingTeacher != null -> {
                                            viewModel.addManualCell(
                                                section = cellAtSlot.section,
                                                subjectId = cellAtSlot.subjectId,
                                                teacherId = activeDraggingTeacher!!.id,
                                                classroomId = cellAtSlot.classroomId,
                                                day = dropDay,
                                                periodIndex = dropPeriodIdx
                                            )
                                        }
                                        activeDraggingRoom != null -> {
                                            viewModel.addManualCell(
                                                section = cellAtSlot.section,
                                                subjectId = cellAtSlot.subjectId,
                                                teacherId = cellAtSlot.teacherId,
                                                classroomId = activeDraggingRoom!!.id,
                                                day = dropDay,
                                                periodIndex = dropPeriodIdx
                                            )
                                        }
                                    }
                                } else {
                                    // Drop onto empty vacant block
                                    if (activeDraggingSubject != null) {
                                        val defaultTeacherId = activeDraggingSubject!!.teacherId ?: teachersList.firstOrNull()?.id ?: 1
                                        val defaultClassroomId = classroomsList.firstOrNull()?.id ?: 1
                                        viewModel.addManualCell(
                                            section = if (viewType == DashboardViewType.Section) activeSection else "Section A",
                                            subjectId = activeDraggingSubject!!.id,
                                            teacherId = defaultTeacherId,
                                            classroomId = defaultClassroomId,
                                            day = dropDay,
                                            periodIndex = dropPeriodIdx
                                        )
                                    }
                                }
                            }
                            // Clean up
                            isCurrentlyDragging = false
                            activeDraggingSubject = null
                            activeDraggingTeacher = null
                            activeDraggingRoom = null
                            dragCurrentOffset = Offset.Zero
                            hoveredSlotKey = null
                        }
                    )
                }
            }
        }

        if (isCurrentlyDragging) {
            val dragItemName = when {
                activeDraggingSubject != null -> "Curriculum: ${activeDraggingSubject!!.name}"
                activeDraggingTeacher != null -> "Faculty: ${activeDraggingTeacher!!.name}"
                activeDraggingRoom != null -> "Room: ${activeDraggingRoom!!.name}"
                else -> "Resource Component"
            }
            val hasHoverTarget = hoveredSlotKey != null
            val overlayScale by animateFloatAsState(
                targetValue = if (hasHoverTarget) 1.15f else 1.08f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            val overlayRotation by animateFloatAsState(
                targetValue = if (hasHoverTarget) 0f else 3.0f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            )
            val overlayAlpha by animateFloatAsState(targetValue = if (hasHoverTarget) 1.0f else 0.90f)
            val overlayBgColor by animateColorAsState(targetValue = if (hasHoverTarget) AccentViolet else AccentTeal)
            val overlayBorderColor by animateColorAsState(targetValue = if (hasHoverTarget) AccentVioletLight else AccentTealLight)

            Box(
                modifier = Modifier
                    .offset(
                        x = with(LocalDensity.current) { dragGlobalPosition.x.toDp() - 60.dp },
                        y = with(LocalDensity.current) { dragGlobalPosition.y.toDp() - 25.dp }
                    )
                    .graphicsLayer {
                        scaleX = overlayScale
                        scaleY = overlayScale
                        alpha = overlayAlpha
                        rotationZ = overlayRotation
                        shadowElevation = 8f
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(overlayBgColor.copy(alpha = 0.95f))
                    .border(1.2.dp, overlayBorderColor, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "dragging overlay icon",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dragItemName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }

    // High fidelity "Manual Slot Assignment" config dialog
    if (showAssignDialog) {
        Dialog(onDismissRequest = { showAssignDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.2.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Schedule Class Block",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Manual assignment parameters for $dialogSelectedDay at Period ${dialogSelectedPeriod + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Section Drop Select
                    Text(
                        text = "Target Class Section",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        sectionsAvailable.forEach { sec ->
                            val isSelected = dialogSection == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentTeal.copy(alpha = 0.15f) else DarkBg)
                                    .border(1.dp, if (isSelected) AccentTealLight else DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable { dialogSection = sec }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sec,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) AccentTealLight else TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subject Selection List Box picker
                    Text(
                        text = "Choose Subject Curriculum",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 130.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBg)
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column {
                            subjectsList.forEach { subj ->
                                val isSelected = dialogSubject?.id == subj.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) AccentTeal.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable {
                                            dialogSubject = subj
                                            // Auto assign subject handled professor if registered
                                            val mappedTeacher = teachersList.find { it.id == subj.teacherId }
                                            if (mappedTeacher != null) {
                                                dialogTeacher = mappedTeacher
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (subj.isLab) Icons.Default.Build else Icons.Default.Info,
                                        contentDescription = "Subject form",
                                        tint = if (subj.isLab) AccentVioletLight else AccentTealLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(subj.name, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                        Text("Code: ${subj.code} • ${subj.periodsPerWeek} times/wk Required", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Instructor Selector List picker
                    Text(
                        text = "Assign Dedicated Faculty Mentor",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 130.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBg)
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column {
                            teachersList.forEach { instructor ->
                                val isSelected = dialogTeacher?.id == instructor.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) AccentViolet.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable { dialogTeacher = instructor }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = "instructor icon", tint = AccentVioletLight, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(instructor.name, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                        Text("Dept: ${instructor.department} • Max Hours Allowed: ${instructor.maxHoursPerWeek}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lecture Hall Room list picker
                    Text(
                        text = "Reserve Classroom / Laboratory space",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 110.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBg)
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column {
                            classroomsList.forEach { rm ->
                                val isSelected = dialogRoom?.id == rm.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) AccentTeal.copy(alpha = 0.1f) else Color.Transparent)
                                        .clickable { dialogRoom = rm }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = "classroom icon", tint = AccentTealLight, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(rm.name, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                        Text("Capacity: ${rm.capacity} • Space: ${if (rm.isLab) "Laboratory Complex" else "Standard Hall"}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAssignDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dismiss")
                        }

                        Button(
                            onClick = {
                                if (dialogSubject != null && dialogTeacher != null && dialogRoom != null) {
                                    viewModel.addManualCell(
                                        section = dialogSection,
                                        subjectId = dialogSubject!!.id,
                                        teacherId = dialogTeacher!!.id,
                                        classroomId = dialogRoom!!.id,
                                        day = dialogSelectedDay,
                                        periodIndex = dialogSelectedPeriod
                                    )
                                    showAssignDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                            enabled = dialogSubject != null && dialogTeacher != null && dialogRoom != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Schedule", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showSavePresetDialog) {
        Dialog(onDismissRequest = { showSavePresetDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.2.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Save Current Layout as Preset",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "This will capture the current allocation of active classes (${cells.size} blocks), health score (${optimizationScore}%), and resource mappings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = presetSaveName,
                        onValueChange = { presetSaveName = it },
                        placeholder = { Text("e.g. Alternate Fall Layout-A", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentTeal,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("preset_name_input")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showSavePresetDialog = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (presetSaveName.isNotBlank()) {
                                    viewModel.saveCurrentAsPreset(presetSaveName)
                                    presetSaveName = ""
                                    showSavePresetDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                            enabled = presetSaveName.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("submit_preset_button")
                        ) {
                            Text("Save Preset", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceManagementSidebar(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    subjectsList: List<Subject>,
    teachersList: List<Teacher>,
    classroomsList: List<Classroom>,
    liveConflicts: List<String>,
    optimizationScore: Int,
    activeCellsCount: Int,
    paintSubject: Subject?,
    paintTeacher: Teacher?,
    paintRoom: Classroom?,
    paintSection: String,
    onPaintSubjectChange: (Subject?) -> Unit,
    onPaintTeacherChange: (Teacher?) -> Unit,
    onPaintRoomChange: (Classroom?) -> Unit,
    onPaintSectionChange: (String) -> Unit,
    onClearPaintBrush: () -> Unit,
    onDragStart: (Any, SidebarTab, LayoutCoordinates, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    var sidebarTab by remember { mutableStateOf(SidebarTab.Subjects) }

    Column(
        modifier = modifier
            .background(DarkSurface)
            .padding(14.dp)
    ) {
        // Analytics Summary Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkBg),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "System Real-time Health",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$optimizationScore%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (optimizationScore >= 80) AccentEmerald else if (optimizationScore >= 50) AccentAmber else AccentRose
                        )
                        Text(
                            text = "Roster Health",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = liveConflicts.size.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (liveConflicts.isEmpty()) AccentEmerald else AccentRose
                        )
                        Text(
                            text = "Active Clashes",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { optimizationScore / 100f },
                    color = if (optimizationScore >= 80) AccentEmerald else if (optimizationScore >= 50) AccentAmber else AccentRose,
                    trackColor = DarkBorder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$activeCellsCount Allocated Block Spaces",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Segmented Tabs For Resources
        TabRow(
            selectedTabIndex = sidebarTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = AccentTealLight,
            modifier = Modifier.fillMaxWidth()
        ) {
            SidebarTab.values().forEach { tab ->
                val tabLabel = when (tab) {
                    SidebarTab.Subjects -> "Subjects"
                    SidebarTab.Professors -> "Faculty"
                    SidebarTab.LectureRooms -> "Rooms"
                    SidebarTab.AiSuggester -> "★ AI Slots"
                }
                Tab(
                    selected = sidebarTab == tab,
                    onClick = { sidebarTab = tab },
                    text = {
                        Text(
                            text = tabLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Resource Paint brush configuration tool (Interactive Brush setup)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (paintSubject != null) AccentViolet.copy(alpha = 0.08f) else DarkBg
            ),
            border = BorderStroke(1.dp, if (paintSubject != null) AccentVioletLight.copy(alpha = 0.4f) else DarkBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Stamp Allocator",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (paintSubject != null) AccentVioletLight else TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (paintSubject != null) {
                        IconButton(
                            onClick = onClearPaintBrush,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "clear brush", tint = AccentRose, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Text(
                    text = if (paintSubject != null) "Load target: click any empty vacant grid block to stamp setup!" else "Tap any resource below to load it into the paintbrush helper tools.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (paintSubject != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✨ Curriculum: ${paintSubject.name} (${paintSubject.code})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "👤 Lecturer: ${paintTeacher?.name ?: "No teacher mapped"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "📍 Room/Lab: ${paintRoom?.name ?: "No lecture hall mapped"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Target allocation class:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        listOf("Section A", "Section B").forEach { sec ->
                            val isChosen = paintSection == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isChosen) AccentViolet.copy(alpha = 0.2f) else DarkBg)
                                    .border(1.dp, if (isChosen) AccentVioletLight else DarkBorder, RoundedCornerShape(6.dp))
                                    .clickable { onPaintSectionChange(sec) }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(sec, style = MaterialTheme.typography.labelSmall, color = if (isChosen) AccentVioletLight else TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic lists according to active Resource tab
        Box(modifier = Modifier.weight(1f)) {
            when (sidebarTab) {
                SidebarTab.Subjects -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(subjectsList) { subj ->
                            val isPaintedCurrent = paintSubject?.id == subj.id
                            ResourceItemCard(
                                title = "${subj.name} (${subj.code})",
                                subtitle = "Weekly periods required: ${subj.periodsPerWeek}",
                                infoText = if (subj.isLab) "LABORATORY" else "STANDARD LECTURE",
                                isSelected = isPaintedCurrent,
                                iconBg = if (subj.isLab) AccentViolet.copy(alpha = 0.15f) else AccentTeal.copy(alpha = 0.15f),
                                iconColor = if (subj.isLab) AccentVioletLight else AccentTealLight,
                                icon = if (subj.isLab) Icons.Default.Build else Icons.Default.Info,
                                onClick = {
                                    onPaintSubjectChange(subj)
                                    // Auto populate connected resources
                                    val mappedTeacher = teachersList.find { it.id == subj.teacherId }
                                    if (mappedTeacher != null) onPaintTeacherChange(mappedTeacher)
                                    if (classroomsList.isNotEmpty()) {
                                        val matchedRoom = classroomsList.find { it.isLab == subj.isLab } ?: classroomsList.first()
                                        onPaintRoomChange(matchedRoom)
                                    }
                                },
                                associatedItem = subj,
                                tab = SidebarTab.Subjects,
                                onDragStart = onDragStart,
                                onDrag = onDrag,
                                onDragEnd = onDragEnd
                            )
                        }
                    }
                }
                SidebarTab.Professors -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(teachersList) { instructor ->
                            val isPaintedCurrent = paintTeacher?.id == instructor.id
                            ResourceItemCard(
                                title = instructor.name,
                                subtitle = "EmpID: ${instructor.employeeId} • ${instructor.designation}",
                                infoText = "Hours limit: ${instructor.maxHoursPerWeek} hrs",
                                isSelected = isPaintedCurrent,
                                iconBg = AccentViolet.copy(alpha = 0.12f),
                                iconColor = AccentVioletLight,
                                icon = Icons.Default.Person,
                                onClick = {
                                    if (paintSubject != null) {
                                        onPaintTeacherChange(instructor)
                                    } else {
                                        // Auto load a placeholder subject for this teacher to paint immediately
                                        val sub = subjectsList.find { it.teacherId == instructor.id }
                                        if (sub != null) {
                                            onPaintSubjectChange(sub)
                                        }
                                        onPaintTeacherChange(instructor)
                                        if (classroomsList.isNotEmpty()) {
                                            onPaintRoomChange(classroomsList.first())
                                        }
                                    }
                                },
                                associatedItem = instructor,
                                tab = SidebarTab.Professors,
                                onDragStart = onDragStart,
                                onDrag = onDrag,
                                onDragEnd = onDragEnd
                            )
                        }
                    }
                }
                SidebarTab.LectureRooms -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(classroomsList) { rm ->
                            val isPaintedCurrent = paintRoom?.id == rm.id
                            ResourceItemCard(
                                title = rm.name,
                                subtitle = "Hall Capacity: ${rm.capacity} seats",
                                infoText = if (rm.isLab) "LAB STATION" else "LECTURE CLASSROOM",
                                isSelected = isPaintedCurrent,
                                iconBg = AccentTeal.copy(alpha = 0.12f),
                                iconColor = AccentTealLight,
                                icon = Icons.Default.Place,
                                onClick = {
                                    onPaintRoomChange(rm)
                                },
                                associatedItem = rm,
                                tab = SidebarTab.LectureRooms,
                                onDragStart = onDragStart,
                                onDrag = onDrag,
                                onDragEnd = onDragEnd
                            )
                        }
                    }
                }
                SidebarTab.AiSuggester -> {
                    AiSuggesterTabContent(
                        viewModel = viewModel,
                        subjectsList = subjectsList,
                        teachersList = teachersList,
                        classroomsList = classroomsList
                    )
                }
            }
        }
    }
}

@Composable
fun ResourceItemCard(
    title: String,
    subtitle: String,
    infoText: String,
    isSelected: Boolean,
    iconBg: Color,
    iconColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    associatedItem: Any,
    tab: SidebarTab,
    onDragStart: (Any, SidebarTab, LayoutCoordinates, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    var cardCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var isDraggingThis by remember { mutableStateOf(false) }

    val cardScale by animateFloatAsState(
        targetValue = if (isDraggingThis) 0.94f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )
    val cardAlpha by animateFloatAsState(targetValue = if (isDraggingThis) 0.5f else 1.0f)
    val dotsColor by animateColorAsState(targetValue = if (isDraggingThis) AccentTealLight else TextMuted.copy(alpha = 0.6f))
    val borderWidth by animateDpAsState(targetValue = if (isDraggingThis) 2.dp else if (isSelected) 1.5.dp else 1.dp)
    val borderColor by animateColorAsState(targetValue = if (isDraggingThis) AccentTealLight else if (isSelected) AccentTealLight else DarkBorder)
    val containerColor by animateColorAsState(targetValue = if (isDraggingThis) AccentTeal.copy(alpha = 0.05f) else if (isSelected) AccentTeal.copy(alpha = 0.1f) else DarkBg)

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(width = borderWidth, color = borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
                alpha = cardAlpha
            }
            .clickable { onClick() }
            .onGloballyPositioned { coords ->
                cardCoordinates = coords
            }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag-and-drop indicator dots drag handles
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .pointerInput(associatedItem) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDraggingThis = true
                                cardCoordinates?.let { coords ->
                                    onDragStart(associatedItem, tab, coords, offset)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount)
                            },
                            onDragEnd = {
                                isDraggingThis = false
                                onDragEnd()
                            },
                            onDragCancel = {
                                isDraggingThis = false
                                onDragEnd()
                            }
                        )
                    }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    repeat(3) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(2) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(dotsColor)
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = "Resource symbol", tint = iconColor, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Active brush component",
                    tint = AccentTealLight,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyScheduleWorkspace(
    onSolve: () -> Unit,
    onSeed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "empty database layout",
                    tint = AccentTealLight,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Weekly Schedule Board is Blank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "There are no academic entries registered yet. Deploy your auto constraint-scheduler solver or seed templates to start.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSolve,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Construct with AI", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = onSeed,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentViolet),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text("Load Layout Presets", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun MasterCalendarGridMatrix(
    cells: List<TimetableCell>,
    subjectsList: List<Subject>,
    teachersList: List<Teacher>,
    classroomsList: List<Classroom>,
    viewType: DashboardViewType,
    activeSection: String,
    activeTeacherId: Int?,
    activeRoomId: Int?,
    swappingCell: TimetableCell?,
    paintSubject: Subject?,
    periodTimes: List<String>,
    onSelectCellSwap: (TimetableCell) -> Unit,
    onDeleteCell: (Int) -> Unit,
    onGridSlotClicked: (String, Int) -> Unit,
    isCurrentlyDragging: Boolean,
    hoveredSlotKey: String?,
    gridSlotsCoordinates: MutableMap<String, LayoutCoordinates>
) {
    val horScrollState = rememberScrollState()
    val verScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horScrollState)
                .verticalScroll(verScrollState)
                .padding(8.dp)
        ) {
            // Monday through Friday headers
            Row(
                modifier = Modifier
                    .background(DarkBg)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time block spacer corner
                Box(
                    modifier = Modifier
                        .width(78.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PERIOD",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }

                ScheduleEngine.WORKING_DAYS.forEach { day ->
                    Box(
                        modifier = Modifier
                            .width(138.dp)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.uppercase(),
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

            // Run standard period loop
            (0 until ScheduleEngine.NUM_PERIODS).forEach { periodIdx ->
                val isLunch = periodIdx == ScheduleEngine.LUNCH_PERIOD

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time Left Column Cell
                    Column(
                        modifier = Modifier
                            .width(78.dp)
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
                            color = TextMuted,
                            fontSize = 8.sp
                        )
                    }

                    if (isLunch) {
                        // Midday break strip
                        Box(
                            modifier = Modifier
                                .width(138.dp * 5)
                                .height(56.dp)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBorder.copy(alpha = 0.4f))
                                .border(1.dp, DarkBorder, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = "Midbreak icon", tint = TextMuted, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Midday Lunch Rest window (12:00 PM - 01:00 PM)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Regular day loops mapped to cells
                        ScheduleEngine.WORKING_DAYS.forEach { dayName ->
                            val slotKey = "$dayName-$periodIdx"
                            val matchedCell = cells.find { cell ->
                                cell.day == dayName && cell.periodIndex == periodIdx && when (viewType) {
                                    DashboardViewType.Section -> cell.section == activeSection
                                    DashboardViewType.Teacher -> cell.teacherId == activeTeacherId
                                    DashboardViewType.Classroom -> cell.classroomId == activeRoomId
                                }
                            }

                            val isHovered = hoveredSlotKey == slotKey

                            if (matchedCell != null) {
                                val subj = subjectsList.find { it.id == matchedCell.subjectId }
                                val teacher = teachersList.find { it.id == matchedCell.teacherId }
                                val rm = classroomsList.find { it.id == matchedCell.classroomId }

                                val isSwappingThis = swappingCell?.id == matchedCell.id
                                val isAnySwapping = swappingCell != null

                                val cellScale by animateFloatAsState(
                                    targetValue = if (isHovered) 1.05f else 1.0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                                )
                                val cellBgColor by animateColorAsState(
                                    targetValue = when {
                                        isHovered -> AccentTeal.copy(alpha = 0.22f)
                                        isSwappingThis -> AccentViolet.copy(alpha = 0.18f)
                                        else -> DarkBg
                                    },
                                    animationSpec = tween(durationMillis = 200)
                                )
                                val cellBorderWidth by animateDpAsState(
                                    targetValue = if (isHovered || isSwappingThis) 2.dp else 1.dp,
                                    animationSpec = tween(durationMillis = 200)
                                )
                                val cellBorderColor by animateColorAsState(
                                    targetValue = when {
                                        isHovered -> AccentTealLight
                                        isSwappingThis -> AccentVioletLight
                                        else -> DarkBorder
                                    },
                                    animationSpec = tween(durationMillis = 200)
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = cellBgColor),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(width = cellBorderWidth, color = cellBorderColor),
                                    modifier = Modifier
                                        .width(138.dp)
                                        .height(115.dp)
                                        .padding(horizontal = 4.dp)
                                        .graphicsLayer {
                                            scaleX = cellScale
                                            scaleY = cellScale
                                        }
                                        .clickable { onSelectCellSwap(matchedCell) }
                                        .onGloballyPositioned { coords ->
                                            gridSlotsCoordinates[slotKey] = coords
                                        }
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
                                                    text = if (subj?.isLab == true) "LAB" else "LECTURE",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (subj?.isLab == true) AccentVioletLight else AccentTealLight,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 8.sp
                                                )

                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Drop period",
                                                    tint = TextMuted,
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clickable { onDeleteCell(matchedCell.id) }
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(3.dp))

                                            Text(
                                                text = subj?.name ?: "Curriculum Class",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 11.sp
                                            )
                                        }

                                        Text(
                                            text = when (viewType) {
                                                DashboardViewType.Section -> "${teacher?.name?.split(" ")?.lastOrNull() ?: "Faculty"} • RM:${rm?.name ?: "N/A"}"
                                                DashboardViewType.Teacher -> "Sec:${matchedCell.section} • RM:${rm?.name ?: "N/A"}"
                                                DashboardViewType.Classroom -> "Sec:${matchedCell.section} • ${teacher?.name?.split(" ")?.lastOrNull() ?: "Faculty"}"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            } else {
                                // Empty Vacant slot configuration box
                                val paintbrushActive = paintSubject != null

                                val vacantScale by animateFloatAsState(
                                    targetValue = if (isHovered) 1.05f else 1.0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                                )
                                val vacantBgColor by animateColorAsState(
                                    targetValue = when {
                                        isHovered -> AccentTeal.copy(alpha = 0.15f)
                                        isCurrentlyDragging -> AccentViolet.copy(alpha = 0.08f)
                                        paintbrushActive -> AccentViolet.copy(alpha = 0.05f)
                                        else -> Color.Transparent
                                    },
                                    animationSpec = tween(durationMillis = 200)
                                )
                                val vacantBorderWidth by animateDpAsState(
                                    targetValue = if (isHovered) 2.dp else 1.dp,
                                    animationSpec = tween(durationMillis = 200)
                                )
                                val vacantBorderColor by animateColorAsState(
                                    targetValue = when {
                                        isHovered -> AccentTealLight
                                        isCurrentlyDragging -> AccentVioletLight.copy(alpha = 0.6f)
                                        paintbrushActive -> AccentVioletLight.copy(alpha = 0.5f)
                                        else -> DarkBorder.copy(alpha = 0.5f)
                                    },
                                    animationSpec = tween(durationMillis = 200)
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = vacantBgColor),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(width = vacantBorderWidth, color = vacantBorderColor),
                                    modifier = Modifier
                                        .width(138.dp)
                                        .height(115.dp)
                                        .padding(horizontal = 4.dp)
                                        .graphicsLayer {
                                            scaleX = vacantScale
                                            scaleY = vacantScale
                                        }
                                        .clickable { onGridSlotClicked(dayName, periodIdx) }
                                        .onGloballyPositioned { coords ->
                                            gridSlotsCoordinates[slotKey] = coords
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                isHovered -> Icons.Default.AddCircle
                                                isCurrentlyDragging -> Icons.Default.AddCircle
                                                paintbrushActive -> Icons.Default.AddCircle
                                                else -> Icons.Default.Add
                                            },
                                            contentDescription = "vacant space placeholder",
                                            tint = when {
                                                isHovered -> AccentTealLight
                                                isCurrentlyDragging -> AccentVioletLight
                                                paintbrushActive -> AccentVioletLight
                                                else -> TextMuted
                                            },
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = when {
                                                isHovered -> "RELEASE DROP"
                                                isCurrentlyDragging -> "VACANT SLOT"
                                                paintbrushActive -> "STAMP HERE"
                                                else -> "Vacant"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when {
                                                isHovered -> AccentTealLight
                                                isCurrentlyDragging -> AccentVioletLight
                                                paintbrushActive -> AccentVioletLight
                                                else -> TextMuted
                                            },
                                            fontWeight = if (isHovered || isCurrentlyDragging || paintbrushActive) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 8.sp,
                                            textAlign = TextAlign.Center
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
}

@Composable
fun MasterRosterListView(
    cells: List<TimetableCell>,
    subjectsList: List<Subject>,
    teachersList: List<Teacher>,
    classroomsList: List<Classroom>,
    viewType: DashboardViewType,
    activeSection: String,
    activeTeacherId: Int?,
    activeRoomId: Int?,
    swappingCell: TimetableCell?,
    periodTimes: List<String>,
    onSelectCellSwap: (TimetableCell) -> Unit,
    onDeleteCell: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ScheduleEngine.WORKING_DAYS) { day ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = day.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AccentTealLight
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Inner list cells for day periods
                    (0 until ScheduleEngine.NUM_PERIODS).forEach { periodIndex ->
                        val isLunch = periodIndex == ScheduleEngine.LUNCH_PERIOD

                        if (isLunch) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkBorder.copy(alpha = 0.2f))
                                    .padding(vertical = 8.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "Period ${periodIndex + 1}: Lunch Hour (12:00 PM - 01:00 PM)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            val cell = cells.find { c ->
                                c.day == day && c.periodIndex == periodIndex && when (viewType) {
                                    DashboardViewType.Section -> c.section == activeSection
                                    DashboardViewType.Teacher -> c.teacherId == activeTeacherId
                                    DashboardViewType.Classroom -> c.classroomId == activeRoomId
                                }
                            }

                            if (cell != null) {
                                val subj = subjectsList.find { it.id == cell.subjectId }
                                val instructor = teachersList.find { it.id == cell.teacherId }
                                val room = classroomsList.find { it.id == cell.classroomId }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkBg)
                                        .border(1.dp, if (swappingCell?.id == cell.id) AccentVioletLight else DarkBorder, RoundedCornerShape(8.dp))
                                        .clickable { onSelectCellSwap(cell) }
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Period ${periodIndex + 1} (${periodTimes[periodIndex].split(" ").first()})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (subj?.isLab == true) AccentVioletLight else AccentTealLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = subj?.name ?: "Unknown Curriculum",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = when (viewType) {
                                                DashboardViewType.Section -> "Faculty: ${instructor?.name ?: "N/A"} • Room: ${room?.name ?: "N/A"}"
                                                DashboardViewType.Teacher -> "Class: ${cell.section} • Room: ${room?.name ?: "N/A"}"
                                                DashboardViewType.Classroom -> "Class: ${cell.section} • Faculty: ${instructor?.name ?: "N/A"}"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteCell(cell.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove block", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(1.dp, DarkBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Period ${periodIndex + 1}: Unallocated Slot Space",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    Icon(Icons.Default.Add, contentDescription = "vacant", tint = TextMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T> CustomSelectorField(
    label: String,
    selectedValueText: String,
    items: List<T>,
    itemToText: (T) -> String,
    onItemSelected: (T) -> Unit,
    placeholder: String = "Select option"
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DarkBg)
                .border(1.dp, if (expanded) AccentTealLight else DarkBorder, RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedValueText.ifEmpty { placeholder },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedValueText.isEmpty()) TextMuted else TextPrimary,
                    fontWeight = if (selectedValueText.isEmpty()) FontWeight.Normal else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Expand menu",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 160.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    items.forEach { item ->
                        val text = itemToText(item)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onItemSelected(item)
                                    expanded = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(text, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiSuggesterTabContent(
    viewModel: MainViewModel,
    subjectsList: List<Subject>,
    teachersList: List<Teacher>,
    classroomsList: List<Classroom>
) {
    var aiSelectedSubject by remember { mutableStateOf<Subject?>(null) }
    var aiSelectedTeacher by remember { mutableStateOf<Teacher?>(null) }
    var aiSelectedRoom by remember { mutableStateOf<Classroom?>(null) }
    var aiSelectedSection by remember { mutableStateOf("Section A") }

    val recommendations by viewModel.slotRecommendations.collectAsState()
    val enrichmentText by viewModel.aiRecommendationEnrichment.collectAsState()
    val isCalculating by viewModel.isCalculatingRecommendations.collectAsState()

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
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkBg),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Configure Targeting",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CustomSelectorField(
                    label = "Target Subject",
                    selectedValueText = aiSelectedSubject?.let { "${it.name} (${it.code})" } ?: "",
                    items = subjectsList,
                    itemToText = { "${it.name} (${it.code})" },
                    onItemSelected = { subj ->
                        aiSelectedSubject = subj
                        val prof = teachersList.find { it.id == subj.teacherId }
                        if (prof != null) {
                            aiSelectedTeacher = prof
                        }
                        if (classroomsList.isNotEmpty()) {
                            aiSelectedRoom = classroomsList.find { it.isLab == subj.isLab } ?: classroomsList.first()
                        }
                    },
                    placeholder = "Select Target Subject"
                )

                CustomSelectorField(
                    label = "Target Group / Section",
                    selectedValueText = aiSelectedSection,
                    items = listOf("Section A", "Section B"),
                    itemToText = { it },
                    onItemSelected = { aiSelectedSection = it }
                )

                CustomSelectorField(
                    label = "Assign Teacher/Professor",
                    selectedValueText = aiSelectedTeacher?.name ?: "",
                    items = teachersList,
                    itemToText = { it.name },
                    onItemSelected = { aiSelectedTeacher = it },
                    placeholder = "Select Teacher"
                )

                CustomSelectorField(
                    label = "Assign Classroom",
                    selectedValueText = aiSelectedRoom?.name ?: "",
                    items = classroomsList,
                    itemToText = { it.name },
                    onItemSelected = { aiSelectedRoom = it },
                    placeholder = "Select Classroom"
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        viewModel.getSlotRecommendations(
                            subject = aiSelectedSubject,
                            teacher = aiSelectedTeacher,
                            classroom = aiSelectedRoom,
                            section = aiSelectedSection
                        )
                    },
                    enabled = aiSelectedSubject != null && aiSelectedTeacher != null && aiSelectedRoom != null && !isCalculating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentTeal,
                        disabledContainerColor = DarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isCalculating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing Schedules...", style = MaterialTheme.typography.labelSmall)
                    } else {
                        Icon(Icons.Default.Star, contentDescription = "suggest button", modifier = Modifier.size(15.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Find Best Available Slot", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
        }

        if (isCalculating) {
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentTealLight, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Predicting availability trends...", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
        } else if (recommendations.isNotEmpty()) {
            Text(
                text = "Recommended Open Periods",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            recommendations.forEach { slot ->
                val timeSpanText = periodTimes.getOrNull(slot.periodIndex) ?: "Standard time slot"
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    border = BorderStroke(1.2.dp, if (slot.score >= 80) AccentEmerald.copy(alpha = 0.4f) else AccentAmber.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${slot.day} - Period ${slot.periodIndex + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = timeSpanText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (slot.score >= 80) AccentEmerald.copy(alpha = 0.15f) else AccentAmber.copy(alpha = 0.15f))
                                    .border(1.dp, if (slot.score >= 80) AccentEmerald else AccentAmber, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${slot.score}% Optimal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (slot.score >= 80) AccentEmerald else AccentAmber,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = slot.suitabilityReason,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (aiSelectedSubject != null && aiSelectedTeacher != null && aiSelectedRoom != null) {
                                    viewModel.addManualCell(
                                        section = aiSelectedSection,
                                        subjectId = aiSelectedSubject!!.id,
                                        teacherId = aiSelectedTeacher!!.id,
                                        classroomId = aiSelectedRoom!!.id,
                                        day = slot.day,
                                        periodIndex = slot.periodIndex
                                    )
                                    viewModel.clearSlotRecommendations()
                                    aiSelectedSubject = null
                                    aiSelectedTeacher = null
                                    aiSelectedRoom = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (slot.score >= 80) AccentEmerald else AccentTeal),
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Schedule Event", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (enrichmentText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = AccentViolet.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, AccentVioletLight.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "AI advice icon", tint = AccentVioletLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Trend Insights",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentVioletLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = enrichmentText.trim(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "empty sug icon",
                        tint = TextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Specify resources above to run AI slot validation and availability scanning.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}
