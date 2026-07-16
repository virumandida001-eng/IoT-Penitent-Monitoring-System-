import SwiftUI
import Charts

struct PatientDetailView: View {
    @ObservedObject var viewModel: GuardianViewModel
    let patient: LinkedPatient
    @State private var showingAddNoteSheet = false
    @State private var newNoteText = ""
    @State private var selectedChartMetric = "Heart Rate"
    
    var body: some View {
        ZStack {
            Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 16) {
                    // Patient Header Card
                    VStack(spacing: 12) {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(patient.name)
                                    .font(.title2)
                                    .fontWeight(.bold)
                                Text("ID: \(patient.id) • Vitals Live Stream")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            Spacer()
                            
                            // Status badge
                            Text(patient.status.uppercased())
                                .font(.caption2)
                                .fontWeight(.bold)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .foregroundColor(statusColor(for: patient.status))
                                .background(statusColor(for: patient.status).opacity(0.12))
                                .cornerRadius(12)
                        }
                        
                        Divider()
                        
                        // Vitals quick stats row
                        HStack(spacing: 20) {
                            DetailVitalStat(label: "HR Pulse", value: "\(Int(patient.heartRate))", unit: "BPM", color: .red)
                            Spacer()
                            DetailVitalStat(label: "Oxygen SpO2", value: "\(Int(patient.spo2))", unit: "%", color: .blue)
                            Spacer()
                            DetailVitalStat(label: "Body Temp", value: String(format: "%.1f", patient.temperature), unit: "°C", color: .orange)
                        }
                        .padding(.vertical, 4)
                    }
                    .padding()
                    .background(Color.white)
                    .cornerRadius(20)
                    .padding(.horizontal)
                    .padding(.top, 10)
                    
                    // Chart Segment Picker & Container
                    VStack(alignment: .leading, spacing: 12) {
                        Picker("Metric Selector", selection: $selectedChartMetric) {
                            Text("Heart Rate").tag("Heart Rate")
                            Text("SpO2").tag("SpO2")
                            Text("Temp").tag("Temp")
                        }
                        .pickerStyle(.segmented)
                        
                        Text("Vitals Trend Graph")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        
                        Chart {
                            ForEach(mockTraceData) { point in
                                LineMark(
                                    x: .value("Time", point.timestamp),
                                    y: .value("Value", getPointValue(point))
                                )
                                .foregroundStyle(metricColor)
                                .interpolationMethod(.catmullRom)
                                
                                AreaMark(
                                    x: .value("Time", point.timestamp),
                                    y: .value("Value", getPointValue(point))
                                )
                                .foregroundStyle(metricColor.opacity(0.1))
                                .interpolationMethod(.catmullRom)
                            }
                        }
                        .chartYScale(domain: chartYDomain)
                        .frame(height: 180)
                    }
                    .padding()
                    .background(Color.white)
                    .cornerRadius(20)
                    .padding(.horizontal)
                    
                    // Action Quick Shortcuts
                    HStack(spacing: 12) {
                        // Handover notes shortcut
                        NavigationLink(destination: HandoverNotesView(viewModel: viewModel, patient: patient)) {
                            HStack {
                                Image(systemName: "note.text")
                                Text("Handover Notes")
                            }
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue)
                            .cornerRadius(12)
                        }
                        
                        // Tasks checklist shortcut
                        NavigationLink(destination: GuardianTasksView(viewModel: viewModel, patient: patient)) {
                            HStack {
                                Image(systemName: "checklist")
                                Text("Care Tasks")
                            }
                            .font(.subheadline)
                            .fontWeight(.bold)
                            .foregroundColor(.blue)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue.opacity(0.12))
                            .cornerRadius(12)
                        }
                    }
                    .padding(.horizontal)
                    
                    // Patient Vitals Alarm History Feed
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Recent Telemetry Alarms")
                            .font(.headline)
                            .foregroundColor(Color(red: 0.08, green: 0.16, blue: 0.27))
                        
                        let patientAlerts = viewModel.alerts.filter { $0.patientId == patient.id }
                        if patientAlerts.isEmpty {
                            Text("No alerts logged for this patient.")
                                .font(.caption)
                                .foregroundColor(.secondary)
                                .italic()
                                .padding()
                                .frame(maxWidth: .infinity, alignment: .center)
                        } else {
                            ForEach(patientAlerts) { alert in
                                HStack {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(alert.parameter)
                                            .fontWeight(.bold)
                                        Text(alert.message)
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                    }
                                    Spacer()
                                    
                                    Text(alert.severity.uppercased())
                                        .font(.system(size: 8, weight: .bold))
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 3)
                                        .foregroundColor(alert.severity == "Critical" ? .red : .orange)
                                        .background((alert.severity == "Critical" ? Color.red : Color.orange).opacity(0.12))
                                        .cornerRadius(6)
                                }
                                .padding()
                                .background(Color.white)
                                .cornerRadius(12)
                            }
                        }
                    }
                    .padding()
                    .background(Color.white.opacity(0.7))
                    .cornerRadius(20)
                    .padding(.horizontal)
                    .padding(.bottom, 20)
                }
            }
        }
        .navigationTitle(patient.name)
        .navigationBarTitleDisplayMode(.inline)
    }
    
    private func statusColor(for status: String) -> Color {
        switch status {
        case "emergency": return .red
        case "warning": return .orange
        default: return .green
        }
    }
    
    private var mockTraceData: [TracePoint] {
        let now = Date()
        return (0..<8).map { i in
            let date = now.addingTimeInterval(Double(-i * 30))
            return TracePoint(
                timestamp: date,
                hr: patient.heartRate + Double.random(in: -4...4),
                spo2: patient.spo2 + Double.random(in: -1...1),
                temp: patient.temperature + Double.random(in: -0.2...0.2)
            )
        }.reversed()
    }
    
    private func getPointValue(_ point: TracePoint) -> Double {
        switch selectedChartMetric {
        case "SpO2": return point.spo2
        case "Temp": return point.temp
        default: return point.hr
        }
    }
    
    private var metricColor: Color {
        switch selectedChartMetric {
        case "SpO2": return .blue
        case "Temp": return .orange
        default: return .red
        }
    }
    
    private var chartYDomain: ClosedRange<Double> {
        switch selectedChartMetric {
        case "SpO2": return 80...100
        case "Temp": return 35...41
        default: return 40...160
        }
    }
}

struct TracePoint: Identifiable {
    let id = UUID()
    let timestamp: Date
    let hr: Double
    let spo2: Double
    let temp: Double
}

struct DetailVitalStat: View {
    let label: String
    let value: String
    let unit: String
    let color: Color
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.caption2)
                .foregroundColor(.secondary)
            HStack(alignment: .firstTextBaseline, spacing: 2) {
                Text(value)
                    .font(.system(.title3, design: .rounded))
                    .fontWeight(.bold)
                    .foregroundColor(color)
                Text(unit)
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }
        }
    }
}
