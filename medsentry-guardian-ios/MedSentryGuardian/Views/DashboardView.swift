import SwiftUI

struct DashboardView: View {
    @ObservedObject var viewModel: GuardianViewModel
    @State private var showingLinkSheet = false
    @State private var linkCode = ""
    @State private var linkSuccess = false
    @State private var linkError = false
    
    var body: some View {
        ZStack {
            Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 16) {
                    // Header Bar
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Guardian Care Platform")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text("Linked Patients")
                                .font(.title3)
                                .fontWeight(.bold)
                                .foregroundColor(Color(red: 0.08, green: 0.16, blue: 0.27))
                        }
                        Spacer()
                        
                        Button(action: { showingLinkSheet.toggle() }) {
                            HStack(spacing: 4) {
                                Image(systemName: "plus")
                                Text("Link Patient")
                            }
                            .font(.caption)
                            .fontWeight(.bold)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .foregroundColor(.blue)
                            .background(Color.blue.opacity(0.12))
                            .cornerRadius(12)
                        }
                    }
                    .padding(.horizontal)
                    .padding(.top, 10)
                    
                    // Patient list
                    ForEach(viewModel.patients) { patient in
                        NavigationLink(destination: PatientDetailView(viewModel: viewModel, patient: patient)) {
                            HStack(spacing: 16) {
                                // Status bulb
                                Circle()
                                    .fill(statusColor(for: patient.status))
                                    .frame(width: 14, height: 14)
                                    .overlay(
                                        Circle()
                                            .stroke(statusColor(for: patient.status).opacity(0.3), lineWidth: 4)
                                    )
                                
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(patient.name)
                                        .font(.headline)
                                        .foregroundColor(.primary)
                                    
                                    Text("ID: \(patient.id) • Updated \(patient.lastUpdated.formatted(date: .omitted, time: .shortened))")
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                }
                                Spacer()
                                
                                // Vitals summary column
                                VStack(alignment: .trailing, spacing: 4) {
                                    Text("\(Int(patient.heartRate)) BPM")
                                        .font(.system(.subheadline, design: .monospaced))
                                        .fontWeight(.bold)
                                        .foregroundColor(.primary)
                                    Text("\(Int(patient.spo2))% SpO2")
                                        .font(.system(.caption, design: .monospaced))
                                        .foregroundColor(.blue)
                                }
                                
                                Image(systemName: "chevron.right")
                                    .foregroundColor(.secondary)
                                    .font(.footnote)
                            }
                            .padding()
                            .background(Color.white)
                            .cornerRadius(16)
                            .shadow(color: Color.black.opacity(0.02), radius: 5, x: 0, y: 2)
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(.horizontal)
                }
            }
        }
        .sheet(isPresented: $showingLinkSheet) {
            NavigationStack {
                VStack(spacing: 20) {
                    Text("Link Patient Account")
                        .font(.title2)
                        .fontWeight(.bold)
                        .padding(.top)
                    
                    Text("Enter the 7-digit Invite Code displayed on the patient's Settings page or scan their code.")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal)
                    
                    TextField("Code (e.g. PX-941A)", text: $linkCode)
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(12)
                        .padding(.horizontal)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.characters)
                    
                    Button(action: {
                        if viewModel.linkPatientByCode(linkCode) {
                            linkSuccess = true
                            showingLinkSheet = false
                            linkCode = ""
                        } else {
                            linkError = true
                        }
                    }) {
                        Text("Verify and Add Link")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue)
                            .cornerRadius(12)
                            .padding(.horizontal)
                    }
                    
                    Spacer()
                }
                .navigationTitle("Link Patient")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .navigationBarLeading) {
                        Button("Cancel") { showingLinkSheet = false }
                    }
                }
                .alert("Link Success", isPresented: $linkSuccess) {
                    Button("OK", role: .cancel) { }
                } message: {
                    Text("Successfully linked to patient record.")
                }
                .alert("Verification Failed", isPresented: $linkError) {
                    Button("Retry", role: .cancel) { }
                } message: {
                    Text("Invalid invite code. Please check code inputs.")
                }
            }
        }
    }
    
    private func statusColor(for status: String) -> Color {
        switch status {
        case "emergency": return .red
        case "warning": return .orange
        default: return .green
        }
    }
}
