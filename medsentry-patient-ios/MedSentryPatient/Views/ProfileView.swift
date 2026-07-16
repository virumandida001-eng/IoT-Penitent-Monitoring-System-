import SwiftUI

struct ProfileView: View {
    @ObservedObject var viewModel: VitalsViewModel
    @State private var isEditing = false
    
    var body: some View {
        ZStack {
            Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 16) {
                    // Profile Header Card
                    VStack(spacing: 12) {
                        Image(systemName: "person.crop.circle.fill")
                            .resizable()
                            .frame(width: 80, height: 80)
                            .foregroundColor(.blue.opacity(0.8))
                        
                        if isEditing {
                            TextField("Name", text: $viewModel.patientName)
                                .font(.headline)
                                .multilineTextAlignment(.center)
                                .textFieldStyle(.roundedBorder)
                                .padding(.horizontal)
                        } else {
                            Text(viewModel.patientName)
                                .font(.title3)
                                .fontWeight(.bold)
                            
                            Text("ID: \(viewModel.patientID)")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding()
                    .frame(maxWidth: .infinity)
                    .background(Color.white)
                    .cornerRadius(16)
                    .padding(.horizontal)
                    .padding(.top, 10)
                    
                    // Clinical Demographics Card
                    VStack(alignment: .leading, spacing: 14) {
                        Text("Clinical Information")
                            .font(.headline)
                            .foregroundColor(Color(red: 0.08, green: 0.16, blue: 0.27))
                        
                        Divider()
                        
                        ProfileRow(label: "Blood Group", value: $viewModel.bloodGroup, isEditing: isEditing)
                        ProfileRow(label: "Conditions", value: $viewModel.medicalConditions, isEditing: isEditing)
                        ProfileRow(label: "Allergies", value: $viewModel.allergies, isEditing: isEditing)
                        ProfileRow(label: "Hospital", value: $viewModel.hospitalInfo, isEditing: isEditing)
                        ProfileRow(label: "Physician", value: $viewModel.doctorName, isEditing: isEditing)
                    }
                    .padding()
                    .background(Color.white)
                    .cornerRadius(16)
                    .padding(.horizontal)
                    
                    // Emergency Contacts Card
                    VStack(alignment: .leading, spacing: 14) {
                        Text("Emergency Contacts")
                            .font(.headline)
                            .foregroundColor(Color(red: 0.08, green: 0.16, blue: 0.27))
                        
                        Divider()
                        
                        ForEach(viewModel.emergencyContacts) { contact in
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    HStack {
                                        Text(contact.name)
                                            .fontWeight(.bold)
                                        if contact.isPrimary {
                                            Text("PRIMARY")
                                                .font(.system(size: 8, weight: .bold))
                                                .padding(.horizontal, 6)
                                                .padding(.vertical, 2)
                                                .foregroundColor(.red)
                                                .background(Color.red.opacity(0.12))
                                                .cornerRadius(4)
                                        }
                                    }
                                    Text("\(contact.relationship) • \(contact.phone)")
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
                            }
                        }
                    }
                    .padding()
                    .background(Color.white)
                    .cornerRadius(16)
                    .padding(.horizontal)
                    
                    // Edit Toggle Button
                    Button(action: {
                        isEditing.toggle()
                    }) {
                        Text(isEditing ? "Save Profile" : "Edit Clinical Info")
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(isEditing ? Color.green : Color.blue)
                            .cornerRadius(12)
                            .padding(.horizontal)
                    }
                    .padding(.bottom, 20)
                }
            }
        }
    }
}

struct ProfileRow: View {
    let label: String
    @Binding var value: String
    let isEditing: Bool
    
    var body: some View {
        HStack {
            Text(label)
                .font(.subheadline)
                .foregroundColor(.secondary)
                .frame(width: 100, alignment: .leading)
            
            if isEditing {
                TextField(label, text: $value)
                    .textFieldStyle(.roundedBorder)
                    .font(.subheadline)
            } else {
                Text(value)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                Spacer()
            }
        }
    }
}
