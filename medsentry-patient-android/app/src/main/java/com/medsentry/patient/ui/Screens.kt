package com.medsentry.patient.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medsentry.patient.data.TelemetryReading
import com.medsentry.patient.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================================
// 1. LOGIN SCREEN
// ============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: VitalsViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(SkyBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = SkyBlue,
                    modifier = Modifier.size(42.dp)
                )
            }

            Text(
                text = "MedSentry Patient",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DeepDarkBlue
            )

            Text(
                text = "Secure Health Vitals & Real-Time Monitoring",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Card inputs container
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.white),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Button(
                        onClick = {
                            if (email.isEmpty()) email = "alexander.pierce@healthmail.com"
                            viewModel.login()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log In", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            if (viewModel.passcodeEnabled) {
                TextButton(onClick = { viewModel.login() }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Text("Unlock with Fingerprint")
                    }
                }
            }

            Text(
                text = "V 1.0.0 — HIPAA Compliant Platform",
                fontSize = 11.sp,
                color = SlateGray.copy(alpha = 0.7f)
            )
        }
    }
}

// ============================================================================
// 2. ONBOARDING / PAIRING SCREEN
// ============================================================================
@Composable
fun OnboardingScreen(viewModel: VitalsViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radarScale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Device Pairing",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DeepDarkBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Establish a Bluetooth link with your wearable sensor band.",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateGray,
                textAlign = TextAlign.Center
            )
        }

        // Radar Simulation graphics
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(radarScale)
                    .border(2.dp, SkyBlue.copy(alpha = radarAlpha), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(radarScale * 0.8f)
                    .border(2.dp, SkyBlue.copy(alpha = radarAlpha), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color.white, CircleShape)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = SkyBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Device Selection
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Available Sensors Nearby", fontWeight = FontWeight.Bold, color = SlateGray)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.white),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.pairDevice("MedSentry Band-4F90") }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, tint = SkyBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("MedSentry Band-4F90", fontWeight = FontWeight.Bold)
                        Text("Signal strength: Good (-64 dBm)", fontSize = 12.sp, color = SlateGray)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = SlateGray)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = { viewModel.pairDevice("Demo Simulation Node") }) {
                Text("Bypass Bluetooth and Use Seeded Simulator")
            }
        }
    }
}

