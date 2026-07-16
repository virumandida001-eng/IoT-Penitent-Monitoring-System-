package com.medsentry.guardian.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medsentry.guardian.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class GuardianViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("medsentry_guardian_prefs", Context.MODE_PRIVATE)

    var isLoggedIn by mutableStateOf(prefs.getBoolean("isLoggedIn", false))
        private set
    var isDarkMode by mutableStateOf(prefs.getBoolean("isDarkMode", false))

    val patients = mutableStateListOf(
        LinkedPatient(
            id = "PATIENT-101",
            name = "Alexander Pierce",
            status = "normal",
            lastUpdated = Date(),
            heartRate = 78.0,
            spo2 = 98.0,
            temperature = 36.8
        ),
        LinkedPatient(
            id = "PATIENT-102",
            name = "Jane Doe",
            status = "warning",
            lastUpdated = Date(),
            heartRate = 94.0,
            spo2 = 92.0,
            temperature = 37.4
        )
    )

    val tasks = mutableStateListOf(
        CareTask(
            name = "Give evening medication (Lisinopril)",
            description = "Patient 101 • 8:00 PM dose",
            patientId = "PATIENT-101"
        ),
        CareTask(
            name = "Log blood pressure manually",
            description = "Patient 101 • Daily clinical check",
            isCompleted = true,
            patientId = "PATIENT-101"
        ),
        CareTask(
            name = "Assist with physical therapy walk",
            description = "Patient 102 • Afternoon routine",
            patientId = "PATIENT-102"
        )
    )

    val notes = mutableStateListOf(
        HandoverNote(
            timestamp = Date(System.currentTimeMillis() - 14400000),
            author = "Guardian Sarah",
            text = "Alexander was resting well. Heart rate stable around 75 BPM. Took morning Lisinopril.",
            patientId = "PATIENT-101"
        ),
        HandoverNote(
            timestamp = Date(System.currentTimeMillis() - 28800000),
            author = "Dr. Vance",
            text = "Checked Jane's charts. SpO2 dropping slightly during sleep. Adjusted threshold boundaries.",
            patientId = "PATIENT-102"
        )
    )

    val alerts = mutableStateListOf(
        AlertNotification(
            parameter = "Blood Oxygen",
            message = "Jane Doe: SpO2 breached threshold — 91%",
            severity = "Warning",
            timestamp = Date(System.currentTimeMillis() - 3600000),
            patientId = "PATIENT-102"
        )
    )

    val googleContacts = mutableStateListOf(
        GoogleContact(name = "Dr. Elizabeth Vance (Cardiologist)", phone = "+15550199", email = "elizabeth.vance@hospital.org"),
        GoogleContact(name = "Sarah Pierce (Primary Guardian)", phone = "+15550143", email = "sarah.pierce@healthmail.com"),
        GoogleContact(name = "City Ambulance Dispatch", phone = "911", email = "dispatch@paramedic.org")
    )

    init {
        startVitalsStreamingSimulation()
    }

    private fun startVitalsStreamingSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(5000)
                fluctuatePatientVitals()
            }
        }
    }

    private fun fluctuatePatientVitals() {
        patients.forEachIndexed { idx, patient ->
            val newHr = (patient.heartRate + (Math.random() * 6 - 3)).coerceIn(45.0, 150.0)
            val newO2 = (patient.spo2 + (Math.random() - 0.5)).coerceIn(80.0, 100.0)
            val newTemp = (patient.temperature + (Math.random() * 0.2 - 0.1)).coerceIn(35.5, 40.5)

            val newStatus = when {
                newHr > 120 || newO2 < 89 || newTemp > 38.3 -> "emergency"
                newHr > 95 || newO2 < 94 || newTemp > 37.3 -> "warning"
                else -> "normal"
            }

            patients[idx] = patient.copy(
                heartRate = Math.round(newHr * 10.0) / 10.0,
                spo2 = Math.round(newO2 * 10.0) / 10.0,
                temperature = Math.round(newTemp * 10.0) / 10.0,
                status = newStatus,
                lastUpdated = Date()
            )

            // Trigger critical alert if emergency
            if (newStatus == "emergency") {
                val last = alerts.firstOrNull()
                val tooSoon = last != null && last.patientId == patient.id &&
                        (Date().time - last.timestamp.time) < 15000
                if (!tooSoon) {
                    alerts.add(
                        0,
                        AlertNotification(
                            parameter = "Telemetry",
                            message = "${patient.name} vitals in emergency: HR ${newHr.toInt()}, SpO2 ${newO2.toInt()}%",
                            severity = "Critical",
                            timestamp = Date(),
                            patientId = patient.id
                        )
                    )
                }
            }
        }
    }

    fun linkPatientByCode(code: String): Boolean {
        return when (code.uppercase().trim()) {
            "PX-941A" -> {
                if (patients.none { it.id == "PATIENT-101" }) {
                    patients.add(
                        LinkedPatient(
                            id = "PATIENT-101", name = "Alexander Pierce",
                            status = "normal", lastUpdated = Date(),
                            heartRate = 78.0, spo2 = 98.0, temperature = 36.8
                        )
                    )
                }
                true
            }
            else -> false
        }
    }

    fun toggleTask(taskId: UUID) {
        val idx = tasks.indexOfFirst { it.id == taskId }
        if (idx >= 0) tasks[idx] = tasks[idx].copy(isCompleted = !tasks[idx].isCompleted)
    }

    fun addHandoverNote(patientId: String, text: String) {
        notes.add(0, HandoverNote(timestamp = Date(), author = "Primary Guardian", text = text, patientId = patientId))
    }

    fun resolveAlert(alertId: UUID) {
        val idx = alerts.indexOfFirst { it.id == alertId }
        if (idx >= 0) alerts[idx] = alerts[idx].copy(isResolved = true)
    }

    fun importGoogleContact() {
        googleContacts.add(
            GoogleContact(name = "Dr. David Tennant (ER Specialist)", phone = "+15550212", email = "dtennant@hospital.org")
        )
    }

    fun login() {
        isLoggedIn = true
        prefs.edit().putBoolean("isLoggedIn", true).apply()
    }

    fun logout() {
        isLoggedIn = false
        prefs.edit().putBoolean("isLoggedIn", false).apply()
    }

    fun setDarkMode(enabled: Boolean) {
        isDarkMode = enabled
        prefs.edit().putBoolean("isDarkMode", enabled).apply()
    }
}
