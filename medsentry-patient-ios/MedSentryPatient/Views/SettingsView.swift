import SwiftUI

struct SettingsView: View {
    @ObservedObject var viewModel: VitalsViewModel
    @State private var enableVibration = true
    @State private var enableAudio = true
    @State private var inviteCode = "PX-941A"
    @State private var showingQRCode = false
    
    var body: some View {
        ZStack {
            Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            
            Form {
                // Notifications Preferences Section
                Section(header: Text("Notification Preferences")) {
                    Toggle("Enable Vibrate Banners", isOn: $enableVibration)
                    Toggle("Enable Audio Alarms", isOn: $enableAudio)
                }
                
                // Device Linking Section
                Section(header: Text("Guardian Link Config")) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Invite Code for Guardian")
                                .font(.caption)
                                .foregroundColor(.secondary)
                            Text(inviteCode)
                                .font(.headline)
                                .foregroundColor(.blue)
                        }
                        Spacer()
                        
                        Button(action: { showingQRCode.toggle() }) {
                            Image(systemName: "qrcode")
                                .font(.title2)
                        }
                    }
                    
                    if showingQRCode {
                        VStack(spacing: 8) {
                            // Simulated QR Code Graphic
                            ZStack {
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(Color.white)
                                    .frame(width: 140, height: 140)
                                    .shadow(radius: 2)
                                
                                Image(systemName: "qrcode")
                                    .resizable()
                                    .frame(width: 120, height: 120)
                            }
                            Text("Scan this QR in the Guardian app to pair.")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 10)
                    }
                }
                
                // Clinical Thresholds Section
                Section(header: Text("Clinician Managed Thresholds (Read-Only)")) {
                    HStack {
                        Text("Heart Rate Range")
                        Spacer()
                        Text("\(Int(viewModel.thresholds.heartRateMin)) - \(Int(viewModel.thresholds.heartRateMax)) BPM")
                            .foregroundColor(.secondary)
                    }
                    
                    HStack {
                        Text("SpO2 Min Boundary")
                        Spacer()
                        Text("\(Int(viewModel.thresholds.spo2Min)) %")
                            .foregroundColor(.secondary)
                    }
                    
                    HStack {
                        Text("Max Temp Limit")
                        Spacer()
                        Text("\(String(format: "%.1f", viewModel.thresholds.tempMax)) °C")
                            .foregroundColor(.secondary)
                    }
                    
                    HStack {
                        Image(systemName: "lock.shield.fill")
                            .foregroundColor(.orange)
                        Text("Clinician Mode Locked")
                            .font(.caption)
                            .foregroundColor(.orange)
                    }
                }
                
                // Application Customization Section
                Section(header: Text("App Preferences")) {
                    Toggle("Dark Mode", isOn: $viewModel.isDarkMode)
                        .onChange(of: viewModel.isDarkMode) { value in
                            // System-wide color override simulated
                        }
                    
                    HStack {
                        Text("Pairing Status")
                        Spacer()
                        Text("ESP32 Sensor Active")
                            .foregroundColor(.green)
                            .font(.caption)
                    }
                }
                
                // Account Action Section
                Section {
                    Button(action: {
                        viewModel.logout()
                    }) {
                        Text("Log Out")
                            .foregroundColor(.red)
                            .frame(maxWidth: .infinity, alignment: .center)
                    }
                }
            }
            .scrollContentBackground(.hidden)
        }
    }
}
