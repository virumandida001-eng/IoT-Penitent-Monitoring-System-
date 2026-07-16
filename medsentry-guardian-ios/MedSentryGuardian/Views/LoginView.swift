import SwiftUI

struct LoginView: View {
    @ObservedObject var viewModel: GuardianViewModel
    @State private var email = ""
    @State private var password = ""
    
    var body: some View {
        ZStack {
            // Background
            LinearGradient(
                colors: [Color(red: 0.93, green: 0.96, blue: 0.98), .white],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            VStack(spacing: 30) {
                Spacer()
                
                // Guardian emblem
                VStack(spacing: 12) {
                    ZStack {
                        Circle()
                            .fill(Color.blue.opacity(0.12))
                            .frame(width: 80, height: 80)
                        Image(systemName: "person.2.badge.gearshape.fill")
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 42, height: 42)
                            .foregroundColor(.blue)
                    }
                    
                    Text("MedSentry Guardian")
                        .font(.system(.title, design: .rounded))
                        .fontWeight(.bold)
                        .foregroundColor(Color(red: 0.08, green: 0.16, blue: 0.27))
                    
                    Text("Multi-Patient Remote Health Guardian")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                
                // Form Card
                VStack(spacing: 16) {
                    TextField("Caregiver Email", text: $email)
                        .padding()
                        .background(Color.white)
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.secondary.opacity(0.2), lineWidth: 1)
                        )
                        .keyboardType(.emailAddress)
                        .textInputAutocapitalization(.none)
                    
                    SecureField("Password", text: $password)
                        .padding()
                        .background(Color.white)
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.secondary.opacity(0.2), lineWidth: 1)
                        )
                    
                    Button(action: {
                        if email.isEmpty { email = "guardian.sarah@healthmail.com" }
                        viewModel.login()
                    }) {
                        Text("Sign In")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue)
                            .cornerRadius(12)
                            .shadow(color: Color.blue.opacity(0.2), radius: 6, x: 0, y: 3)
                    }
                }
                .padding(24)
                .background(Color.white.opacity(0.8))
                .cornerRadius(20)
                .shadow(color: Color.black.opacity(0.03), radius: 10, x: 0, y: 5)
                .padding(.horizontal)
                
                Spacer()
                
                Text("Remote Caregiving Portal • Encryption Active")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.bottom)
            }
        }
    }
}
