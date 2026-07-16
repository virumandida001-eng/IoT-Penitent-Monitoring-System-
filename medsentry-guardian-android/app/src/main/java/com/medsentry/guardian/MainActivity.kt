package com.medsentry.guardian

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medsentry.guardian.data.LinkedPatient
import com.medsentry.guardian.ui.*
import com.medsentry.guardian.ui.theme.MedSentryGuardianTheme

// ============================================================================
// Navigation destinations
// ============================================================================
sealed class GuardianScreen {
    object Login           : GuardianScreen()
    object Dashboard       : GuardianScreen()
    object AlertsFeed      : GuardianScreen()
    object Tasks           : GuardianScreen()
    object Contacts        : GuardianScreen()
    object Settings        : GuardianScreen()
    data class PatientDetail(val patient: LinkedPatient) : GuardianScreen()
    data class HandoverNotes(val patientId: String)      : GuardianScreen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: GuardianViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedSentryGuardianTheme(darkTheme = viewModel.isDarkMode) {
                GuardianApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardianApp(viewModel: GuardianViewModel) {
    var currentScreen by remember { mutableStateOf<GuardianScreen>(GuardianScreen.Login) }
    var selectedTab  by remember { mutableStateOf(0) }

    // Auto-forward on login
    LaunchedEffect(viewModel.isLoggedIn) {
        if (viewModel.isLoggedIn && currentScreen is GuardianScreen.Login) {
            currentScreen = GuardianScreen.Dashboard
        } else if (!viewModel.isLoggedIn) {
            currentScreen = GuardianScreen.Login
        }
    }

    if (!viewModel.isLoggedIn) {
        LoginScreen(viewModel = viewModel)
        return
    }

    val tabs = listOf(
        Triple("Dashboard",  Icons.Default.Home,          0),
        Triple("Alerts",     Icons.Default.Notifications, 1),
        Triple("Tasks",      Icons.Default.List,          2),
        Triple("Contacts",   Icons.Default.Person,        3),
        Triple("Settings",   Icons.Default.Settings,      4)
    )

    // Back-stack helper — go back from sub-screens to relevant tab
    val isTopLevel = currentScreen is GuardianScreen.Dashboard ||
            currentScreen is GuardianScreen.AlertsFeed ||
            currentScreen is GuardianScreen.Tasks ||
            currentScreen is GuardianScreen.Contacts ||
            currentScreen is GuardianScreen.Settings

    Scaffold(
        topBar = {
            val title = when (val s = currentScreen) {
                is GuardianScreen.Dashboard     -> "Care Dashboard"
                is GuardianScreen.AlertsFeed    -> "Active Alerts"
                is GuardianScreen.Tasks         -> "Care Tasks"
                is GuardianScreen.Contacts      -> "Emergency Network"
                is GuardianScreen.Settings      -> "Guardian Settings"
                is GuardianScreen.PatientDetail -> s.patient.name
                is GuardianScreen.HandoverNotes -> "Shift Notes"
                else                            -> "MedSentry Guardian"
            }
            TopAppBar(
                title = {
                    Text(title, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    if (!isTopLevel) {
                        IconButton(onClick = { currentScreen = GuardianScreen.Dashboard }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { (label, icon, idx) ->
                    NavigationBarItem(
                        selected = selectedTab == idx,
                        onClick = {
                            selectedTab = idx
                            currentScreen = when (idx) {
                                0 -> GuardianScreen.Dashboard
                                1 -> GuardianScreen.AlertsFeed
                                2 -> GuardianScreen.Tasks
                                3 -> GuardianScreen.Contacts
                                else -> GuardianScreen.Settings
                            }
                        },
                        icon = {
                            if (idx == 1) {
                                BadgedBox(badge = {
                                    val count = viewModel.alerts.count { !it.isResolved }
                                    if (count > 0) Badge { Text("$count") }
                                }) {
                                    Icon(icon, contentDescription = label)
                                }
                            } else {
                                Icon(icon, contentDescription = label)
                            }
                        },
                        label = { Text(label, fontWeight = FontWeight.Medium) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val s = currentScreen) {
                is GuardianScreen.Login         -> {} // handled above
                is GuardianScreen.Dashboard     -> DashboardScreen(
                    viewModel = viewModel,
                    onPatientSelected = { patient ->
                        currentScreen = GuardianScreen.PatientDetail(patient)
                    }
                )
                is GuardianScreen.AlertsFeed    -> AlertsFeedScreen(viewModel = viewModel)
                is GuardianScreen.Tasks         -> TasksScreen(viewModel = viewModel)
                is GuardianScreen.Contacts      -> ContactsScreen(viewModel = viewModel)
                is GuardianScreen.Settings      -> SettingsScreen(viewModel = viewModel)
                is GuardianScreen.PatientDetail -> PatientDetailScreen(
                    viewModel  = viewModel,
                    patient    = viewModel.patients.find { it.id == s.patient.id } ?: s.patient,
                    onNavigateToTasks = {
                        selectedTab = 2
                        currentScreen = GuardianScreen.Tasks
                    },
                    onNavigateToNotes = {
                        currentScreen = GuardianScreen.HandoverNotes(s.patient.id)
                    }
                )
                is GuardianScreen.HandoverNotes -> HandoverNotesScreen(
                    viewModel = viewModel,
                    patientId = s.patientId
                )
            }
        }
    }
}
