import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = VitalsViewModel()
    @State private var selectedTab = 0
    @State private var isDevicePaired = UserDefaults.standard.bool(forKey: "isDevicePaired")
    @State private var showingQuickSOSSheet = false
    
    var body: some View {
        Group {
            if !viewModel.isLoggedIn {
                LoginView(viewModel: viewModel)
            } else if !isDevicePaired {
                OnboardingView(viewModel: viewModel)
            } else {
                ZStack {
                    TabView(selection: $selectedTab) {
                        NavigationStack {
                            DashboardView(viewModel: viewModel)
                                .navigationTitle("Dashboard")
                        }
                        .tabItem {
                            Label("Dashboard", systemImage: "square.grid.2x2.fill")
                        }
                        .tag(0)
                        
                        NavigationStack {
                            LiveVitalsView(viewModel: viewModel)
                                .navigationTitle("Live Vitals")
                        }
                        .tabItem {
                            Label("Live Feed", systemImage: "chart.xyaxis.line")
                        }
                        .tag(1)
                        
                        NavigationStack {
                            HistoryView(viewModel: viewModel)
                                .navigationTitle("Vitals History")
                        }
                        .tabItem {
                            Label("History", systemImage: "clock.arrow.2.circlepath")
                        }
                        .tag(2)
                        
                        NavigationStack {
                            SOSView(viewModel: viewModel)
                                .navigationTitle("SOS Alarm")
                        }
                        .tabItem {
                            Label("Emergency", systemImage: "phone.circle.fill")
                        }
                        .tag(3)
                        
                        NavigationStack {
                            ProfileView(viewModel: viewModel)
                                .navigationTitle("Medical Profile")
                        }
                        .tabItem {
                            Label("Profile", systemImage: "person.text.rectangle")
                        }
                        .tag(4)
                        
                        NavigationStack {
                            SettingsView(viewModel: viewModel)
                                .navigationTitle("Settings")
                        }
                        .tabItem {
                            Label("Settings", systemImage: "gearshape.fill")
                        }
                        .tag(5)
                    }
                    .preferredColorScheme(viewModel.isDarkMode ? .dark : .light)
                    
                    // Persistent Floating SOS Action Button (accessible on non-SOS tabs)
                    if selectedTab != 3 {
                        VStack {
                            Spacer()
                            HStack {
                                Spacer()
                                Button(action: {
                                    showingQuickSOSSheet = true
                                }) {
                                    Image(systemName: "exclamationmark.shield.fill")
                                        .font(.title2)
                                        .foregroundColor(.white)
                                        .padding(18)
                                        .background(Color.red)
                                        .clipShape(Circle())
                                        .shadow(color: Color.red.opacity(0.4), radius: 6, x: 0, y: 3)
                                }
                                .padding(.trailing, 20)
                                .padding(.bottom, 70) // Floating above the tab bar
                            }
                        }
                    }
                }
                .sheet(isPresented: $showingQuickSOSSheet) {
                    NavigationStack {
                        SOSView(viewModel: viewModel)
                            .navigationBarTitleDisplayMode(.inline)
                            .toolbar {
                                ToolbarItem(placement: .navigationBarTrailing) {
                                    Button("Dismiss") {
                                        showingQuickSOSSheet = false
                                    }
                                }
                            }
                    }
                }
            }
        }
        .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("DevicePairedSuccessfully"))) { _ in
            isDevicePaired = true
        }
        .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("SOSAlertActive"))) { _ in
            // Route to Emergency Tab on alert trigger
            selectedTab = 3
            showingQuickSOSSheet = false
        }
    }
}
