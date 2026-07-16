import SwiftUI

struct ContentView: View {
    @StateObject private var viewModel = GuardianViewModel()
    @State private var selectedTab = 0
    
    var body: some View {
        Group {
            if !viewModel.isLoggedIn {
                LoginView(viewModel: viewModel)
            } else {
                TabView(selection: $selectedTab) {
                    NavigationStack {
                        DashboardView(viewModel: viewModel)
                            .navigationTitle("Care Dashboard")
                    }
                    .tabItem {
                        Label("Dashboard", systemImage: "heart.text.square.fill")
                    }
                    .tag(0)
                    
                    NavigationStack {
                        AlertsFeedView(viewModel: viewModel)
                            .navigationTitle("Urgent Alerts")
                    }
                    .tabItem {
                        Label("Alerts", systemImage: "bell.badge.fill")
                    }
                    .badge(activeAlertCount)
                    .tag(1)
                    
                    NavigationStack {
                        ContactsView(viewModel: viewModel)
                            .navigationTitle("Clinicians & Caregivers")
                    }
                    .tabItem {
                        Label("Contacts", systemImage: "person.3.fill")
                    }
                    .tag(2)
                    
                    NavigationStack {
                        SettingsView(viewModel: viewModel)
                            .navigationTitle("Settings")
                    }
                    .tabItem {
                        Label("Settings", systemImage: "slider.horizontal.3")
                    }
                    .tag(3)
                }
                .preferredColorScheme(viewModel.isDarkMode ? .dark : .light)
            }
        }
    }
    
    private var activeAlertCount: Int {
        return viewModel.alerts.filter { !$0.isResolved }.count
    }
}
