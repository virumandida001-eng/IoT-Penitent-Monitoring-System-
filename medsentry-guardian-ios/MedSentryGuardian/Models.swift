import Foundation

struct LinkedPatient: Codable, Identifiable {
    var id: String // e.g. "PATIENT-101"
    var name: String
    var status: String // "normal" | "warning" | "emergency"
    var lastUpdated: Date
    var heartRate: Double
    var spo2: Double
    var temperature: Double
}

struct CareTask: Codable, Identifiable {
    var id = UUID()
    let name: String
    let description: String
    var isCompleted: Bool
    let patientId: String
}

struct HandoverNote: Codable, Identifiable {
    var id = UUID()
    let timestamp: Date
    let author: String
    let text: String
    let patientId: String
}

struct AlertNotification: Codable, Identifiable {
    var id = UUID()
    let parameter: String
    let message: String
    let severity: String // "Warning" | "Critical"
    let timestamp: Date
    var isResolved: Bool
    let patientId: String
}

struct GoogleContact: Identifiable, Codable {
    var id = UUID()
    let name: String
    let phone: String
    let email: String
}
