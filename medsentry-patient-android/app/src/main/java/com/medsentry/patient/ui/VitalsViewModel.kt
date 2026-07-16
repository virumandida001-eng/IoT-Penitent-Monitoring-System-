package com.medsentry.patient.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medsentry.patient.data.AlertMessage
import com.medsentry.patient.data.AlertThresholds
import com.medsentry.patient.data.EmergencyContact
import com.medsentry.patient.data.MedicationReminder
import com.medsentry.patient.data.TelemetryReading
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class VitalsViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("medsentry_patient_prefs", Context.MODE_PRIVATE)

    // Auth states
    var isLoggedIn by mutableStateOf(sharedPrefs.getBoolean("isLoggedIn", false))
        private set
    var passcodeEnabled by mutableStateOf(sharedPrefs.getBoolean("passcodeEnabled", false))
        private set
    var isDevicePaired by mutableStateOf(sharedPrefs.getBoolean("isDevicePaired", false))
        private set
    var pairedDeviceName by mutableStateOf(sharedPrefs.getString("pairedDeviceName", null))
        private set

    // Clinical states
    var vitalCondition by mutableStateOf("normal")
    var currentVitals by mutableStateOf(TelemetryReading(heartRate = 78.0, spo2 = 98.0, temperature = 36.8, timestamp = Date(), status = "normal"))
    val history = mutableStateListOf<TelemetryReading>()
    val alerts = mutableStateListOf<AlertMessage>()
    var thresholds by mutableStateOf(AlertThresholds())
    var sensorBattery by mutableStateOf(85)
    var isDarkMode by mutableStateOf(sharedPrefs.getBoolean("isDarkMode", false))

    // Profile details
    var patientName by mutableStateOf("Alexander Pierce")
    var patientID by mutableStateOf("PATIENT-101")
    var email by mutableStateOf("alexander.pierce@healthmail.com")
    var bloodGroup by mutableStateOf("O-Positive")
    var medicalConditions by mutableStateOf("Hypertension, Mild Asthma")
    var allergies by mutableStateOf("Penicillin, Peanuts")
    var hospitalInfo by mutableStateOf("St. Jude Medical Center - (555) 0199")
    var doctorName by mutableStateOf("Dr. Elizabeth Vance")

    val emergencyContacts = mutableStateListOf(
        EmergencyContact(name = "Sarah Pierce", phone = "+15550143", relationship = "Spouse", isPrimary = true),
        EmergencyContact(name = "John Pierce", phone = "+15550187", relationship = "Brother", isPrimary = false)
    )

    val medications = mutableStateListOf(
        MedicationReminder(time = "08:00 AM", name = "Lisinopril", dosage = "10mg"),
        MedicationReminder(time = "02:00 PM", name = "Aspirin", dosage = "81mg"),
        MedicationReminder(time = "08:00 PM", name = "Albuterol Inhaler", dosage = "2 Puffs")
    )

    private var simulationJob: Job? = null

    init {
        seedInitialHistory()
        startSimulation()
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (true) {
                delay(5000)
                generateNextReading()
            }
        }
    }

    private fun seedInitialHistory() {
        val now = Date().time
        for (i in 24 downTo 0) {
            val offsetTime = Date(now - i * 3600 * 1000)
            history.add(generateReading("normal", offsetTime))
        }
    }

    fun setSimulatorCondition(condition: String) {
        vitalCondition = condition
        generateNextReading()
    }

    private fun generateNextReading() {
        val nextReading = generateReading(vitalCondition, Date())
        currentVitals = nextReading
        history.add(0, nextReading)

        if (history.size > 100) {
            history.removeLast()
        }

        if (Math.random() > 0.85 && sensorBattery > 1) {
            sensorBattery -= 1
        }

        evaluateReadings(nextReading)
    }

    private fun generateReading(condition: String, date: Date): TelemetryReading {
        var hr = 68.0 + Math.random() * 14
        var o2 = 96.0 + Math.random() * 3
        var temp = 36.4 + Math.random() * 0.6
        var status = "normal"

        if (condition == "warning") {
            val low = Math.random() > 0.5
            hr = if (low) 52.0 + Math.random() * 6 else 96.0 + Math.random() * 12
            o2 = 91.0 + Math.random() * 3
            temp = 37.3 + Math.random() * 0.6
            status = "warning"
        } else if (condition == "critical") {
            hr = 121.0 + Math.random() * 17
            o2 = 82.0 + Math.random() * 7
            temp = 38.3 + Math.random() * 1.3
            status = "critical"
        }

        return TelemetryReading(
            heartRate = Math.round(hr * 10.0) / 10.0,
            spo2 = Math.round(o2 * 10.0) / 10.0,
            temperature = Math.round(temp * 10.0) / 10.0,
            timestamp = date,
            status = status
        )
    }

    private fun evaluateReadings(reading: TelemetryReading) {
        if (reading.heartRate > thresholds.heartRateMax) {
            triggerAlert("Heart Rate", "Critical High Pulse Rate: ${reading.heartRate.toInt()} BPM", "Critical")
        } else if (reading.heartRate < thresholds.heartRateMin) {
            triggerAlert("Heart Rate", "Low Pulse Rate: ${reading.heartRate.toInt()} BPM", "Critical")
        }

        if (reading.spo2 < thresholds.spo2Min) {
            val severity = if (reading.spo2 < 90) "Critical" else "Warning"
            triggerAlert("Blood Oxygen", "Oxygen Saturation Breach: ${reading.spo2.toInt()}%", severity)
        }

        if (reading.temperature > thresholds.tempMax) {
            triggerAlert("Body Temperature", "Hyperthermia Threshold Crossed: ${reading.temperature}°C", "Critical")
        }
    }

    private fun triggerAlert(param: String, message: String, severity: String) {
        // Anti-spam guard: skip duplicates within 15 seconds
        val last = alerts.firstOrNull()
        if (last != null && last.parameter == param && (Date().time - last.timestamp.time) < 15000) {
            return
        }

        val newAlert = AlertMessage(parameter = param, message = message, severity = severity, timestamp = Date())
        alerts.add(0, newAlert)
    }

    fun login() {
        isLoggedIn = true
        sharedPrefs.edit().putBoolean("isLoggedIn", true).apply()
    }

    fun logout() {
        isLoggedIn = false
        sharedPrefs.edit().putBoolean("isLoggedIn", false).apply()
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        isDarkMode = enabled
        sharedPrefs.edit().putBoolean("isDarkMode", enabled).apply()
    }

    fun setPasscode(enabled: Boolean) {
        passcodeEnabled = enabled
        sharedPrefs.edit().putBoolean("passcodeEnabled", enabled).apply()
    }

    fun pairDevice(deviceName: String) {
        isDevicePaired = true
        pairedDeviceName = deviceName
        sharedPrefs.edit()
            .putBoolean("isDevicePaired", true)
            .putString("pairedDeviceName", deviceName)
            .apply()
    }

    fun unpairDevice() {
        isDevicePaired = false
        pairedDeviceName = null
        sharedPrefs.edit()
            .remove("isDevicePaired")
            .remove("pairedDeviceName")
            .apply()
    }
}
