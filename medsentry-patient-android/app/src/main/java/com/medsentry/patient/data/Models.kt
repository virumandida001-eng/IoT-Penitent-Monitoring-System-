package com.medsentry.patient.data

import java.util.Date
import java.util.UUID

data class TelemetryReading(
    val id: UUID = UUID.randomUUID(),
    val heartRate: Double,
    val spo2: Double,
    val temperature: Double,
    val timestamp: Date,
    val status: String // "normal" | "warning" | "critical"
)

data class MedicationReminder(
    val id: UUID = UUID.randomUUID(),
    val time: String,
    val name: String,
    val dosage: String,
    var isTaken: Boolean = false
)

data class EmergencyContact(
    val id: UUID = UUID.randomUUID(),
    var name: String,
    var phone: String,
    var relationship: String,
    var isPrimary: Boolean
)

data class AlertMessage(
    val id: UUID = UUID.randomUUID(),
    val parameter: String,
    val message: String,
    val severity: String, // "Warning" | "Critical"
    val timestamp: Date
)

data class AlertThresholds(
    var heartRateMin: Double = 60.0,
    var heartRateMax: Double = 100.0,
    var spo2Min: Double = 95.0,
    var tempMax: Double = 37.8
)
