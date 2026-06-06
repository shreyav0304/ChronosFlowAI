package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.data.Teacher
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun TeacherManagementScreen(viewModel: MainViewModel) {
    val teachersList by viewModel.teachers.collectAsState()
    val departmentsList by viewModel.departments.collectAsState()

    var showForm by remember { mutableStateOf(false) }

    // Forms State
    var name by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf("") }
    var designation by remember { mutableStateOf("Professor") }
    var subjectsHandled by remember { mutableStateOf("") }
    var maxHours by remember { mutableStateOf("16") }
    var labEligibility by remember { mutableStateOf(true) }

    // Availability constraints helper
    var blockMon0 by remember { mutableStateOf(false) }
    var blockWedPM by remember { mutableStateOf(false) }
    var blockFriPM by remember { mutableStateOf(false) }

    LaunchedEffect(departmentsList) {
        if (departmentsList.isNotEmpty() && selectedDept.isEmpty()) {
            selectedDept = departmentsList.first().name
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Upper Admin Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(top = 16.dp, bottom = 18.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Teacher Directory", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text("Manage constraints of teaching staff.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(containerColor = if (showForm) AccentViolet else AccentTeal),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(if (showForm) Icons.Default.Close else Icons.Default.Add, contentDescription = "Toggle")
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (showForm) "Close Form" else "Add Instructor", color = Color.White)
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
                            Text("New Teacher Specifications", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name", color = TextSecondary) },
                                placeholder = { Text("e.g. Dr. Arthur Smith", color = TextMuted) },
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
                                    value = employeeId,
                                    onValueChange = { employeeId = it },
                                    label = { Text("Employee ID", color = TextSecondary) },
                                    placeholder = { Text("EMP01", color = TextMuted) },
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
                                    value = designation,
                                    onValueChange = { designation = it },
                                    label = { Text("Designation", color = TextSecondary) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTealLight,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = subjectsHandled,
                                onValueChange = { subjectsHandled = it },
                                label = { Text("Subjects Handled (comma-separated)", color = TextSecondary) },
                                placeholder = { Text("Mathematics, Statistics", color = TextMuted) },
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

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = labEligibility,
                                        onCheckedChange = { labEligibility = it },
                                        colors = CheckboxDefaults.colors(checkedColor = AccentTealLight)
                                    )
                                    Text("Lab Eligible", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                                }

                                OutlinedTextField(
                                    value = maxHours,
                                    onValueChange = { maxHours = it },
                                    label = { Text("Max hrs/wk", color = TextSecondary) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTealLight,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.width(110.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Constraints Selection
                            Text("Unavailable Block Periods", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = blockMon0,
                                    onClick = { blockMon0 = !blockMon0 },
                                    label = { Text("Mon Early Hour") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentRose.copy(alpha = 0.2f),
                                        selectedLabelColor = AccentRose
                                    )
                                )

                                FilterChip(
                                    selected = blockWedPM,
                                    onClick = { blockWedPM = !blockWedPM },
                                    label = { Text("Wed Afternoon") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentRose.copy(alpha = 0.2f),
                                        selectedLabelColor = AccentRose
                                    )
                                )

                                FilterChip(
                                    selected = blockFriPM,
                                    onClick = { blockFriPM = !blockFriPM },
                                    label = { Text("Fri Late Hour") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentRose.copy(alpha = 0.2f),
                                        selectedLabelColor = AccentRose
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (name.isNotBlank() && employeeId.isNotBlank()) {
                                        // Collect blocked constraints
                                        val constraints = mutableListOf<String>()
                                        if (blockMon0) constraints.add("Monday-0")
                                        if (blockWedPM) {
                                            constraints.add("Wednesday-4")
                                            constraints.add("Wednesday-5")
                                        }
                                        if (blockFriPM) constraints.add("Friday-5")

                                        val deptNameStr = if (selectedDept.isNotEmpty()) selectedDept else "Computer Science"

                                        viewModel.addTeacher(
                                            name = name,
                                            empId = employeeId,
                                            dept = deptNameStr,
                                            subjects = subjectsHandled,
                                            maxHours = maxHours.toIntOrNull() ?: 16,
                                            freeSlots = "Friday-4", // Auto preference slots
                                            teachingSlots = "Monday-1,Tuesday-0",
                                            unavailableSlots = constraints.joinToString(","),
                                            labEligible = labEligibility,
                                            desig = designation
                                        )

                                        // Reset Form
                                        name = ""
                                        employeeId = ""
                                        designation = "Professor"
                                        subjectsHandled = ""
                                        maxHours = "16"
                                        labEligibility = true
                                        blockMon0 = false
                                        blockWedPM = false
                                        blockFriPM = false
                                        showForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Register Teacher Account", color = Color.White)
                            }
                        }
                    }
                }
            }

            items(teachersList) { teacher ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = teacher.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "• ${teacher.designation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Text(
                                text = "Employee ID: ${teacher.employeeId} • Dept: ${teacher.department}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Subjects handles badge list
                            Text(
                                text = "Assigned subjects: ${teacher.subjectsHandled}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AccentTealLight
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Constraints display Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column {
                                    Text("Preferences & Limits:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        text = "${teacher.maxHoursPerWeek} hrs limit • ${if (teacher.labEligibility) "Lab Qualified" else "Theoretical sessions only"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }

                                if (teacher.availabilityConstraints.isNotBlank()) {
                                    Column {
                                        Text("Unavailable Slots:", style = MaterialTheme.typography.labelSmall, color = AccentRose)
                                        Text(
                                            text = teacher.availabilityConstraints,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AccentRose
                                        )
                                    }
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.deleteTeacher(teacher) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Teacher", tint = AccentRose)
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
                onClick = { viewModel.navigateTo(Screen.SetupWizard) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                Text("Setup Wizard", color = TextPrimary)
            }

            Button(
                onClick = { viewModel.navigateTo(Screen.SubjectAllocation) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
            ) {
                Text("Subject Allocation", color = Color.White)
                Icon(Icons.Default.ArrowForward, contentDescription = "Continue", tint = Color.White)
            }
        }
    }
}
