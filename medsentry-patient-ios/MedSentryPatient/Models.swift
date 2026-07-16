import Foundation

struct TelemetryReading: Codable, Identifiable {
    var id = UUID()
    let heartRate: Double
    let spo2: Double
    let temperature: Double
    let timestamp: Date
    let status: String // "normal" | "warning" | "critical"
}

struct MedicationReminder: Codable, Identifiable {
    let id: UUID
    let time: String // e.g. "08:00 AM"
    let name: String
    let dosage: String
    var isTaken: Bool
}

struct EmergencyContact: Codable, Identifiable {
    var id = UUID()
    var name: String
    var phone: String
    var relationship: String
    var isPrimary: Bool
}

struct AlertMessage: Codable, Identifiable {
    var id = UUID()
    let parameter: String
    let message: String
    let severity: String // "Warning" | "Critical"
    let timestamp: Date
}

struct AlertThresholds: Codable {
    var heartRateMin: Double = 60.0
    var heartRateMax: Double = 100.0
    var spo2Min: Double = 95.0
    var tempMax: Double = 37.8
}
