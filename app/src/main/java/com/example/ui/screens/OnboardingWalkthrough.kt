package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

// Onboarding step model
data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgGradientColors: List<Color>,
    val badgeText: String,
    val highlights: List<String>
)

@Composable
fun OnboardingWalkthrough(
    onDismiss: () -> Unit
) {
    var currentStepIdx by remember { mutableStateOf(0) }
    
    val steps = listOf(
        OnboardingStep(
            title = "Welcome to ChronosFlow AI",
            description = "Experience the next-gen institutional scheduling system. ChronosFlow builds perfect, conflict-free calendars for academic departments, faculty, and physical classrooms.",
            icon = Icons.Default.Star,
            iconColor = AccentTealLight,
            bgGradientColors = listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE)),
            badgeText = "Intelligent Automation",
            highlights = listOf(
                "Dynamic conflict check resolution",
                "Automated classroom allocation",
                "Real-time validation engine"
            )
        ),
        OnboardingStep(
            title = "Interactive Hours Swapping",
            description = "Change plans instantly without making static errors. Simply double-tap any booked slot in the Master Timetable to select it, then tap any other slot to swap or move hours instantly, backed by live clash checking.",
            icon = Icons.Default.Refresh,
            iconColor = AccentVioletLight,
            bgGradientColors = listOf(Color(0xFFEEF2F6), Color(0xFFE2E8F0)),
            badgeText = "Real-Time Modification",
            highlights = listOf(
                "Double-tap to activate Swapping",
                "Visual animation highlights candidate slots",
                "Instant recalculation of room & teacher status"
            )
        ),
        OnboardingStep(
            title = "Smart Absence & Substitution",
            description = "Tackle unexpected roster leaves with comfort. When an instructor takes a leave request, ChronosFlow AI identifies unaffected, eligible department candidates who are free to substitute instantly.",
            icon = Icons.Default.Warning,
            iconColor = AccentRose,
            bgGradientColors = listOf(Color(0xFFFEF2F2), Color(0xFFFEE2E2)),
            badgeText = "Conflict Mitigation",
            highlights = listOf(
                "Automatic leave request tracking",
                "One-click smart cover duty recommendation",
                "Instant alerts & notifications for substitutes"
            )
        ),
        OnboardingStep(
            title = "Predictive Event Simulation",
            description = "Prepare for upcoming disruptions with what-if scenarios. Enter natural queries (e.g. 'What if Prof. Collins misses Friday morning?' or 'What if we add new classes?') to play out predictive outcomes with intelligent simulation logic.",
            icon = Icons.Default.Send,
            iconColor = AccentEmerald,
            bgGradientColors = listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5)),
            badgeText = "Interactive What-If",
            highlights = listOf(
                "Intelligent simulation reports",
                "Interactive scenario states tracking",
                "Risk scoring & utilization analysis"
            )
        )
    )

    val currentStep = steps[currentStepIdx]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .border(1.2.dp, DarkBorder, RoundedCornerShape(24.dp))
                .testTag("onboarding_dialog_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row with "ChronosFlow" branding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.horizontalGradient(listOf(AccentTeal, AccentViolet)))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CHRONOSFLOW AI",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = AccentTealLight
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentStep.bgGradientColors[0])
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentStep.badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            ),
                            color = currentStep.iconColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Beautiful interactive illustration area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.verticalGradient(currentStep.bgGradientColors))
                        .border(1.dp, currentStep.iconColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = currentStep.icon,
                            contentDescription = "Step Icon",
                            tint = currentStep.iconColor,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Step Title & description with high legibility on small screens
                Text(
                    text = currentStep.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentStep.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bullet/Highlights indicators for the step
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBg, RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentStep.highlights.forEach { bullet ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "bullet",
                                tint = currentStep.iconColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = bullet,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dots Progress Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { idx, _ ->
                        val isSelected = idx == currentStepIdx
                        val dotWidth = if (isSelected) 18.dp else 6.dp
                        val dotColor = if (isSelected) currentStep.iconColor else TextMuted.copy(alpha = 0.5f)
                        
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer Buttons Control Flow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip or Back Button
                    if (currentStepIdx > 0) {
                        TextButton(
                            onClick = {
                                if (currentStepIdx > 0) {
                                    currentStepIdx--
                                }
                            },
                            modifier = Modifier.testTag("onboarding_back_button")
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "back arrow")
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Back", color = TextSecondary, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("onboarding_skip_button")
                        ) {
                            Text("Skip", color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Next / Finish Button
                    val isLast = currentStepIdx == steps.size - 1
                    Button(
                        onClick = {
                            if (isLast) {
                                onDismiss()
                            } else {
                                currentStepIdx++
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentStep.iconColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("onboarding_next_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isLast) "Get Started" else "Next",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (!isLast) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "next arrow",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
