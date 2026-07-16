import SwiftUI

struct OnboardingView: View {
    @ObservedObject var viewModel: VitalsViewModel
    @StateObject private var bluetoothManager = BluetoothManager()
    @State private var pairingPulse = false
    
    var body: some View {
        ZStack {
            Color(red: 0.96, green: 0.98, blue: 1.0).ignoresSafeArea()
            
            VStack(spacing: 30) {
                // Header
                VStack(spacing: 12) {
                    Text("Device Pairing")
                        .font(.system(.largeTitle, design: .rounded))
                        .fontWeight(.bold)
                        .foregroundColor(Color(red: 0.08, green: 0.16, blue: 0.27))
                    
                    Text("Connect your MedSentry sensor to begin streaming your health vitals in real time.")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }
                .padding(.top, 40)
                
                // Device Scan Visualization
                ZStack {
                    Circle()
                        .stroke(Color.blue.opacity(0.15), lineWidth: 2)
                        .frame(width: 220, height: 220)
                        .scaleEffect(pairingPulse ? 1.2 : 0.8)
                        .opacity(pairingPulse ? 0.0 : 1.0)
                    
                    Circle()
                        .stroke(Color.blue.opacity(0.3), lineWidth: 2)
                        .frame(width: 160, height: 160)
                        .scaleEffect(pairingPulse ? 1.3 : 0.9)
                        .opacity(pairingPulse ? 0.0 : 1.0)
                    
                    Circle()
                        .fill(Color.white)
                        .frame(width: 100, height: 100)
                        .shadow(color: Color.blue.opacity(0.15), radius: 10, x: 0, y: 5)
                    
                    Image(systemName: bluetoothManager.isConnecting ? "hourglass" : "antenna.radiowaves.left.and.right")
                        .font(.largeTitle)
                        .foregroundColor(.blue)
                }
                .padding()
                .onAppear {
                    withAnimation(Animation.linear(duration: 1.8).repeatForever(autoreverses: false)) {
                        pairingPulse = true
                    }
                    bluetoothManager.startScanning()
                }
                .onDisappear {
                    bluetoothManager.stopScanning()
                }
                
                // Device List Cards
                VStack(alignment: .leading, spacing: 12) {
                    Text(bluetoothManager.isConnecting ? "Connecting to sensor..." : "Available Devices")
                        .font(.headline)
                        .foregroundColor(.secondary)
                    
                    if bluetoothManager.isConnecting {
                        HStack {
                            ProgressView()
                                .padding(.trailing, 8)
                            Text("Establishing secure Bluetooth link...")
                                .foregroundColor(.secondary)
                            Spacer()
                        }
                        .padding()
                        .background(Color.white)
                        .cornerRadius(12)
                    } else if bluetoothManager.discoveredDevices.isEmpty {
                        HStack {
                            Text("Searching for signals...")
                                .italic()
                                .foregroundColor(.secondary)
                            Spacer()
                        }
                        .padding()
                        .background(Color.white)
                        .cornerRadius(12)
                    } else {
                        ScrollView {
                            VStack(spacing: 8) {
                                ForEach(bluetoothManager.discoveredDevices) { device in
                                    Button(action: {
                                        bluetoothManager.connect(to: device)
                                    }) {
                                        HStack {
                                            Image(systemName: "cpu")
                                                .font(.headline)
                                                .foregroundColor(.blue)
                                                .padding(10)
                                                .background(Color.blue.opacity(0.1))
                                                .cornerRadius(8)
                                            
                                            VStack(alignment: .leading) {
                                                Text(device.name)
                                                    .font(.headline)
                                                    .foregroundColor(.primary)
                                                Text("Signal strength: \(device.rssi) dBm")
                                                    .font(.caption)
                                                    .foregroundColor(.secondary)
                                            }
                                            
                                            Spacer()
                                            
                                            Image(systemName: "link")
                                                .foregroundColor(.blue)
                                        }
                                        .padding()
                                        .background(Color.white)
                                        .cornerRadius(12)
                                        .shadow(color: Color.black.opacity(0.02), radius: 3, x: 0, y: 1)
                                    }
                                }
                            }
                        }
                        .frame(maxHeight: 180)
                    }
                }
                .padding(.horizontal)
                
                Spacer()
                
                // Skip / Bypass Button
                Button(action: {
                    // Bypass Pairing with a mock default sensor
                    UserDefaults.standard.set(true, forKey: "isDevicePaired")
                    UserDefaults.standard.set("MedSentry Demo Band", forKey: "pairedDeviceName")
                    // Notify layout state changed
                    NotificationCenter.default.post(name: NSNotification.Name("DevicePairedSuccessfully"), object: nil)
                }) {
                    Text("Skip and Use Seeded Simulator")
                        .font(.subheadline)
                        .foregroundColor(.blue)
                        .padding()
                }
            }
        }
    }
}