// ============================================================================
// 3. DASHBOARD SCREEN
// ============================================================================
@Composable
fun DashboardScreen(viewModel: VitalsViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Battery Info Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Welcome back,", fontSize = 12.sp, color = SlateGray)
                Text(viewModel.patientName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            // Battery Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NormalGreen.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home, // Represents battery level icon fallback
                        contentDescription = null,
                        tint = NormalGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "${viewModel.sensorBattery}% Battery",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NormalGreen
                    )
                }
            }
        }

        // Streaming Chip Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(NormalGreen, CircleShape)
            )
            Text("Connected Sensor Node Streaming", fontSize = 11.sp, color = SlateGray)
        }

        // Vitals Stack
        VitalTile(
            title = "Heart Rate",
            value = "${viewModel.currentVitals.heartRate.toInt()}",
            unit = "BPM",
            status = getStatus(viewModel.currentVitals.heartRate, viewModel.thresholds.heartRateMin, viewModel.thresholds.heartRateMax),
            color = CriticalRed
        )

        VitalTile(
            title = "Blood Oxygen (SpO2)",
            value = "${viewModel.currentVitals.spo2.toInt()}",
            unit = "%",
            status = getStatus(viewModel.currentVitals.spo2, viewModel.thresholds.spo2Min, 100.0),
            color = SkyBlue
        )

        VitalTile(
            title = "Body Temperature",
            value = "${viewModel.currentVitals.temperature}",
            unit = "°C",
            status = getStatus(viewModel.currentVitals.temperature, 36.0, viewModel.thresholds.tempMax),
            color = WarningYellow
        )

        // Medications Card List
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.white),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Medication Reminders", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

                viewModel.medications.forEachIndexed { idx, med ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = med.isTaken,
                            onCheckedChange = { viewModel.medications[idx] = med.copy(isTaken = it) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = med.name,
                                fontWeight = FontWeight.SemiBold,
                                color = if (med.isTaken) SlateGray else DeepDarkBlue
                            )
                            Text(
                                text = "${med.dosage} • ${med.time}",
                                fontSize = 11.sp,
                                color = SlateGray
                            )
                        }
                    }
                }
            }
        }

        // Simulation control Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateGray.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Clinical Simulator Node Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGray)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.setSimulatorCondition("normal") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.vitalCondition == "normal") NormalGreen else Color.white),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Normal", color = if (viewModel.vitalCondition == "normal") Color.white else SlateGray, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.setSimulatorCondition("warning") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.vitalCondition == "warning") WarningYellow else Color.white),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Warning", color = if (viewModel.vitalCondition == "warning") Color.white else SlateGray, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.setSimulatorCondition("critical") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.vitalCondition == "critical") CriticalRed else Color.white),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Critical", color = if (viewModel.vitalCondition == "critical") Color.white else SlateGray, fontSize = 11.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun VitalTile(title: String, value: String, unit: String, status: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.white),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = color)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(title, fontSize = 12.sp, color = SlateGray)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(value, fontWeight = FontWeight.Bold, fontSize = 24.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(unit, fontSize = 12.sp, color = SlateGray)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Status chip
            val chipBg = when (status) {
                "Critical" -> CriticalRed.copy(alpha = 0.12f)
                "Warning" -> WarningYellow.copy(alpha = 0.12f)
                else -> NormalGreen.copy(alpha = 0.12f)
            }
            val chipColor = when (status) {
                "Critical" -> CriticalRed
                "Warning" -> WarningYellow
                else -> NormalGreen
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(chipBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(status, color = chipColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

private fun getStatus(value: Double, min: Double, max: Double): String {
    return if (value < min || value > max) {
        "Critical"
    } else if (value > (max - (max - min) * 0.1) || value < (min + (max - min) * 0.1)) {
        "Warning"
    } else {
        "Normal"
    }
}

// ============================================================================
// 4. LIVE VITALS SCREEN (WITH CUSTOM CANVAS SCROLLING CHART)
// ============================================================================
@Composable
fun LiveVitalsScreen(viewModel: VitalsViewModel) {
    var selectedMetric by remember { mutableStateOf("Heart Rate") }
    var mockConnected by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle connected banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.white),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (mockConnected) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (mockConnected) NormalGreen else CriticalRed,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(if (mockConnected) "Sensor Streaming Active" else "Sensor Paused", fontWeight = FontWeight.Bold)
                    Text("Connected via Bluetooth", fontSize = 12.sp, color = SlateGray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Switch(checked = mockConnected, onCheckedChange = { mockConnected = it })
            }
        }

        // Tab selection
        TabRow(
            selectedTabIndex = when (selectedMetric) {
                "Heart Rate" -> 0
                "SpO2" -> 1
                else -> 2
            },
            containerColor = Color.transparent
        ) {
            Tab(selected = selectedMetric == "Heart Rate", onClick = { selectedMetric = "Heart Rate" }) {
                Text("Heart Rate", modifier = Modifier.padding(12.dp))
            }
            Tab(selected = selectedMetric == "SpO2", onClick = { selectedMetric = "SpO2" }) {
                Text("SpO2", modifier = Modifier.padding(12.dp))
            }
            Tab(selected = selectedMetric == "Temp", onClick = { selectedMetric = "Temp" }) {
                Text("Temp", modifier = Modifier.padding(12.dp))
            }
        }

        // Native Canvas Line Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.white),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Live Telemetry Plot", fontWeight = FontWeight.Bold, color = SlateGray)
                Spacer(modifier = Modifier.height(10.dp))

                // Custom Draw Canvas matching Apple SwiftUI graph logic
                val metricLogs = viewModel.history.take(15).reversed()
                val lineStrokeColor = when (selectedMetric) {
                    "SpO2" -> SkyBlue
                    "Temp" -> WarningYellow
                    else -> CriticalRed
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(SoftBg.copy(alpha = 0.5f))
                ) {
                    if (metricLogs.size >= 2) {
                        val maxVal = when (selectedMetric) {
                            "SpO2" -> 100f
                            "Temp" -> 41f
                            else -> 160f
                        }
                        val minVal = when (selectedMetric) {
                            "SpO2" -> 80f
                            "Temp" -> 35f
                            else -> 40f
                        }
                        val valueRange = maxVal - minVal

                        val path = Path()
                        val stepX = size.width / (metricLogs.size - 1)

                        metricLogs.forEachIndexed { index, reading ->
                            val currentMetricVal = when (selectedMetric) {
                                "SpO2" -> reading.spo2.toFloat()
                                "Temp" -> reading.temperature.toFloat()
                                else -> reading.heartRate.toFloat()
                            }
                            // Scale coordinates
                            val x = index * stepX
                            val scaledY = size.height - ((currentMetricVal - minVal) / valueRange) * size.height

                            if (index == 0) {
                                path.moveTo(x, scaledY)
                            } else {
                                path.lineTo(x, scaledY)
                            }
                        }

                        drawPath(
                            path = path,
                            color = lineStrokeColor,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }
        }

        // Logger output console
        Text("Console Log", fontWeight = FontWeight.Bold, color = SlateGray)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                items(viewModel.history.take(8)) { reading ->
                    Text(
                        text = "SYNC [${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(reading.timestamp)}] SENS_NODE: HR=${reading.heartRate} O2=${reading.spo2}% TEMP=${reading.temperature}°C STATUS=${reading.status.uppercase()}",
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 5. HISTORY SCREEN
// ============================================================================
@Composable
fun HistoryScreen(viewModel: VitalsViewModel) {
    var searchTxt by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.white),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchTxt,
                    onValueChange = { searchTxt = it },
                    label = { Text("Search logs...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(10.dp))
                TabRow(selectedTabIndex = selectedFilterTab, containerColor = Color.transparent) {
                    Tab(selected = selectedFilterTab == 0, onClick = { selectedFilterTab = 0 }) {
                        Text("All Logs", modifier = Modifier.padding(8.dp))
                    }
                    Tab(selected = selectedFilterTab == 1, onClick = { selectedFilterTab = 1 }) {
                        Text("Today", modifier = Modifier.padding(8.dp))
                    }
                    Tab(selected = selectedFilterTab == 2, onClick = { selectedFilterTab = 2 }) {
                        Text("Alerts", modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }

        // Export report row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "MedSentry Vitals Report")
                        putExtra(Intent.EXTRA_TEXT, "Simulated PDF Medical Report download link: https://medsentry.io/reports/PATIENT-101")
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Vitals Report"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF Report", color = SkyBlue, fontWeight = FontWeight.Bold)
            }
        }

        // Results list
        val filteredLogs = viewModel.history.filter {
            if (selectedFilterTab == 1 && !isToday(it.timestamp)) return@filter false
            if (selectedFilterTab == 2 && it.status == "normal") return@filter false
            if (searchTxt.isNotEmpty() && !it.status.contains(searchTxt, ignoreCase = true)) return@filter false
            true
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredLogs) { reading ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.white),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(reading.timestamp),
                                fontSize = 12.sp,
                                color = SlateGray
                            )

                            val badgeColor = when (reading.status) {
                                "critical" -> CriticalRed
                                "warning" -> WarningYellow
                                else -> NormalGreen
                            }
                            Text(
                                text = reading.status.uppercase(),
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("HR", fontSize = 10.sp, color = SlateGray)
                                Text("${reading.heartRate.toInt()} BPM", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("SpO2", fontSize = 10.sp, color = SlateGray)
                                Text("${reading.spo2.toInt()}%", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Temp", fontSize = 10.sp, color = SlateGray)
                                Text("${reading.temperature}°C", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isToday(date: Date): Boolean {
    val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return fmt.format(date) == fmt.format(Date())
}

// ============================================================================
// 6. EMERGENCY SOS SCREEN
// ============================================================================
@Composable
fun SOSScreen(viewModel: VitalsViewModel) {
    var countdown by remember { mutableStateOf(3) }
    var isCounting by remember { mutableStateOf(false) }
    var isTriggered by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Launch countdown effect
    LaunchedEffect(isCounting) {
        if (isCounting) {
            countdown = 3
            while (countdown > 1) {
                delay(1000)
                countdown -= 1
            }
            isCounting = false
            isTriggered = true
            // Call primary contact dial simulation
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${viewModel.emergencyContacts.first().phone}")
            }
            context.startActivity(dialIntent)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isTriggered) CriticalRed else SoftBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            if (isTriggered) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.white,
                    modifier = Modifier.size(90.dp)
                )

                Text(
                    "SOS DISTRESS ACTIVE",
                    color = Color.white,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    "Pushed critical telemetry alerts to Guardian app and dispatched GPS coordinates: Lat: 37.7749, Lng: -122.4194",
                    color = Color.white.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = { isTriggered = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.white),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text("Dismiss SOS", color = CriticalRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp))
                }
            } else if (isCounting) {
                Text("Triggering SOS emergency in", color = SlateGray, fontSize = 20.sp)
                Text(
                    "$countdown",
                    fontWeight = FontWeight.Black,
                    fontSize = 110.sp,
                    color = CriticalRed,
                    fontFamily = FontFamily.Monospace
                )
                Button(
                    onClick = { isCounting = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SlateGray),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Stop Alarm", modifier = Modifier.padding(horizontal = 20.dp))
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CriticalRed,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    "Emergency Distress Transmissions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepDarkBlue
                )
                Text(
                    "Press and hold the SOS widget below to broadcast GPS, sound alerts, and notify primary emergency networks.",
                    color = SlateGray,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(CriticalRed, CircleShape)
                        .clickable { isCounting = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("SOS", color = Color.white, fontWeight = FontWeight.Black, fontSize = 36.sp)
                }

                Text("GPS Broadcast: Active", fontSize = 12.sp, color = SlateGray)
            }
        }
    }
}

// ============================================================================
// 7. PROFILE SCREEN
// ============================================================================
@Composable
fun ProfileScreen(viewModel: VitalsViewModel) {
    var editMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.white),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBox,
                    contentDescription = null,
                    tint = SkyBlue,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (editMode) {
                    OutlinedTextField(
                        value = viewModel.patientName,
                        onValueChange = { viewModel.patientName = it },
                        label = { Text("Name") }
                    )
                } else {
                    Text(viewModel.patientName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text("ID: ${viewModel.patientID}", fontSize = 11.sp, color = SlateGray)
                }
            }
        }

        // Clinical properties card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.white),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Clinical Record Details", fontWeight = FontWeight.Bold)
                Divider()

                ClinicalItemRow("Blood Group", viewModel.bloodGroup, editMode) { viewModel.bloodGroup = it }
                ClinicalItemRow("Conditions", viewModel.medicalConditions, editMode) { viewModel.medicalConditions = it }
                ClinicalItemRow("Allergies", viewModel.allergies, editMode) { viewModel.allergies = it }
                ClinicalItemRow("Physician", viewModel.doctorName, editMode) { viewModel.doctorName = it }
                ClinicalItemRow("Hospital", viewModel.hospitalInfo, editMode) { viewModel.hospitalInfo = it }
            }
        }

        // Emergency Contacts
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.white),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Primary Emergency Contact Network", fontWeight = FontWeight.Bold)
                Divider()

                viewModel.emergencyContacts.forEach { contact ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(contact.name, fontWeight = FontWeight.Bold)
                                if (contact.isPrimary) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(CriticalRed.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("SOS", color = CriticalRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text("${contact.relationship} • ${contact.phone}", fontSize = 11.sp, color = SlateGray)
                        }

                        val context = LocalContext.current
                        IconButton(onClick = {
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${contact.phone}")
                            }
                            context.startActivity(dialIntent)
                        }) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = NormalGreen)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { editMode = !editMode },
            colors = ButtonDefaults.buttonColors(containerColor = if (editMode) NormalGreen else SkyBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (editMode) "Save Records" : "Edit Profile Info")
        }
        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun ClinicalItemRow(label: String, value: String, editMode: Boolean, onValChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.width(100.dp), color = SlateGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(8.dp))
        if (editMode) {
            OutlinedTextField(
                value = value,
                onValueChange = onValChange,
                modifier = Modifier.weight(1f)
            )
        } else {
            Text(value, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        }
    }
}

