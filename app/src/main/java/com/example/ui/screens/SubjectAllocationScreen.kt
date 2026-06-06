package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.Subject
import com.example.data.Teacher
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun SubjectAllocationScreen(viewModel: MainViewModel) {
    val subjectsList by viewModel.subjects.collectAsState()
    val teachersList by viewModel.teachers.collectAsState()
    val departmentsList by viewModel.departments.collectAsState()

    var showForm by remember { mutableStateOf(false) }

    // Forms state
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf("") }
    var selectedTeacherId by remember { mutableStateOf<Int?>(null) }
    var periodsPerWeek by remember { mutableStateOf("3") }
    var isLab by remember { mutableStateOf(false) }
    var consecutiveRequired by remember { mutableStateOf("1") }
    var preferredClassrooms by remember { mutableStateOf("") }

    LaunchedEffect(departmentsList, teachersList) {
        if (departmentsList.isNotEmpty() && selectedDept.isEmpty()) {
            selectedDept = departmentsList.first().name
        }
        if (teachersList.isNotEmpty() && selectedTeacherId == null) {
            selectedTeacherId = teachersList.first().id
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // High Admin Header Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(top = 16.dp, bottom = 18.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Subject Allocations", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text("Assign subject lines to academic faculties.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(containerColor = if (showForm) AccentViolet else AccentTeal),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(if (showForm) Icons.Default.Close else Icons.Default.Add, contentDescription = "Add Allocation")
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (showForm) "Close Form" else "Add Allocation", color = Color.White)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showForm) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Map Subject to Faculty", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Subject Name", color = TextSecondary) },
                                placeholder = { Text("e.g. Database Management Systems", color = TextMuted) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentTealLight,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = code,
                                    onValueChange = { code = it },
                                    label = { Text("Subject Code", color = TextSecondary) },
                                    placeholder = { Text("CS202", color = TextMuted) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTealLight,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = periodsPerWeek,
                                    onValueChange = { periodsPerWeek = it },
                                    label = { Text("Periods / Wk", color = TextSecondary) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTealLight,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Department dropdown-like picker selection
                            Text("Active Department Stream", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                departmentsList.forEach { dept ->
                                    val isSel = selectedDept == dept.name
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) AccentTeal.copy(alpha = 0.2f) else Color.Transparent)
                                            .border(1.dp, if (isSel) AccentTealLight else DarkBorder, RoundedCornerShape(8.dp))
                                            .clickable { selectedDept = dept.name }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(dept.code, color = if (isSel) TextPrimary else TextSecondary, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Assigned Teacher selection dropdown-like list
                            Text("Assigned Teaching Faculty", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                teachersList.forEach { teacher ->
                                    val isSel = selectedTeacherId == teacher.id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) AccentViolet.copy(alpha = 0.2f) else Color.Transparent)
                                            .border(1.dp, if (isSel) AccentVioletLight else DarkBorder, RoundedCornerShape(8.dp))
                                            .clickable { selectedTeacherId = teacher.id }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(teacher.name.split(" ").lastOrNull() ?: teacher.name, color = if (isSel) TextPrimary else TextSecondary, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = isLab,
                                        onCheckedChange = { isLab = it },
                                        colors = CheckboxDefaults.colors(checkedColor = AccentTealLight)
                                    )
                                    Text("Requires Technology Lab Slot", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                                }

                                if (isLab) {
                                    OutlinedTextField(
                                        value = consecutiveRequired,
                                        onValueChange = { consecutiveRequired = it },
                                        label = { Text("Block Slots", color = TextSecondary) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentTealLight,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        singleLine = true,
                                        modifier = Modifier.width(90.dp)
                                    )
                                }
                            }

                            if (isLab) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = preferredClassrooms,
                                    onValueChange = { preferredClassrooms = it },
                                    label = { Text("Preferred Special Classrooms / Labs", color = TextSecondary) },
                                    placeholder = { Text("e.g. CS Networks Lab", color = TextMuted) },
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

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (name.isNotBlank() && code.isNotBlank()) {
                                        val p = periodsPerWeek.toIntOrNull() ?: 3
                                        val consec = consecutiveRequired.toIntOrNull() ?: 1

                                        viewModel.addSubject(
                                            name = name,
                                            code = code,
                                            dept = selectedDept,
                                            teacherId = selectedTeacherId,
                                            periods = p,
                                            isLab = isLab,
                                            consecutive = consec,
                                            rooms = preferredClassrooms
                                        )

                                        // Reset
                                        name = ""
                                        code = ""
                                        periodsPerWeek = "3"
                                        isLab = false
                                        consecutiveRequired = "1"
                                        preferredClassrooms = ""
                                        showForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Commit Subject Allocation", color = Color.White)
                            }
                        }
                    }
                }
            }

            items(subjectsList) { subject ->
                val assignedTeacher = teachersList.find { it.id == subject.teacherId }

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (subject.isLab) AccentViolet.copy(alpha = 0.2f) else AccentTeal.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (subject.isLab) "Lab" else "Lect",
                                        color = if (subject.isLab) AccentVioletLight else AccentTealLight,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Text(
                                text = "Code: ${subject.code} • Stream: ${subject.department} • ${subject.periodsPerWeek} periods allocated",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = "Teacher icon", tint = AccentVioletLight, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Instructor: ${assignedTeacher?.name ?: "Vacant/Unassigned"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }

                            if (subject.isLab && subject.preferredClassrooms.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Room target", tint = AccentRose, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Room Limit: ${subject.preferredClassrooms} (${subject.consecutivePeriodsRequired} block hrs required)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AccentRose
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.deleteSubject(subject) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRose)
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
                onClick = { viewModel.navigateTo(Screen.TeacherManagement) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                Text("Teacher Directory", color = TextPrimary)
            }

            Button(
                onClick = { viewModel.navigateTo(Screen.Generator) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
            ) {
                Text("AI Scheduling Engine", color = Color.White)
                Icon(Icons.Default.ArrowForward, contentDescription = "Continue", tint = Color.White)
            }
        }
    }
}
