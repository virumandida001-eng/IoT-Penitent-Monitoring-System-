import SwiftUI

struct ContactsView: View {
    @ObservedObject var viewModel: GuardianViewModel
    @State private var importSuccess = false
    
    var body: some View {
        ZStack {
            Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            
            VStack(spacing: 16) {
                // Google OAuth Integration trigger
                CardButton(
                    title: "Import via Google Contacts",
                    subtitle: "Sync clinician & helper directories",
                    icon: "arrow.triangle.2.circlepath.circle.fill",
                    color: .blue
                ) {
                    // Simulate Google Contacts import callback
                    importSuccess = true
                    viewModel.googleContacts.append(GoogleContact(name: "Dr. David Tennant (ER Direct)", phone: "+15550212", email: "dtennant@hospital.org"))
                }
                .padding([.horizontal, .top])
                
                // Contacts List
                List {
                    ForEach(viewModel.googleContacts) { contact in
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(contact.name)
                                    .fontWeight(.bold)
                                Text(contact.phone)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            Spacer()
                            
                            Button(action: {
                                if let url = URL(string: "tel://\(contact.phone.replacingOccurrences(of: " ", with: ""))") {
                                    UIApplication.shared.open(url)
                                }
                            }) {
                                Image(systemName: "phone.fill")
                                    .foregroundColor(.green)
                                    .padding(10)
                                    .background(Color.green.opacity(0.12))
                                    .cornerRadius(30)
                            }
                            .buttonStyle(.plain)
                        }
                        .padding(.vertical, 4)
                        .listRowBackground(Color.white)
                    }
                }
                .listStyle(.insetGrouped)
                .scrollContentBackground(.hidden)
            }
        }
        .alert("Import Complete", isPresented: $importSuccess) {
            Button("OK", role: .cancel) { }
        } message: {
            Text("Google Contacts synced successfully. Added Dr. David Tennant.")
        }
    }
}

struct CardButton: View {
    let title: String
    let subtitle: String
    let icon: String
    let color: Color
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack {
                ZStack {
                    Circle()
                        .fill(color.opacity(0.12))
                        .frame(width: 48, height: 48)
                    Image(systemName: icon)
                        .font(.title2)
                        .foregroundColor(color)
                }
                .padding(.trailing, 8)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.headline)
                        .foregroundColor(.primary)
                    Text(subtitle)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                Spacer()
                
                Image(systemName: "chevron.right")
                    .foregroundColor(.secondary)
            }
            .padding()
            .background(Color.white)
            .cornerRadius(16)
            .shadow(color: Color.black.opacity(0.02), radius: 5, x: 0, y: 2)
        }
        .buttonStyle(.plain)
    }
}
