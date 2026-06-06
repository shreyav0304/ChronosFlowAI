package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TimetableVisualSlot(
    title: String,
    room: String,
    color: Color
) {
    if (color == Color.Transparent) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkBg.copy(alpha = 0.5f))
                .border(1.dp, DarkBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "FREE",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                color = TextMuted.copy(alpha = 0.6f)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.12f))
                .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(vertical = 3.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = color
                )
                Text(
                    text = room,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun TimetableMiniatureVisual(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "timetable_visual")
    
    // Smooth pulsing solver/optimizer simulator
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .border(1.2.dp, DarkBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper status bar: Constraint Engine Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Flashing pulse dot representing real-time scheduler state
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(AccentEmerald.copy(alpha = pulseAlpha))
                        .border(1.dp, AccentEmerald, androidx.compose.foundation.shape.CircleShape)
                )
                Text(
                    text = "CONFLICT-FREE ENGINE ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 9.sp
                    ),
                    color = AccentEmerald
                )
            }
            Text(
                text = "Rooms Validated",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid contents
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Time Labels Column
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.width(36.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp)) // Align with header
                Text("09:00", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextMuted)
                Text("11:00", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextMuted)
                Text("13:00", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextMuted)
            }

            // Morning/Mid/Afternoon timetable blocks for Days
            val days = listOf("MON", "TUE", "WED", "THU")
            days.forEachIndexed { dayIdx, day ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Day title header
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = TextPrimary
                    )

                    // Slots
                    when (dayIdx) {
                        0 -> { // MON
                            TimetableVisualSlot(title = "CS101", room = "R102", color = AccentTeal)
                            TimetableVisualSlot(title = "MATH5", room = "R204", color = AccentViolet)
                            TimetableVisualSlot(title = "—", room = "Free", color = Color.Transparent)
                        }
                        1 -> { // TUE
                            TimetableVisualSlot(title = "PHYS2", room = "LabA", color = AccentViolet)
                            TimetableVisualSlot(title = "CS101", room = "R102", color = AccentTeal)
                            TimetableVisualSlot(title = "CHEM1", room = "LabB", color = AccentRose)
                        }
                        2 -> { // WED
                            TimetableVisualSlot(title = "—", room = "Free", color = Color.Transparent)
                            TimetableVisualSlot(title = "MATH5", room = "R204", color = AccentViolet)
                            TimetableVisualSlot(title = "PHYS1", room = "LabA", color = AccentTeal)
                        }
                        3 -> { // THU
                            TimetableVisualSlot(title = "CHEM1", room = "LabB", color = AccentRose)
                            TimetableVisualSlot(title = "—", room = "Free", color = Color.Transparent)
                            TimetableVisualSlot(title = "CS102", room = "R102", color = AccentTeal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var institutionName by remember { mutableStateOf("St. Andrews University") }
    var selectedRoleAdmin by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBg, Color(0xFF020617)) // Deep space obsidian flow
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Custom Visual Timetable Optimizer Header
            TimetableMiniatureVisual(
                modifier = Modifier.padding(bottom = 18.dp)
            )

            Text(
                text = "ChronosFlow AI",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            
            Text(
                text = "Constraint-Optimization Scheduling System",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Portal Selector Card (Glassmorphic Container)
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Workspace Portal",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Admin Sector Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedRoleAdmin) AccentTeal.copy(alpha = 0.2f) else DarkBg)
                                .border(
                                    width = 1.5.dp,
                                    color = if (selectedRoleAdmin) AccentTealLight else DarkBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedRoleAdmin = true }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Admin icon",
                                    tint = if (selectedRoleAdmin) AccentTealLight else TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Administration",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selectedRoleAdmin) TextPrimary else TextSecondary
                                )
                            }
                        }

                        // Teacher Sector Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (!selectedRoleAdmin) AccentViolet.copy(alpha = 0.2f) else DarkBg)
                                .border(
                                    width = 1.5.dp,
                                    color = if (!selectedRoleAdmin) AccentVioletLight else DarkBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedRoleAdmin = false }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Teacher icon",
                                    tint = if (!selectedRoleAdmin) AccentVioletLight else TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Teacher Portal",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (!selectedRoleAdmin) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Institution Text Field
                    OutlinedTextField(
                        value = institutionName,
                        onValueChange = { institutionName = it },
                        label = { Text("Institution Name", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentTealLight,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Launch Button
                    Button(
                        onClick = {
                            viewModel.setAdminMode(selectedRoleAdmin)
                            if (selectedRoleAdmin) {
                                viewModel.navigateTo(Screen.SetupWizard)
                            } else {
                                viewModel.navigateTo(Screen.TeacherPortal)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedRoleAdmin) AccentTeal else AccentViolet
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (selectedRoleAdmin) "Enter Admin Suite" else "Enter Teacher Portal",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.setAdminMode(true)
                            viewModel.forceSeed()
                            viewModel.navigateTo(Screen.Dashboard)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentTealLight),
                        border = BorderStroke(1.2.dp, AccentTealLight.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Quick load setup",
                            tint = AccentTealLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Instant Demo Quick Start",
                            style = MaterialTheme.typography.titleMedium,
                            color = AccentTealLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
