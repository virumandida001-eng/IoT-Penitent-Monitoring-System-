import Foundation
import Combine
import SwiftUI

class VitalsViewModel: ObservableObject {
    // Auth & App States
    @Published var isLoggedIn = false
    @Published var passcodeEnabled = false
    @Published var vitalCondition: String = "normal" // "normal" | "warning" | "critical"
    
    // Telemetry & Logs
    @Published var currentVitals: TelemetryReading = TelemetryReading(
        heartRate: 78,
        spo2: 98,
        temperature: 36.8,
        timestamp: Date(),
        status: "normal"
    )
    @Published var history: [TelemetryReading] = []
    @Published var alerts: [AlertMessage] = []
    
    // Configurations
    @Published var thresholds = AlertThresholds()
    @Published var isClinicianManaged = false
    
    // Profile & Info
    @Published var patientName = "Alexander Pierce"
    @Published var patientID = "PATIENT-101"
    @Published var email = "alexander.pierce@healthmail.com"
    @Published var bloodGroup = "O-Positive"
    @Published var medicalConditions = "Hypertension, Mild Asthma"
    @Published var allergies = "Penicillin, Peanuts"
    @Published var hospitalInfo = "St. Jude Medical Center - (555) 0199"
    @Published var doctorName = "Dr. Elizabeth Vance"
    @Published var emergencyContacts: [EmergencyContact] = [
        EmergencyContact(name: "Sarah Pierce", phone: "+15550143", relationship: "Spouse", isPrimary: true),
        EmergencyContact(name: "John Pierce", phone: "+15550187", relationship: "Brother", isPrimary: false)
    ]
    
    // Medication Schedule
    @Published var medications: [MedicationReminder] = [
        MedicationReminder(id: UUID(), time: "08:00 AM", name: "Lisinopril", dosage: "10mg", isTaken: false),
        MedicationReminder(id: UUID(), time: "02:00 PM", name: "Aspirin", dosage: "81mg", isTaken: false),
        MedicationReminder(id: UUID(), time: "08:00 PM", name: "Albuterol Inhaler", dosage: "2 Puffs", isTaken: false)
    ]
    
    // Dark mode
    @AppStorage("isDarkMode") var isDarkMode = false
    
    // Battery Simulation
    @Published var sensorBattery = 85
    
    private var telemetryTimer: Timer?
    private var notificationsManager = NotificationsManager()
    
    init() {
        self.isLoggedIn = UserDefaults.standard.bool(forKey: "isLoggedIn")
        self.passcodeEnabled = UserDefaults.standard.bool(forKey: "passcodeEnabled")
        seedInitialHistory()
        startTelemetrySimulation()
    }
    
    func startTelemetrySimulation() {
        telemetryTimer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { [weak self] _ in
            self?.generateNextReading()
        }
    }
    
    func stopTelemetrySimulation() {
        telemetryTimer?.invalidate()
    }
    
    func seedInitialHistory() {
        let now = Date()
        for i in (0...24).reversed() {
            let offsetTime = now.addingTimeInterval(Double(-i * 3600))
            let reading = generateReading(for: "normal", at: offsetTime)
            history.append(reading)
        }
    }
    
    func setSimulatorCondition(_ condition: String) {
        self.vitalCondition = condition
        generateNextReading()
    }
    
    func generateNextReading() {
        let nextReading = generateReading(for: vitalCondition, at: Date())
        currentVitals = nextReading
        history.insert(nextReading, at: 0)
        
        // Keep logs bounded
        if history.count > 100 {
            history.removeLast()
        }
        
        // Drain battery
        if Double.random(in: 0...1) > 0.85 && sensorBattery > 1 {
            sensorBattery -= 1
        }
        
        // Evaluate threshold flags and generate notifications/alarms
        evaluateReadings(nextReading)
    }
    
    private func generateReading(for condition: String, at date: Date) -> TelemetryReading {
        var hr = Double.random(in: 68...82)
        var o2 = Double.random(in: 96...99)
        var temp = Double.random(in: 36.4...37.0)
        var status = "normal"
        
        if condition == "warning" {
            let low = Double.random(in: 0...1) > 0.5
            hr = low ? Double.random(in: 52...58) : Double.random(in: 96...108)
            o2 = Double.random(in: 91...94)
            temp = Double.random(in: 37.3...37.9)
            status = "warning"
        } else if condition == "critical" {
            hr = Double.random(in: 121...138)
            o2 = Double.random(in: 82...88)
            temp = Double.random(in: 38.3...39.6)
            status = "critical"
        }
        
        return TelemetryReading(heartRate: hr, spo2: o2, temperature: temp, timestamp: date, status: status)
    }
    
    private func evaluateReadings(_ reading: TelemetryReading) {
        let formatter = DateFormatter()
        formatter.timeStyle = .medium
        
        if reading.heartRate > thresholds.heartRateMax {
            triggerAlert(param: "Heart Rate", message: "Critical High Pulse Rate: \(Int(reading.heartRate)) BPM", severity: "Critical")
        } else if reading.heartRate < thresholds.heartRateMin {
            triggerAlert(param: "Heart Rate", message: "Low Pulse Rate: \(Int(reading.heartRate)) BPM", severity: "Critical")
        }
        
        if reading.spo2 < thresholds.spo2Min {
            let severity = reading.spo2 < 90 ? "Critical" : "Warning"
            triggerAlert(param: "Blood Oxygen", message: "Oxygen Saturation Breach: \(Int(reading.spo2))%", severity: severity)
        }
        
        if reading.temperature > thresholds.tempMax {
            triggerAlert(param: "Body Temperature", message: "Hyperthermia Threshold Crossed: \(String(format: "%.1f", reading.temperature))°C", severity: "Critical")
        }
    }
    
    private func triggerAlert(param: String, message: String, severity: String) {
        // Anti-spam guard
        if let last = alerts.first, last.parameter == param, Date().timeIntervalSince(last.timestamp) < 15.0 {
            return
        }
        
        let newAlert = AlertMessage(parameter: param, message: message, severity: severity, timestamp: Date())
        alerts.insert(newAlert, at: 0)
        
        // Post local banner
        notificationsManager.triggerImmediateAlert(title: "\(severity) Alert: \(param)", body: message)
    }
    
    func exportReportPDF() -> Data? {
        // Returns simulated PDF document bytes
        return "PDF_REPORT_DATA_SAMPLE".data(using: .utf8)
    }
    
    func togglePasscode() {
        passcodeEnabled.toggle()
        UserDefaults.standard.set(passcodeEnabled, forKey: "passcodeEnabled")
    }
    
    func login() {
        isLoggedIn = true
        UserDefaults.standard.set(true, forKey: "isLoggedIn")
    }
    
    func logout() {
        isLoggedIn = false
        UserDefaults.standard.removeObject(forKey: "isLoggedIn")
    }
}
