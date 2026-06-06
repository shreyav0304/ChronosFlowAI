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
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var institutionName by remember { mutableStateOf("St. Andrews University") }
    var selectedRoleAdmin by remember { mutableStateOf(true) }

    // Stunning unique icon animation: subtle infinite rotation and breathing scale
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_rotation"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle_scale"
    )

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
            // Sparkly AI Header with interactive rotating halo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // Outer glowing halo
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .graphicsLayer {
                            rotationZ = -rotation
                            scaleX = scale
                            scaleY = scale
                        }
                        .border(
                            width = 1.5.dp,
                            brush = Brush.sweepGradient(listOf(AccentTealLight, AccentVioletLight, AccentTealLight)),
                            shape = RoundedCornerShape(18.dp)
                        )
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "AI Sparkle",
                    tint = AccentTealLight,
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer {
                            rotationZ = rotation
                        }
                )
            }

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
                            imageVector = Icons.Default.Star,
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
