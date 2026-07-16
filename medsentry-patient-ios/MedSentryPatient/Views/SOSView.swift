import SwiftUI

struct SOSView: View {
    @ObservedObject var viewModel: VitalsViewModel
    @StateObject private var locationManager = LocationManager()
    @State private var countdown = 3
    @State private var isCountingDown = false
    @State private var isSOSActive = false
    @State private var timer: Timer?
    @State private var pulseWave = false
    
    var body: some View {
        ZStack {
            // High visibility red layout during alert
            if isSOSActive {
                Color(red: 0.75, green: 0.1, blue: 0.1).ignoresSafeArea()
            } else {
                Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            }
            
            VStack(spacing: 24) {
                Spacer()
                
                if isSOSActive {
                    // Active Alert Panel
                    VStack(spacing: 20) {
                        ZStack {
                            Circle()
                                .stroke(Color.white.opacity(0.3), lineWidth: 4)
                                .frame(width: 140, height: 140)
                                .scaleEffect(pulseWave ? 1.4 : 0.9)
                                .opacity(pulseWave ? 0.0 : 1.0)
                            
                            Circle()
                                .fill(Color.white)
                                .frame(width: 110, height: 110)
                            
                            Image(systemName: "light.beacon.min.fill")
                                .font(.system(size: 44))
                                .foregroundColor(.red)
                        }
                        .onAppear {
                            withAnimation(Animation.linear(duration: 1.0).repeatForever(autoreverses: false)) {
                                pulseWave = true
                            }
                        }
                        
                        Text("SOS ALARM ACTIVE")
                            .font(.system(.title, design: .rounded))
                            .fontWeight(.black)
                            .foregroundColor(.white)
                        
                        Text("Notifying primary guardian: \(viewModel.emergencyContacts.first?.name ?? "Sarah Pierce")")
                            .foregroundColor(.white.opacity(0.9))
                            .font(.headline)
                        
                        Text("Broadcasting GPS coordinates:\n\(locationManager.coordinatesString)")
                            .font(.system(.callout, design: .monospaced))
                            .foregroundColor(.white.opacity(0.8))
                            .multilineTextAlignment(.center)
                            .padding()
                            .background(Color.black.opacity(0.2))
                            .cornerRadius(12)
                        
                        Text("A high-priority notification was pushed. Calling ambulance and emergency contacts...")
                            .font(.caption)
                            .foregroundColor(.white.opacity(0.7))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal)
                        
                        Button(action: cancelSOS) {
                            Text("Cancel SOS")
                                .fontWeight(.bold)
                                .foregroundColor(.red)
                                .frame(width: 200)
                                .padding()
                                .background(Color.white)
                                .cornerRadius(30)
                                .shadow(radius: 5)
                        }
                    }
                    
                } else if isCountingDown {
                    // Countdown Panel
                    VStack(spacing: 24) {
                        Text("Triggering SOS in")
                            .font(.title2)
                            .foregroundColor(.secondary)
                        
                        Text("\(countdown)")
                            .font(.system(size: 100, weight: .black, design: .rounded))
                            .foregroundColor(.red)
                        
                        Button(action: cancelSOS) {
                            Text("Stop Countdown")
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                                .padding(.horizontal, 30)
                                .padding(.vertical, 15)
                                .background(Color.gray)
                                .cornerRadius(30)
                        }
                    }
                } else {
                    // Inactive Base View
                    VStack(spacing: 20) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.system(size: 60))
                            .foregroundColor(.red)
                        
                        Text("Emergency Assistance")
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundColor(Color(red: 0.08, green: 0.16, blue: 0.27))
                        
                        Text("Tap and hold the button below to initiate an immediate medical distress alarm to guardians and clinician networks.")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 32)
                        
                        // Large Trigger Button
                        Button(action: startCountdown) {
                            ZStack {
                                Circle()
                                    .fill(Color.red)
                                    .frame(width: 180, height: 180)
                                    .shadow(color: Color.red.opacity(0.4), radius: 10, x: 0, y: 5)
                                
                                Text("SOS")
                                    .font(.system(size: 42, weight: .heavy))
                                    .foregroundColor(.white)
                            }
                        }
                        .padding(.vertical, 30)
                        
                        VStack(alignment: .leading, spacing: 6) {
                            HStack {
                                Image(systemName: "mappin.and.ellipse")
                                    .foregroundColor(.blue)
                                Text("Last Known Location:")
                                    .fontWeight(.semibold)
                            }
                            .font(.footnote)
                            
                            Text(locationManager.coordinatesString)
                                .font(.system(.caption2, design: .monospaced))
                                .foregroundColor(.secondary)
                        }
                        .padding()
                        .background(Color.white)
                        .cornerRadius(12)
                    }
                }
                
                Spacer()
            }
        }
        .onAppear {
            locationManager.requestPermission()
            locationManager.startUpdatingLocation()
        }
        .onDisappear {
            locationManager.stopUpdatingLocation()
        }
    }
    
    private func startCountdown() {
        countdown = 3
        isCountingDown = true
        locationManager.simulateSOSLocation() // Refresh coordinates
        
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { _ in
            if self.countdown > 1 {
                self.countdown -= 1
            } else {
                self.timer?.invalidate()
                self.isCountingDown = false
                self.isSOSActive = true
                self.triggerSOSNotification()
            }
        }
    }
    
    private func cancelSOS() {
        timer?.invalidate()
        isCountingDown = false
        isSOSActive = false
    }
    
    private func triggerSOSNotification() {
        // Trigger local notification to mock background alerts
        let loc = locationManager.coordinatesString
        let message = "Emergency Alert triggered from Patient! GPS Location: \(loc)"
        NotificationCenter.default.post(name: NSNotification.Name("SOSAlertActive"), object: nil)
        
        // Simulates Twilio call logs
        print("SIMULATING SMS VIA TWILIO: \(message)")
    }
}
