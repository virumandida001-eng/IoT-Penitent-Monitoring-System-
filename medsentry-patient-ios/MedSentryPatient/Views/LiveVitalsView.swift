import SwiftUI
import Charts

struct LiveVitalsView: View {
    @ObservedObject var viewModel: VitalsViewModel
    @State private var connectionStatus = true
    @State private var selectedMetric = "Heart Rate"
    
    var body: some View {
        ZStack {
            Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            
            VStack(spacing: 16) {
                // Connection Banner
                HStack {
                    ZStack {
                        Circle()
                            .fill(connectionStatus ? Color.green.opacity(0.12) : Color.red.opacity(0.12))
                            .frame(width: 44, height: 44)
                        Image(systemName: connectionStatus ? "wifi" : "wifi.slash")
                            .foregroundColor(connectionStatus ? .green : .red)
                    }
                    
                    VStack(alignment: .leading, spacing: 2) {
                        Text(connectionStatus ? "Sensor Connected" : "Sensor Disconnected")
                            .font(.subheadline)
                            .fontWeight(.bold)
                        Text(connectionStatus ? "Sampling rate: 1Hz (1 reading/sec)" : "Please check your Bluetooth settings")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                    
                    Toggle("", isOn: $connectionStatus)
                        .labelsHidden()
                        .onChange(of: connectionStatus) { connected in
                            if connected {
                                viewModel.startTelemetrySimulation()
                            } else {
                                viewModel.stopTelemetrySimulation()
                            }
                        }
                }
                .padding()
                .background(Color.white)
                .cornerRadius(16)
                .padding(.horizontal)
                .padding(.top, 10)
                
                // Vitals Picker Tabs
                Picker("Metric Selection", selection: $selectedMetric) {
                    Text("Heart Rate").tag("Heart Rate")
                    Text("SpO2").tag("SpO2")
                    Text("Temp").tag("Temp")
                }
                .pickerStyle(.segmented)
                .padding(.horizontal)
                
                // Chart Display Card
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Real-Time \(selectedMetric) Feed")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text(getCurrentValueString())
                                .font(.title)
                                .fontWeight(.bold)
                                .foregroundColor(Color(red: 0.08, green: 0.16, blue: 0.27))
                        }
                        Spacer()
                        
                        Text("Live Stream")
                            .font(.caption2)
                            .fontWeight(.bold)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .foregroundColor(.red)
                            .background(Color.red.opacity(0.12))
                            .cornerRadius(8)
                    }
                    
                    // Swift Charts view rendering historical trace
                    Chart {
                        ForEach(chartData) { reading in
                            LineMark(
                                x: .value("Time", reading.timestamp, unit: .second),
                                y: .value("Value", getMetricValue(from: reading))
                            )
                            .foregroundStyle(metricColor)
                            .interpolationMethod(.catmullRom)
                            
                            AreaMark(
                                x: .value("Time", reading.timestamp, unit: .second),
                                y: .value("Value", getMetricValue(from: reading))
                            )
                            .foregroundStyle(metricColor.opacity(0.1))
                            .interpolationMethod(.catmullRom)
                        }
                    }
                    .chartYScale(domain: chartYDomain)
                    .frame(height: 220)
                    .padding(.vertical, 10)
                }
                .padding()
                .background(Color.white)
                .cornerRadius(20)
                .shadow(color: Color.black.opacity(0.02), radius: 5, x: 0, y: 2)
                .padding(.horizontal)
                
                // Diagnostics Log
                VStack(alignment: .leading, spacing: 8) {
                    Text("Sensor Console Output")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(.secondary)
                        .padding(.horizontal)
                    
                    ScrollView {
                        VStack(alignment: .leading, spacing: 6) {
                            ForEach(viewModel.history.prefix(5)) { reading in
                                Text("[\(reading.timestamp.formatted(date: .omitted, time: .standard))] SENS: HR=\(Int(reading.heartRate)) SpO2=\(Int(reading.spo2))% TEMP=\(String(format: "%.1f", reading.temperature))°C STATUS=\(reading.status.uppercased())")
                                    .font(.system(.caption, design: .monospaced))
                                    .foregroundColor(.secondary)
                            }
                        }
                        .padding()
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.black.opacity(0.04))
                        .cornerRadius(12)
                    }
                    .frame(maxHeight: 120)
                    .padding(.horizontal)
                }
                
                Spacer()
            }
        }
    }
    
    private var chartData: [TelemetryReading] {
        return viewModel.history.prefix(15).reversed()
    }
    
    private func getMetricValue(from reading: TelemetryReading) -> Double {
        switch selectedMetric {
        case "SpO2": return reading.spo2
        case "Temp": return reading.temperature
        default: return reading.heartRate
        }
    }
    
    private func getCurrentValueString() -> String {
        switch selectedMetric {
        case "SpO2": return "\(Int(viewModel.currentVitals.spo2)) %"
        case "Temp": return String(format: "%.1f °C", viewModel.currentVitals.temperature)
        default: return "\(Int(viewModel.currentVitals.heartRate)) BPM"
        }
    }
    
    private var metricColor: Color {
        switch selectedMetric {
        case "SpO2": return .blue
        case "Temp": return .orange
        default: return .red
        }
    }
    
    private var chartYDomain: ClosedRange<Double> {
        switch selectedMetric {
        case "SpO2": return 80...100
        case "Temp": return 35...41
        default: return 40...160
        }
    }
}
