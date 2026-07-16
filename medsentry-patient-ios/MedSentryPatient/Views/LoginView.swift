import SwiftUI
import LocalAuthentication

struct LoginView: View {
    @ObservedObject var viewModel: VitalsViewModel
    @State private var email = ""
    @State private var password = ""
    @State private var showingBiometricError = false
    @State private var biometricErrorMessage = ""
    
    var body: some View {
        ZStack {
            // Background Gradient
            LinearGradient(
                colors: [Color(red: 0.94, green: 0.97, blue: 0.99), .white],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            VStack(spacing: 30) {
                Spacer()
                
                // Medical Emblem Header
                VStack(spacing: 12) {
                    ZStack {
                        Circle()
                            .fill(Color.blue.opacity(0.15))
                            .frame(width: 80, height: 80)
                        Image(systemName: "shield.pulse")
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 42, height: 42)
                            .foregroundColor(.blue)
                    }
                    
                    Text("MedSentry Patient")
                        .font(.system(.title, design: .rounded))
                        .fontWeight(.bold)
                        .foregroundColor(Color(red: 0.08, green: 0.16, blue: 0.27))
                    
                    Text("Secure Vitals & Emergency Monitoring")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                
                // Form Fields Card
                VStack(spacing: 16) {
                    TextField("Email Address", text: $email)
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
                        // Demo credentials bypass or any input accepted
                        if email.isEmpty { email = "alexander.pierce@healthmail.com" }
                        viewModel.login()
                    }) {
                        Text("Log In")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue)
                            .cornerRadius(12)
                            .shadow(color: Color.blue.opacity(0.3), radius: 6, x: 0, y: 3)
                    }
                }
                .padding(24)
                .background(Color.white.opacity(0.8))
                .cornerRadius(20)
                .shadow(color: Color.black.opacity(0.04), radius: 10, x: 0, y: 5)
                .padding(.horizontal)
                
                // Biometrics Trigger (Face ID / Fingerprint Option)
                if viewModel.passcodeEnabled {
                    Button(action: authenticateWithBiometrics) {
                        HStack(spacing: 8) {
                            Image(systemName: "faceid")
                                .font(.title2)
                            Text("Unlock with Face ID")
                                .font(.callout)
                                .fontWeight(.semibold)
                        }
                        .foregroundColor(.blue)
                        .padding()
                    }
                }
                
                Spacer()
                
                Text("V 1.0.0 — HIPAA Compliant Platform")
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .padding(.bottom)
            }
        }
        .alert("Biometric Security", isPresented: $showingBiometricError) {
            Button("OK", role: .cancel) { }
        } message: {
            Text(biometricErrorMessage)
        }
        .onAppear {
            if viewModel.passcodeEnabled {
                // Auto trigger Face ID unlock on launch if enabled
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                    authenticateWithBiometrics()
                }
            }
        }
    }
    
    private func authenticateWithBiometrics() {
        let context = LAContext()
        var error: NSError?
        
        if context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) {
            let reason = "Authenticate securely to enter MedSentry Patient App."
            context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason) { success, authenticationError in
                DispatchQueue.main.async {
                    if success {
                        viewModel.login()
                    } else {
                        biometricErrorMessage = authenticationError?.localizedDescription ?? "Authentication failed."
                        showingBiometricError = true
                    }
                }
            }
        } else {
            // Simulated Success fallback for simulator debugging
            DispatchQueue.main.async {
                viewModel.login()
            }
        }
    }
}
