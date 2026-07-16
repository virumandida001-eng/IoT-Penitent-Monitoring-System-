import SwiftUI

struct AlertsFeedView: View {
    @ObservedObject var viewModel: GuardianViewModel
    
    var body: some View {
        ZStack {
            Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            
            VStack(spacing: 16) {
                // Emergency Quick Actions Bar
                HStack(spacing: 12) {
                    Button(action: {
                        if let url = URL(string: "tel://911") {
                            UIApplication.shared.open(url)
                        }
                    }) {
                        HStack {
                            Image(systemName: "phone.fill")
                            Text("Call 911 Ambulance")
                        }
                        .font(.subheadline)
                        .fontWeight(.black)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.red)
                        .cornerRadius(12)
                        .shadow(color: Color.red.opacity(0.3), radius: 6, x: 0, y: 3)
                    }
                }
                .padding(.horizontal)
                .padding(.top, 10)
                
                // Active Alarm Feed
                if activeAlerts.isEmpty {
                    VStack(spacing: 12) {
                        Spacer()
                        Image(systemName: "bell.slash.fill")
                            .font(.system(size: 40))
                            .foregroundColor(.secondary)
                        Text("No active emergency alerts.")
                            .foregroundColor(.secondary)
                        Spacer()
                    }
                } else {
                    List {
                        ForEach(activeAlerts) { alert in
                            VStack(alignment: .leading, spacing: 8) {
                                HStack {
                                    Text(alert.parameter)
                                        .font(.headline)
                                    Spacer()
                                    
                                    Text(alert.severity.uppercased())
                                        .font(.system(size: 8, weight: .bold))
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 3)
                                        .foregroundColor(.white)
                                        .background(alert.severity == "Critical" ? Color.red : Color.orange)
                                        .cornerRadius(6)
                                }
                                
                                Text(alert.message)
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                                
                                HStack {
                                    Text(alert.timestamp.formatted(date: .omitted, time: .shortened))
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                    
                                    Spacer()
                                    
                                    Button(action: { viewModel.resolveAlert(alertId: alert.id) }) {
                                        Text("Acknowledge & Resolve")
                                            .font(.caption2)
                                            .fontWeight(.bold)
                                            .foregroundColor(.blue)
                                            .padding(.horizontal, 10)
                                            .padding(.vertical, 6)
                                            .background(Color.blue.opacity(0.12))
                                            .cornerRadius(8)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(.vertical, 6)
                            .listRowBackground(Color.white)
                        }
                    }
                    .listStyle(.insetGrouped)
                    .scrollContentBackground(.hidden)
                }
            }
        }
    }
    
    private var activeAlerts: [AlertNotification] {
        return viewModel.alerts.filter { !$0.isResolved }
    }
}
