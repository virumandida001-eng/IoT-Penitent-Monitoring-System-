package com.medsentry.patient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.medsentry.patient.ui.*
import com.medsentry.patient.ui.theme.MedSentryTheme
import com.medsentry.patient.ui.theme.CriticalRed

class MainActivity : ComponentActivity() {

    private val viewModel: VitalsViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedSentryTheme(darkTheme = viewModel.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var selectedTab by remember { mutableIntStateOf(0) }
                    var showQuickSOSSheet by remember { mutableStateOf(false) }

                    if (!viewModel.isLoggedIn) {
                        LoginScreen(viewModel = viewModel)
                    } else if (!viewModel.isDevicePaired) {
                        OnboardingScreen(viewModel = viewModel)
                    } else {
                        Scaffold(
                            bottomBar = {
                                NavigationBar(containerColor = Color.white) {
                                    NavigationBarItem(
                                        selected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Dashboard") },
                                        label = { Text("Dashboard", fontSize = 10.sp) }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        icon = { Icon(imageVector = Icons.Default.Info, contentDescription = "Live Feed") },
                                        label = { Text("Live Feed", fontSize = 10.sp) }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 2,
                                        onClick = { selectedTab = 2 },
                                        icon = { Icon(imageVector = Icons.Default.List, contentDescription = "History") },
                                        label = { Text("History", fontSize = 10.sp) }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 3,
                                        onClick = { selectedTab = 3 },
                                        icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = "SOS") },
                                        label = { Text("SOS", fontSize = 10.sp) }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 4,
                                        onClick = { selectedTab = 4 },
                                        icon = { Icon(imageVector = Icons.Default.AccountBox, contentDescription = "Profile") },
                                        label = { Text("Profile", fontSize = 10.sp) }
                                    )
                                    NavigationBarItem(
                                        selected = selectedTab == 5,
                                        onClick = { selectedTab = 5 },
                                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                                        label = { Text("Settings", fontSize = 10.sp) }
                                    )
                                }
                            },
                            floatingActionButton = {
                                // Persistent FAB floating action button for quick SOS (if not on the SOS tab itself)
                                if (selectedTab != 3) {
                                    FloatingActionButton(
                                        onClick = { showQuickSOSSheet = true },
                                        containerColor = CriticalRed,
                                        contentColor = Color.white
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Quick SOS Alarm Trigger",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                when (selectedTab) {
                                    0 -> DashboardScreen(viewModel = viewModel)
                                    1 -> LiveVitalsScreen(viewModel = viewModel)
                                    2 -> HistoryScreen(viewModel = viewModel)
                                    3 -> SOSScreen(viewModel = viewModel)
                                    4 -> ProfileScreen(viewModel = viewModel)
                                    5 -> SettingsScreen(viewModel = viewModel)
                                }
                            }
                        }

                        // Modal Bottom Sheet / Dialog simulating Quick SOS window overlay
                        if (showQuickSOSSheet) {
                            AlertDialog(
                                onDismissRequest = { showQuickSOSSheet = false },
                                title = { Text("Quick Emergency SOS Trigger") },
                                text = { Text("Are you sure you want to broadcast a distress message and call primary contacts?") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showQuickSOSSheet = false
                                            selectedTab = 3 // Route to active SOS page
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CriticalRed)
                                    ) {
                                        Text("CONFIRM SOS", color = Color.white)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showQuickSOSSheet = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
import androidx.compose.ui.unit.sp
