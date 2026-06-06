package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()
                val isAdmin by viewModel.isAdmin.collectAsState()
                val liveConflicts by viewModel.liveConflicts.collectAsState()
                val isOnboardingActive by viewModel.isOnboardingActive.collectAsState()

                // Determine navigation visibility
                val showBottomNav = currentScreen != Screen.Login && currentScreen != Screen.SetupWizard

                if (isOnboardingActive) {
                    OnboardingWalkthrough(onDismiss = { viewModel.setOnboardingActive(false) })
                }


                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomNav) {
                            if (isAdmin) {
                                // Administration Bottom Navigation Pill Bar
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 8.dp
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Dashboard,
                                        onClick = { viewModel.navigateTo(Screen.Dashboard) },
                                        icon = { Icon(Icons.Default.Home, contentDescription = "Schedules", modifier = Modifier.size(22.dp)) },
                                        label = { Text("Master", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.TeacherManagement || currentScreen == Screen.SubjectAllocation,
                                        onClick = { viewModel.navigateTo(Screen.TeacherManagement) },
                                        icon = { Icon(Icons.Default.Person, contentDescription = "Staff", modifier = Modifier.size(22.dp)) },
                                        label = { Text("Faculties", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Generator,
                                        onClick = { viewModel.navigateTo(Screen.Generator) },
                                        icon = { Icon(Icons.Default.Star, contentDescription = "AI Solves", modifier = Modifier.size(22.dp)) },
                                        label = { Text("AI Slv", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.ConflictCenter,
                                        onClick = { viewModel.navigateTo(Screen.ConflictCenter) },
                                        icon = { 
                                            BadgedBox(
                                                badge = { if (liveConflicts.isNotEmpty()) Badge { Text(liveConflicts.size.toString()) } }
                                            ) {
                                                Icon(Icons.Default.Warning, contentDescription = "Clashes", modifier = Modifier.size(22.dp))
                                            }
                                        },
                                        label = { Text("Clashes", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Analytics,
                                        onClick = { viewModel.navigateTo(Screen.Analytics) },
                                        icon = { Icon(Icons.Default.Info, contentDescription = "Metrics", modifier = Modifier.size(22.dp)) },
                                        label = { Text("Metrics", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Settings,
                                        onClick = { viewModel.navigateTo(Screen.Settings) },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "What-If", modifier = Modifier.size(22.dp)) },
                                        label = { Text("Configs", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            } else {
                                // Teacher specific Portal Bottom Nav
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 8.dp
                                ) {
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.TeacherPortal,
                                        onClick = { viewModel.navigateTo(Screen.TeacherPortal) },
                                        icon = { Icon(Icons.Default.List, contentDescription = "My schedule", modifier = Modifier.size(22.dp)) },
                                        label = { Text("Calendar", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.ConflictCenter,
                                        onClick = { viewModel.navigateTo(Screen.ConflictCenter) },
                                        icon = { Icon(Icons.Default.Add, contentDescription = "Leaves", modifier = Modifier.size(22.dp)) },
                                        label = { Text("Absences", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Analytics,
                                        onClick = { viewModel.navigateTo(Screen.Analytics) },
                                        icon = { Icon(Icons.Default.Info, contentDescription = "Analytics", modifier = Modifier.size(22.dp)) },
                                        label = { Text("Reports", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.Login,
                                        onClick = { viewModel.navigateTo(Screen.Login) },
                                        icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Sign out", modifier = Modifier.size(22.dp)) },
                                        label = { Text("Sign Out", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.Login -> LoginScreen(viewModel)
                            Screen.SetupWizard -> SetupWizardScreen(viewModel)
                            Screen.TeacherManagement -> TeacherManagementScreen(viewModel)
                            Screen.SubjectAllocation -> SubjectAllocationScreen(viewModel)
                            Screen.Generator -> GeneratorScreen(viewModel)
                            Screen.Dashboard -> DashboardScreen(viewModel)
                            Screen.ConflictCenter -> ConflictCenterScreen(viewModel)
                            Screen.TeacherPortal -> TeacherPortalScreen(viewModel)
                            Screen.Analytics -> AnalyticsScreen(viewModel)
                            Screen.Settings -> SettingsScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
