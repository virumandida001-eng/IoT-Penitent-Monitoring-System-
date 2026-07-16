import SwiftUI

struct SettingsView: View {
    @ObservedObject var viewModel: GuardianViewModel
    @State private var enableHighPriority = true
    @State private var enableVibration = true
    
    var body: some View {
        ZStack {
            Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            
            Form {
                // Notifications channels
                Section(header: Text("Critical Alerts Override")) {
                    Toggle("High-Priority Push Bypass", isOn: $enableHighPriority)
                    Toggle("Vibrate on Urgent SOS", isOn: $enableVibration)
                    
                    HStack {
                        Image(systemName: "bell.badge.fill")
                            .foregroundColor(.red)
                        Text("SOS alerts will bypass silent mode settings.")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }
                
                // Account management
                Section(header: Text("Patient Accounts Managed")) {
                    ForEach(viewModel.patients) { patient in
                        HStack {
                            Text(patient.name)
                            Spacer()
                            Text(patient.id)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                
                // Dark mode toggle
                Section(header: Text("Display Settings")) {
                    Toggle("Dark Mode Theme", isOn: $viewModel.isDarkMode)
                }
                
                // Sign out
                Section {
                    Button(action: { viewModel.logout() }) {
                        Text("Sign Out Caregiver")
                            .foregroundColor(.red)
                            .frame(maxWidth: .infinity, alignment: .center)
                    }
                }
            }
            .scrollContentBackground(.hidden)
        }
    }
}
