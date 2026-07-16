import Foundation
import Combine
import SwiftUI

class GuardianViewModel: ObservableObject {
    @Published var isLoggedIn = false
    @Published var isDarkMode = false
    
    // Linked Patients Data
    @Published var patients: [LinkedPatient] = [
        LinkedPatient(id: "PATIENT-101", name: "Alexander Pierce", status: "normal", lastUpdated: Date(), heartRate: 78.0, spo2: 98.0, temperature: 36.8),
        LinkedPatient(id: "PATIENT-102", name: "Jane Doe", status: "warning", lastUpdated: Date(), heartRate: 94.0, spo2: 92.0, temperature: 37.4)
    ]
    
    // Checklist Care Tasks per patient
    @Published var tasks: [CareTask] = [
        CareTask(name: "Give evening medication (Lisinopril)", description: "Patient 101 • 8:00 PM dose", isCompleted: false, patientId: "PATIENT-101"),
        CareTask(name: "Log blood pressure manually", description: "Patient 101 • Daily clinical check", isCompleted: true, patientId: "PATIENT-101"),
        CareTask(name: "Assist with physical therapy walk", description: "Patient 102 • Afternoon routine", isCompleted: false, patientId: "PATIENT-102")
    ]
    
    // Timestamped Handover notes
    @Published var notes: [HandoverNote] = [
        HandoverNote(timestamp: Date().addingTimeInterval(-14400), author: "Guardian Sarah", text: "Alexander was resting well. Heart rate was stable around 75 BPM. He took his morning Lisinopril.", patientId: "PATIENT-101"),
        HandoverNote(timestamp: Date().addingTimeInterval(-28800), author: "Dr. Vance", text: "Checked Jane's charts. SpO2 has been dropping slightly when sleeping. Increased target threshold boundaries.", patientId: "PATIENT-102")
    ]
    
    // Push Alerts feed
    @Published var alerts: [AlertNotification] = [
        AlertNotification(parameter: "Blood Oxygen", message: "Jane Doe: SpO2 breached threshold - 91%", severity: "Warning", timestamp: Date().addingTimeInterval(-3600), isResolved: false, patientId: "PATIENT-102")
    ]
    
    // Google Contacts imported
    @Published var googleContacts: [GoogleContact] = [
        GoogleContact(name: "Dr. Elizabeth Vance (Cardiologist)", phone: "+15550199", email: "elizabeth.vance@hospital.org"),
        GoogleContact(name: "Sarah Pierce (Wife/Primary)", phone: "+15550143", email: "sarah.pierce@healthmail.com"),
        GoogleContact(name: "City Ambulance Service", phone: "911", email: "dispatch@paramedic.org")
    ]
    
    private var streamingTimer: Timer?
    
    init() {
        self.isLoggedIn = UserDefaults.standard.bool(forKey: "isGuardianLoggedIn")
        startVitalsStreamingSimulation()
    }
    
    func startVitalsStreamingSimulation() {
        streamingTimer = Timer.scheduledTimer(withTimeInterval: 5.0, repeats: true) { [weak self] _ in
            self?.fluctuatePatientVitals()
        }
    }
    
    private func fluctuatePatientVitals() {
        for idx in patients.indices {
            var patient = patients[idx]
            
            // Fluctuate values slightly
            let deltaHr = Double.random(in: -3...3)
            let deltaO2 = Double.random(in: -0.5...0.5)
            let deltaTemp = Double.random(in: -0.1...0.1)
            
            var newHr = patient.heartRate + deltaHr
            var newO2 = patient.spo2 + deltaO2
            var newTemp = patient.temperature + deltaTemp
            
            // Constrain ranges
            newHr = max(45.0, min(150.0, newHr))
            newO2 = max(80.0, min(100.0, newO2))
            newTemp = max(35.5, min(40.5, newTemp))
            
            // Determine status
            var newStatus = "normal"
            if newHr > 120.0 || newO2 < 89.0 || newTemp > 38.3 {
                newStatus = "emergency"
            } else if newHr > 95.0 || newO2 < 94.0 || newTemp > 37.3 {
                newStatus = "warning"
            }
            
            patient.heartRate = Math.round(newHr * 10) / 10
            patient.spo2 = Math.round(newO2 * 10) / 10
            patient.temperature = Math.round(newTemp * 10) / 10
            patient.status = newStatus
            patient.lastUpdated = Date()
            
            patients[idx] = patient
            
            // If the patient entered emergency state, trigger critical alert
            if newStatus == "emergency" {
                triggerHighPriorityPushAlert(for: patient)
            }
        }
    }
    
    private func triggerHighPriorityPushAlert(for patient: LinkedPatient) {
        let msg = "\(patient.name) vitals breached: HR \(Int(patient.heartRate)) BPM, SpO2 \(Int(patient.spo2))%"
        // Avoid duplicate alerts within 15 seconds
        if let last = alerts.first, last.patientId == patient.id, Date().timeIntervalSince(last.timestamp) < 15.0 {
            return
        }
        
        let newAlert = AlertNotification(parameter: "Telemetry", message: msg, severity: "Critical", timestamp: Date(), isResolved: false, patientId: patient.id)
        alerts.insert(newAlert, at: 0)
    }
    
    func linkPatientByCode(_ code: String) -> Bool {
        // Mock validation: PX-941A represents Patient-101, QR scan also adds a mock patient
        if code.uppercased() == "PX-941A" {
            // Re-add patient or confirm status
            if !patients.contains(where: { $0.id == "PATIENT-101" }) {
                patients.append(LinkedPatient(id: "PATIENT-101", name: "Alexander Pierce", status: "normal", lastUpdated: Date(), heartRate: 78.0, spo2: 98.0, temperature: 36.8))
            }
            return true
        }
        
        // Scan validation: other code adds mock patient Jane Doe
        if code.starts(with: "QR-") {
            let patientId = "PATIENT-" + code.replacingOccurrences(of: "QR-", with: "")
            if !patients.contains(where: { $0.id == patientId }) {
                patients.append(LinkedPatient(id: patientId, name: "Imported Patient \(patientId)", status: "normal", lastUpdated: Date(), heartRate: 72.0, spo2: 99.0, temperature: 36.6))
            }
            return true
        }
        return false
    }
    
    func toggleTask(task: CareTask) {
        if let index = tasks.firstIndex(where: { $0.id == task.id }) {
            tasks[index].isCompleted.toggle()
        }
    }
    
    func addHandoverNote(patientId: String, text: String) {
        let newNote = HandoverNote(timestamp: Date(), author: "Primary Guardian", text: text, patientId: patientId)
        notes.insert(newNote, at: 0)
    }
    
    func resolveAlert(alertId: UUID) {
        if let index = alerts.firstIndex(where: { $0.id == alertId }) {
            alerts[index].isResolved = true
        }
    }
    
    func login() {
        isLoggedIn = true
        UserDefaults.standard.set(true, forKey: "isGuardianLoggedIn")
    }
    
    func logout() {
        isLoggedIn = false
        UserDefaults.standard.removeObject(forKey: "isGuardianLoggedIn")
    }
}

// Math Round helper for SwiftUI Float double decimals
struct Math {
    static func round(_ value: Double, toDecimalPlaces places: Int) -> Double {
        let divisor = pow(10.0, Double(places))
        return (value * divisor).rounded() / divisor
    }
}