// ============================================================================
// 8. SETTINGS SCREEN
// ============================================================================
@Composable
fun SettingsScreen(viewModel: VitalsViewModel) {
    var pushAudio by remember { mutableStateOf(true) }
    var pushVibrate by remember { mutableStateOf(true) }
    var showQR by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Notification Preferences", fontWeight = FontWeight.Bold, color = SlateGray)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.white),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vibration Warnings")
                    Switch(checked = pushVibrate, onCheckedChange = { pushVibrate = it })
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Audio Threshold Sirens")
                    Switch(checked = pushAudio, onCheckedChange = { pushAudio = it })
                }
            }
        }

        Text("Clinician Managed Ranges (Read-Only)", fontWeight = FontWeight.Bold, color = SlateGray)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.white),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Heart Rate limits")
                    Text("${viewModel.thresholds.heartRateMin.toInt()} - ${viewModel.thresholds.heartRateMax.toInt()} BPM", fontWeight = FontWeight.Bold, color = SlateGray)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SpO2 Min boundary")
                    Text("${viewModel.thresholds.spo2Min.toInt()} %", fontWeight = FontWeight.Bold, color = SlateGray)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Max Temp threshold")
                    Text("${viewModel.thresholds.tempMax} °C", fontWeight = FontWeight.Bold, color = SlateGray)
                }
            }
        }

        Text("Guardian Connection Node", fontWeight = FontWeight.Bold, color = SlateGray)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.white),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Pairing Code", fontSize = 11.sp, color = SlateGray)
                        Text("PX-941A", fontWeight = FontWeight.Black, fontSize = 20.sp, color = SkyBlue)
                    }
                    IconButton(onClick = { showQR = !showQR }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = SkyBlue)
                    }
                }
                if (showQR) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .background(Color.white)
                            .border(1.dp, SlateGray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Simulated QR code",
                            tint = Color.Black,
                            modifier = Modifier.size(90.dp)
                        )
                    }
                    Text("Hold near guardian scanner to link.", fontSize = 10.sp, color = SlateGray)
                }
            }
        }

        Text("App Configurations", fontWeight = FontWeight.Bold, color = SlateGray)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.white),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dark Mode Theme")
                    Switch(checked = viewModel.isDarkMode, onCheckedChange = { viewModel.setDarkModeEnabled(it) })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Out")
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}
