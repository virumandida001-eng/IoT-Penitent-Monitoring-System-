import SwiftUI

struct HistoryView: View {
    @ObservedObject var viewModel: VitalsViewModel
    @State private var searchText = ""
    @State private var selectedDateFilter = 0 // 0: All, 1: Today, 2: Warnings/Critical Only
    @State private var showingExportSheet = false
    @State private var exportedPDFData: Data? = nil
    
    var body: some View {
        ZStack {
            Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            
            VStack(spacing: 12) {
                // Filter Panel Card
                VStack(spacing: 12) {
                    // Search Bar
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(.secondary)
                        TextField("Search by status or readings...", text: $searchText)
                    }
                    .padding(10)
                    .background(Color(.systemGray6))
                    .cornerRadius(10)
                    
                    // Filter segment
                    Picker("Log Filter", selection: $selectedDateFilter) {
                        Text("All Logs").tag(0)
                        Text("Today").tag(1)
                        Text("Alerts").tag(2)
                    }
                    .pickerStyle(.segmented)
                }
                .padding()
                .background(Color.white)
                .cornerRadius(16)
                .padding(.horizontal)
                .padding(.top, 10)
                
                // Export Floating Action
                HStack {
                    Spacer()
                    Button(action: {
                        exportedPDFData = viewModel.exportReportPDF()
                        showingExportSheet = true
                    }) {
                        HStack(spacing: 6) {
                            Image(systemName: "doc.plaintext.fill")
                            Text("Export PDF Report")
                        }
                        .font(.caption)
                        .fontWeight(.bold)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .foregroundColor(.blue)
                        .background(Color.blue.opacity(0.12))
                        .cornerRadius(12)
                    }
                }
                .padding(.horizontal)
                
                // History List
                if filteredHistory.isEmpty {
                    VStack(spacing: 12) {
                        Spacer()
                        Image(systemName: "folder.badge.minus")
                            .font(.system(size: 40))
                            .foregroundColor(.secondary)
                        Text("No records match your filters.")
                            .foregroundColor(.secondary)
                        Spacer()
                    }
                } else {
                    List {
                        ForEach(filteredHistory) { reading in
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    HStack {
                                        Text(reading.timestamp.formatted(date: .abbreviated, time: .shortened))
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                        Spacer()
                                        
                                        // Status Chip
                                        Text(reading.status.uppercased())
                                            .font(.system(size: 8, weight: .bold))
                                            .padding(.horizontal, 6)
                                            .padding(.vertical, 3)
                                            .foregroundColor(statusColor(for: reading.status))
                                            .background(statusColor(for: reading.status).opacity(0.12))
                                            .cornerRadius(6)
                                    }
                                    
                                    HStack(spacing: 20) {
                                        VStack(alignment: .leading) {
                                            Text("HR")
                                                .font(.caption2)
                                                .foregroundColor(.secondary)
                                            Text("\(Int(reading.heartRate)) BPM")
                                                .font(.subheadline)
                                                .fontWeight(.bold)
                                        }
                                        
                                        VStack(alignment: .leading) {
                                            Text("SpO2")
                                                .font(.caption2)
                                                .foregroundColor(.secondary)
                                            Text("\(Int(reading.spo2))%")
                                                .font(.subheadline)
                                                .fontWeight(.bold)
                                        }
                                        
                                        VStack(alignment: .leading) {
                                            Text("Temp")
                                                .font(.caption2)
                                                .foregroundColor(.secondary)
                                            Text(String(format: "%.1f°C", reading.temperature))
                                                .font(.subheadline)
                                                .fontWeight(.bold)
                                        }
                                    }
                                }
                            }
                            .padding(.vertical, 4)
                            .listRowBackground(Color.white)
                        }
                    }
                    .listStyle(.insetGrouped)
                    .scrollContentBackground(.hidden)
                }
            }
        }
        .sheet(isPresented: $showingExportSheet) {
            if let pdfData = exportedPDFData {
                ShareSheet(activityItems: ["MedSentry-Vitals-Report.pdf", pdfData])
            }
        }
    }
    
    private var filteredHistory: [TelemetryReading] {
        return viewModel.history.filter { reading in
            // Search Text filter
            if !searchText.isEmpty {
                let searchLower = searchText.lowercased()
                let matchStatus = reading.status.lowercased().contains(searchLower)
                let matchHr = "\(Int(reading.heartRate))".contains(searchLower)
                let matchO2 = "\(Int(reading.spo2))".contains(searchLower)
                if !matchStatus && !matchHr && !matchO2 { return false }
            }
            
            // Tab filter
            switch selectedDateFilter {
            case 1: // Today only
                return Calendar.current.isDateInToday(reading.timestamp)
            case 2: // Warning & Critical only
                return reading.status != "normal"
            default: // All
                return true
            }
        }
    }
    
    private func statusColor(for status: String) -> Color {
        switch status {
        case "critical": return .red
        case "warning": return .orange
        default: return .green
        }
    }
}

// ShareSheet Helper Wrapper for SwiftUI
struct ShareSheet: UIViewControllerRepresentable {
    var activityItems: [Any]
    var applicationActivities: [UIActivity]? = nil
    
    func makeUIViewController(context: UIViewControllerRepresentableContext<ShareSheet>) -> UIActivityViewController {
        let controller = UIActivityViewController(activityItems: activityItems, applicationActivities: applicationActivities)
        return controller
    }
    
    func updateUIViewController(_ uiViewController: UIActivityViewController, context: UIViewControllerRepresentableContext<ShareSheet>) {}
}
