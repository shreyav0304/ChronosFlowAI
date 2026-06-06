package com.example.ui.screens

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable

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
import androidx.compose.ui.unit.dp
import com.example.data.Classroom
import com.example.data.Department
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun SetupWizardScreen(viewModel: MainViewModel) {
    var wizardStep by remember { mutableStateOf(1) } // Step 1: Departments, Step 2: Rooms

    val departmentsList by viewModel.departments.collectAsState()
    val classroomsList by viewModel.classrooms.collectAsState()

    // Forms
    var newDeptName by remember { mutableStateOf("") }
    var newDeptCode by remember { mutableStateOf("") }

    var newRoomName by remember { mutableStateOf("") }
    var newRoomCapacity by remember { mutableStateOf("60") }
    var newRoomIsLab by remember { mutableStateOf(false) }
    var newRoomEquipment by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Wizard Header Status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(top = 16.dp, bottom = 18.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Institution Setup Wizard",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Step $wizardStep of 2",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentTealLight
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress indicators tracker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (wizardStep >= 1) AccentTeal else DarkBorder)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (wizardStep >= 2) AccentTeal else DarkBorder)
                    )
                }
            }
        }

        // Action Body
        Box(modifier = Modifier.weight(1f)) {
            if (wizardStep == 1) {
                // STEP 1: DEPARTMENTS setup
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Configure Active Departments",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Establish study streams. Standard defaults have been pre-seeded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // User Friendliness: Preset Onboarding Shortcut
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AccentTeal.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.forceSeed()
                                viewModel.navigateTo(Screen.Dashboard)
                            }
                            .padding(bottom = 16.dp)
                            .border(1.dp, AccentTealLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Quick load setup",
                                tint = AccentTealLight,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Seed Demo Academic Preset",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Skips manual entry to seed departments, classrooms, faculty, and optimized timetables in 1-tap.",
                                    color = TextSecondary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Load",
                                tint = AccentTealLight
                            )
                        }
                    }

                    // Department Form Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = newDeptName,
                                    onValueChange = { newDeptName = it },
                                    label = { Text("Dept Name", color = TextSecondary) },
                                    placeholder = { Text("e.g. Mechanical Eng.", color = TextMuted) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTealLight,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(1.5f)
                                )

                                OutlinedTextField(
                                    value = newDeptCode,
                                    onValueChange = { newDeptCode = it },
                                    label = { Text("Code", color = TextSecondary) },
                                    placeholder = { Text("e.g. ME", color = TextMuted) },
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

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (newDeptName.isNotBlank() && newDeptCode.isNotBlank()) {
                                        viewModel.addDepartment(newDeptName, newDeptCode)
                                        newDeptName = ""
                                        newDeptCode = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Text("Add Department", color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Existing Departments list
                    Text(
                        text = "Current Registry (${departmentsList.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(departmentsList) { dept ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = "Dept indicator",
                                        tint = AccentTealLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${dept.name} (${dept.code})",
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteDepartment(dept) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRose)
                                }
                            }
                        }
                    }
                }
            } else {
                // STEP 2: CLASSROOMS & LABORATORIES setup
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Add Classrooms & Technology Labs",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Define locations. Assign hardware configurations for computing/science labs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Room Form Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = newRoomName,
                                    onValueChange = { newRoomName = it },
                                    label = { Text("Room Name", color = TextSecondary) },
                                    placeholder = { Text("e.g. Room 402", color = TextMuted) },
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
                                    value = newRoomCapacity,
                                    onValueChange = { newRoomCapacity = it },
                                    label = { Text("Capacity", color = TextSecondary) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTealLight,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.weight(0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = newRoomIsLab,
                                    onCheckedChange = { newRoomIsLab = it },
                                    colors = CheckboxDefaults.colors(checkedColor = AccentTealLight)
                                )
                                Text("This is a hands-on Science/IT Lab", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                            }

                            if (newRoomIsLab) {
                                OutlinedTextField(
                                    value = newRoomEquipment,
                                    onValueChange = { newRoomEquipment = it },
                                    label = { Text("Laboratory Equipment", color = TextSecondary) },
                                    placeholder = { Text("e.g. 30 iMacs, Adobe CC Suite", color = TextMuted) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentTealLight,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (newRoomName.isNotBlank()) {
                                        val cap = newRoomCapacity.toIntOrNull() ?: 60
                                        viewModel.addClassroom(newRoomName, cap, newRoomIsLab, newRoomEquipment, "Main Campus")
                                        newRoomName = ""
                                        newRoomCapacity = "60"
                                        newRoomIsLab = false
                                        newRoomEquipment = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                                Text("Add Resource", color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Current Registry (${classroomsList.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(classroomsList) { room ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (room.isLab) Icons.Default.Build else Icons.Default.Place,
                                        contentDescription = "Room Type",
                                        tint = if (room.isLab) AccentVioletLight else AccentTealLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = room.name,
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Capacity: ${room.capacity} seats • ${if (room.isLab) "Lab (" + room.equipment + ")" else "Classroom"}",
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteClassroom(room) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRose)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (wizardStep == 2) {
                Button(
                    onClick = { wizardStep = 1 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    Text("Prev Step", color = TextPrimary)
                }
            } else {
                Spacer(modifier = Modifier.width(10.dp))
            }

            Button(
                onClick = {
                    if (wizardStep == 1) {
                        wizardStep = 2
                    } else {
                        // Advance out of setup wizard to Teacher list Management!
                        viewModel.navigateTo(Screen.TeacherManagement)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
            ) {
                Text(if (wizardStep == 1) "Next Step" else "Complete Wizard", color = Color.White)
                Icon(Icons.Default.ArrowForward, contentDescription = "Continue", tint = Color.White)
            }
        }
    }
}
