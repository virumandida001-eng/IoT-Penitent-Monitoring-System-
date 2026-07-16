package com.medsentry.guardian.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medsentry.guardian.data.*
import com.medsentry.guardian.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ============================================================================
// LOGIN SCREEN
// ============================================================================
@Composable
fun LoginScreen(viewModel: GuardianViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(SoftBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo
            Box(
                modifier = Modifier.size(80.dp).background(SkyBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = null,
                    tint = SkyBlue,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text("MedSentry Guardian", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Multi-Patient Remote Caregiving Platform",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Caregiver Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.login() },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign In as Guardian", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            Text(
                "Remote Caregiving Portal · Encrypted Connection",
                fontSize = 11.sp,
                color = SlateGray.copy(alpha = 0.7f)
            )
        }
    }
}

// ============================================================================
// DASHBOARD SCREEN — patient list with live status
// ============================================================================
@Composable
fun DashboardScreen(viewModel: GuardianViewModel, onPatientSelected: (LinkedPatient) -> Unit) {
    var showLinkDialog by remember { mutableStateOf(false) }
    var linkCode by remember { mutableStateOf("") }
    var linkResult by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().background(SoftBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Care Dashboard", fontSize = 12.sp, color = SlateGray)
                Text("Linked Patients", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Button(
                onClick = { showLinkDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Link Patient", color = SkyBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Patient cards
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(viewModel.patients) { patient ->
                PatientCard(patient = patient, onClick = { onPatientSelected(patient) })
            }
        }
    }

    // Link Dialog
    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false; linkResult = null },
            title = { Text("Link Patient by Invite Code") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter the code shown on the patient's Settings screen.", color = SlateGray, fontSize = 13.sp)
                    OutlinedTextField(
                        value = linkCode,
                        onValueChange = { linkCode = it.uppercase() },
                        label = { Text("Invite Code (e.g. PX-941A)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    linkResult?.let { success ->
                        Text(
                            if (success) "✓ Successfully linked!" else "✗ Invalid code. Try again.",
                            color = if (success) NormalGreen else CriticalRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        linkResult = viewModel.linkPatientByCode(linkCode)
                        if (linkResult == true) {
                            linkCode = ""
                            showLinkDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue)
                ) { Text("Verify & Link") }
            },
            dismissButton = {
                TextButton(onClick = { showLinkDialog = false; linkResult = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun PatientCard(patient: LinkedPatient, onClick: () -> Unit) {
    val statusColor = when (patient.status) {
        "emergency" -> CriticalRed
        "warning" -> WarningYellow
        else -> NormalGreen
    }

    // Pulsing status dot animation
    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Pulsing status indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(statusColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .scale(if (patient.status == "emergency") pulseScale else 1f)
                        .background(statusColor, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(patient.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "ID: ${patient.id} · ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(patient.lastUpdated)}",
                    fontSize = 11.sp, color = SlateGray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${patient.heartRate.toInt()} BPM",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
                Text(
                    "${patient.spo2.toInt()}% O₂",
                    fontSize = 12.sp,
                    color = SkyBlue,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = SlateGray)
        }
    }
}

// ============================================================================
// PATIENT DETAIL SCREEN
// ============================================================================
@Composable
fun PatientDetailScreen(
    viewModel: GuardianViewModel,
    patient: LinkedPatient,
    onNavigateToTasks: () -> Unit,
    onNavigateToNotes: () -> Unit
) {
    val context = LocalContext.current
    var selectedMetric by remember { mutableStateOf(0) } // 0=HR, 1=SpO2, 2=Temp

    val statusColor = when (patient.status) {
        "emergency" -> CriticalRed
        "warning" -> WarningYellow
        else -> NormalGreen
    }

    Column(
        modifier = Modifier.fillMaxSize().background(SoftBg).padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status header card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(patient.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("ID: ${patient.id}", fontSize = 12.sp, color = SlateGray)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(patient.status.uppercase(), color = statusColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider()
                Spacer(modifier = Modifier.height(14.dp))

                // Vitals row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    VitalStat("Heart Rate", "${patient.heartRate.toInt()}", "BPM", CriticalRed)
                    VitalStat("SpO2", "${patient.spo2.toInt()}", "%", SkyBlue)
                    VitalStat("Temp", "${patient.temperature}", "°C", WarningYellow)
                }
            }
        }

        // Live vitals mini chart
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Metric tabs
                TabRow(
                    selectedTabIndex = selectedMetric,
                    containerColor = Color.Transparent,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    listOf("Heart Rate", "SpO2", "Temp").forEachIndexed { idx, label ->
                        Tab(
                            selected = selectedMetric == idx,
                            onClick = { selectedMetric = idx },
                            text = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Canvas-drawn chart
                val lineColor = when (selectedMetric) {
                    1 -> SkyBlue
                    2 -> WarningYellow
                    else -> CriticalRed
                }
                val baseValue = when (selectedMetric) {
                    1 -> patient.spo2
                    2 -> patient.temperature
                    else -> patient.heartRate
                }
                val mockPoints = (0..11).map { i ->
                    baseValue + (Math.random() * 6 - 3)
                }

                Canvas(
                    modifier = Modifier.fillMaxWidth().height(160.dp).background(SoftBg.copy(0.5f))
                ) {
                    if (mockPoints.size >= 2) {
                        val maxV = mockPoints.maxOrNull() ?: 1.0
                        val minV = mockPoints.minOrNull() ?: 0.0
                        val range = (maxV - minV).coerceAtLeast(1.0)
                        val stepX = size.width / (mockPoints.size - 1)
                        val path = Path()
                        mockPoints.forEachIndexed { i, v ->
                            val x = i * stepX
                            val y = size.height - ((v - minV) / range * size.height).toFloat()
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx()))

                        // Draw dots
                        mockPoints.forEachIndexed { i, v ->
                            val x = i * stepX
                            val y = size.height - ((v - minV) / range * size.height).toFloat()
                            drawCircle(lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
                        }
                    }
                }
            }
        }

        // Action shortcuts
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onNavigateToNotes,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Notes", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onNavigateToTasks,
                colors = ButtonDefaults.buttonColors(containerColor = NormalGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tasks", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:+15550143")
                    }
                    context.startActivity(dialIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NormalGreen.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = NormalGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Call", color = NormalGreen, fontWeight = FontWeight.Bold)
            }
        }

        // Recent Alerts for this patient
        val patientAlerts = viewModel.alerts.filter { it.patientId == patient.id }
        if (patientAlerts.isNotEmpty()) {
            Text("Recent Alerts", fontWeight = FontWeight.Bold, color = SlateGray)
            patientAlerts.take(5).forEach { alert ->
                AlertItemCard(alert = alert, onResolve = { viewModel.resolveAlert(alert.id) })
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun VitalStat(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = SlateGray)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.width(2.dp))
            Text(unit, fontSize = 11.sp, color = SlateGray)
        }
    }
}

// ============================================================================
// ALERTS FEED SCREEN
// ============================================================================
@Composable
fun AlertsFeedScreen(viewModel: GuardianViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().background(SoftBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Emergency Quick-Dial Bar
        Button(
            onClick = {
                context.startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:911") })
            },
            colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("CALL 911 — Emergency Dispatch", fontWeight = FontWeight.Black, fontSize = 14.sp)
        }

        val activeAlerts = viewModel.alerts.filter { !it.isResolved }

        if (activeAlerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = SlateGray, modifier = Modifier.size(56.dp))
                    Text("All Clear — No Active Alerts", color = SlateGray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(activeAlerts, key = { it.id }) { alert ->
                    AlertItemCard(alert = alert, onResolve = { viewModel.resolveAlert(alert.id) })
                }
            }
        }
    }
}

@Composable
fun AlertItemCard(alert: AlertNotification, onResolve: () -> Unit) {
    val severityColor = if (alert.severity == "Critical") CriticalRed else WarningYellow

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, severityColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.size(8.dp).background(severityColor, CircleShape)
                    )
                    Text(alert.parameter, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(severityColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(alert.severity.uppercase(), color = severityColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Text(alert.message, fontSize = 13.sp, color = DeepDarkBlue)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    SimpleDateFormat("HH:mm:ss · MMM d", Locale.getDefault()).format(alert.timestamp),
                    fontSize = 11.sp, color = SlateGray
                )
                if (!alert.isResolved) {
                    TextButton(onClick = onResolve) {
                        Text("Acknowledge & Resolve", color = SkyBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                } else {
                    Text("Resolved ✓", color = NormalGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================================================
// TASKS SCREEN
// ============================================================================
@Composable
fun TasksScreen(viewModel: GuardianViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().background(SoftBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SkyBlue.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Task completions sync to clinician dashboards in real time.", fontSize = 12.sp, color = SlateGray)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.tasks, key = { it.id }) { task ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.toggleTask(task.id) },
                            colors = CheckboxDefaults.colors(checkedColor = NormalGreen)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                task.name,
                                fontWeight = FontWeight.SemiBold,
                                color = if (task.isCompleted) SlateGray else DeepDarkBlue,
                                fontSize = 14.sp
                            )
                            Text(task.description, fontSize = 11.sp, color = SlateGray)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// HANDOVER NOTES SCREEN
// ============================================================================
@Composable
fun HandoverNotesScreen(viewModel: GuardianViewModel, patientId: String) {
    var noteText by remember { mutableStateOf("") }
    val patientNotes = viewModel.notes.filter { it.patientId == patientId }

    Column(
        modifier = Modifier.fillMaxSize().background(SoftBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Add Clinical Observation", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Enter observation...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                Button(
                    onClick = {
                        if (noteText.isNotBlank()) {
                            viewModel.addHandoverNote(patientId, noteText)
                            noteText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Post Note") }
            }
        }

        Text("Shift Notes Timeline", fontWeight = FontWeight.Bold, color = SlateGray)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(patientNotes, key = { it.id }) { note ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(note.author, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(note.timestamp),
                                fontSize = 11.sp, color = SlateGray
                            )
                        }
                        Divider()
                        Text(note.text, fontSize = 13.sp, color = DeepDarkBlue)
                    }
                }
            }
        }
    }
}

// ============================================================================
// CONTACTS SCREEN
// ============================================================================
@Composable
fun ContactsScreen(viewModel: GuardianViewModel) {
    val context = LocalContext.current
    var importSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(SoftBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Google OAuth Import Button
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().clickable {
                viewModel.importGoogleContact()
                importSuccess = true
            }
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(46.dp).background(SkyBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = SkyBlue)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Import from Google Contacts", fontWeight = FontWeight.Bold)
                    Text("Sync clinicians & emergency responders", fontSize = 12.sp, color = SlateGray)
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = SlateGray)
            }
        }

        if (importSuccess) {
            Card(
                colors = CardDefaults.cardColors(containerColor = NormalGreen.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NormalGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dr. David Tennant added from Google Contacts.", fontSize = 12.sp, color = NormalGreen)
                }
            }
        }

        Text("Emergency Network Directory", fontWeight = FontWeight.Bold, color = SlateGray)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.googleContacts, key = { it.id }) { contact ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp).background(SlateGray.copy(alpha = 0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = SlateGray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(contact.phone, fontSize = 11.sp, color = SlateGray)
                        }
                        IconButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${contact.phone}")
                            })
                        }) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = NormalGreen)
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// SETTINGS SCREEN
// ============================================================================
@Composable
fun SettingsScreen(viewModel: GuardianViewModel) {
    var highPriorityAlerts by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().background(SoftBg).padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Critical Alert Override", fontWeight = FontWeight.Bold, color = SlateGray, modifier = Modifier.padding(top = 4.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("High-Priority Push Bypass", fontWeight = FontWeight.SemiBold)
                        Text("Bypass silent mode for SOS alerts", fontSize = 11.sp, color = SlateGray)
                    }
                    Switch(checked = highPriorityAlerts, onCheckedChange = { highPriorityAlerts = it })
                }
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vibration on Critical SOS", fontWeight = FontWeight.SemiBold)
                    Switch(checked = vibrationEnabled, onCheckedChange = { vibrationEnabled = it })
                }
                Divider()
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(16.dp))
                    Text("SOS alerts always override device mute.", fontSize = 11.sp, color = CriticalRed)
                }
            }
        }

        Text("Monitored Patients", fontWeight = FontWeight.Bold, color = SlateGray)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                viewModel.patients.forEach { patient ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(patient.name, fontWeight = FontWeight.SemiBold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (patient.status) {
                                        "emergency" -> CriticalRed.copy(alpha = 0.1f)
                                        "warning" -> WarningYellow.copy(alpha = 0.1f)
                                        else -> NormalGreen.copy(alpha = 0.1f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                patient.id,
                                fontSize = 11.sp,
                                color = SlateGray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Text("Display", fontWeight = FontWeight.Bold, color = SlateGray)

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dark Mode Theme", fontWeight = FontWeight.SemiBold)
                Switch(checked = viewModel.isDarkMode, onCheckedChange = { viewModel.setDarkMode(it) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out Guardian Account", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
