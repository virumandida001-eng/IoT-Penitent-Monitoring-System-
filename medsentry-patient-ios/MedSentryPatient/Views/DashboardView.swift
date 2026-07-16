import SwiftUI

struct DashboardView: View {
    @ObservedObject var viewModel: VitalsViewModel
    @State private var isHeartAnimating = false
    
    var body: some View {
        ZStack {
            Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 16) {
                    // Sync Status Header Card
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Welcome back,")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text(viewModel.patientName)
                                .font(.title3)
                                .fontWeight(.bold)
                                .foregroundColor(Color(red: 0.08, green: 0.16, blue: 0.27))
                        }
                        
                        Spacer()
                        
                        // Wearable Battery Chip
                        HStack(spacing: 4) {
                            Image(systemName: "battery.100")
                                .foregroundColor(.green)
                            Text("\(viewModel.sensorBattery)%")
                                .font(.caption)
                                .fontWeight(.semibold)
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(Color.green.opacity(0.1))
                        .cornerRadius(20)
                    }
                    .padding(.horizontal)
                    .padding(.top, 10)
                    
                    // Connected Status Chip
                    HStack {
                        Circle()
                            .fill(Color.green)
                            .frame(width: 8, height: 8)
                            .scaleEffect(isHeartAnimating ? 1.2 : 0.8)
                            .animation(.easeInOut(duration: 0.6).repeatForever(autoreverses: true), value: isHeartAnimating)
                        Text("ESP32 Sensor Streaming Vitals")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        Spacer()
                    }
                    .padding(.horizontal)
                    
                    // Vitals Cards Grid
                    VStack(spacing: 12) {
                        // Heart Rate Card
                        VitalCard(
                            title: "Heart Rate",
                            value: "\(Int(viewModel.currentVitals.heartRate))",
                            unit: "BPM",
                            status: getStatusLabel(for: viewModel.currentVitals.heartRate, min: viewModel.thresholds.heartRateMin, max: viewModel.thresholds.heartRateMax),
                            icon: "heart.fill",
                            iconColor: .red,
                            cardColor: .red
                        )
                        
                        // SpO2 Card
                        VitalCard(
                            title: "Blood Oxygen (SpO2)",
                            value: "\(Int(viewModel.currentVitals.spo2))",
                            unit: "%",
                            status: getStatusLabel(for: viewModel.currentVitals.spo2, min: viewModel.thresholds.spo2Min, max: 100),
                            icon: "waveform.path.ecg",
                            iconColor: .blue,
                            cardColor: .blue
                        )
                        
                        // Temperature Card
                        VitalCard(
                            title: "Body Temp",
                            value: String(format: "%.1f", viewModel.currentVitals.temperature),
                            unit: "°C",
                            status: getStatusLabel(for: viewModel.currentVitals.temperature, min: 36.0, max: viewModel.thresholds.tempMax),
                            icon: "thermometer.medium",
                            iconColor: .orange,
                            cardColor: .orange
                        )
                    }
                    .padding(.horizontal)
                    
                    // Medication Card
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Medication Schedule")
                            .font(.headline)
                            .foregroundColor(Color(red: 0.08, green: 0.16, blue: 0.27))
                        
                        ForEach(viewModel.medications.indices, id: \.self) { index in
                            HStack {
                                Button(action: {
                                    viewModel.medications[index].isTaken.toggle()
                                }) {
                                    Image(systemName: viewModel.medications[index].isTaken ? "checkmark.circle.fill" : "circle")
                                        .foregroundColor(viewModel.medications[index].isTaken ? .green : .secondary)
                                        .font(.title3)
                                }
                                
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(viewModel.medications[index].name)
                                        .font(.subheadline)
                                        .fontWeight(.semibold)
                                        .foregroundColor(viewModel.medications[index].isTaken ? .secondary : .primary)
                                    Text("\(viewModel.medications[index].dosage) • \(viewModel.medications[index].time)")
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                                
                                Image(systemName: "pills.fill")
                                    .foregroundColor(.blue.opacity(0.6))
                            }
                            .padding()
                            .background(Color.white)
                            .cornerRadius(12)
                        }
                    }
                    .padding()
                    .background(Color.white.opacity(0.7))
                    .cornerRadius(16)
                    .padding(.horizontal)
                    
                    // Demo Simulation Controller (Clinical Evaluation Panel)
                    VStack(alignment: .leading, spacing: 10) {
                        Text("Clinical Test & Simulation Controller")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(.secondary)
                        
                        HStack(spacing: 8) {
                            Button(action: { viewModel.setSimulatorCondition("normal") }) {
                                Text("Normal")
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(viewModel.vitalCondition == "normal" ? .white : .primary)
                                    .padding(.vertical, 8)
                                    .frame(maxWidth: .infinity)
                                    .background(viewModel.vitalCondition == "normal" ? Color.green : Color.white)
                                    .cornerRadius(8)
                            }
                            
                            Button(action: { viewModel.setSimulatorCondition("warning") }) {
                                Text("Warning")
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(viewModel.vitalCondition == "warning" ? .white : .primary)
                                    .padding(.vertical, 8)
                                    .frame(maxWidth: .infinity)
                                    .background(viewModel.vitalCondition == "warning" ? Color.orange : Color.white)
                                    .cornerRadius(8)
                            }
                            
                            Button(action: { viewModel.setSimulatorCondition("critical") }) {
                                Text("Critical")
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(viewModel.vitalCondition == "critical" ? .white : .primary)
                                    .padding(.vertical, 8)
                                    .frame(maxWidth: .infinity)
                                    .background(viewModel.vitalCondition == "critical" ? Color.red : Color.white)
                                    .cornerRadius(8)
                            }
                        }
                    }
                    .padding()
                    .background(Color.secondary.opacity(0.08))
                    .cornerRadius(16)
                    .padding(.horizontal)
                    .padding(.bottom, 80) // Leave space for SOS and tabs
                }
            }
        }
        .onAppear {
            isHeartAnimating = true
        }
    }
    
    private func getStatusLabel(for value: Double, min: Double, max: Double) -> String {
        if value < min || value > max {
            return "Critical"
        } else if value > (max - (max-min)*0.1) || value < (min + (max-min)*0.1) {
            return "Warning"
        }
        return "Normal"
    }
}

struct VitalCard: View {
    let title: String
    let value: String
    let unit: String
    let status: String
    let icon: String
    let iconColor: Color
    let cardColor: Color
    
    var body: some View {
        HStack {
            ZStack {
                Circle()
                    .fill(iconColor.opacity(0.12))
                    .frame(width: 50, height: 50)
                Image(systemName: icon)
                    .font(.title2)
                    .foregroundColor(iconColor)
            }
            .padding(.trailing, 8)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.caption)
                    .foregroundColor(.secondary)
                HStack(alignment: .firstTextBaseline, spacing: 2) {
                    Text(value)
                        .font(.system(.title, design: .rounded))
                        .fontWeight(.bold)
                    Text(unit)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
            
            Spacer()
            
            // Status Chip
            Text(status)
                .font(.caption2)
                .fontWeight(.bold)
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .foregroundColor(statusColor)
                .background(statusColor.opacity(0.12))
                .cornerRadius(12)
        }
        .padding()
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.02), radius: 5, x: 0, y: 2)
    }
    
    private var statusColor: Color {
        switch status {
        case "Critical": return .red
        case "Warning": return .orange
        default: return .green
        }
    }
}
