package com.medsentry.guardian.data

import java.util.Date
import java.util.UUID

data class LinkedPatient(
    val id: String,
    var name: String,
    var status: String, // "normal" | "warning" | "emergency"
    var lastUpdated: Date,
    var heartRate: Double,
    var spo2: Double,
    var temperature: Double
)

data class CareTask(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    var isCompleted: Boolean = false,
    val patientId: String
)

data class HandoverNote(
    val id: UUID = UUID.randomUUID(),
    val timestamp: Date,
    val author: String,
    val text: String,
    val patientId: String
)

data class AlertNotification(
    val id: UUID = UUID.randomUUID(),
    val parameter: String,
    val message: String,
    val severity: String, // "Warning" | "Critical"
    val timestamp: Date,
    var isResolved: Boolean = false,
    val patientId: String
)

data class GoogleContact(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val phone: String,
    val email: String
)
