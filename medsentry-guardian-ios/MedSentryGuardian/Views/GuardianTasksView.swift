import SwiftUI

struct GuardianTasksView: View {
    @ObservedObject var viewModel: GuardianViewModel
    let patient: LinkedPatient
    
    var body: some View {
        ZStack {
            Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            
            VStack(spacing: 12) {
                // Info Banner
                HStack {
                    Image(systemName: "info.circle.fill")
                        .foregroundColor(.blue)
                    Text("Checklist results are shared with care networks in real time.")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    Spacer()
                }
                .padding()
                .background(Color.white)
                .cornerRadius(12)
                .padding([.horizontal, .top])
                
                // Tasks List
                List {
                    ForEach(patientTasks) { task in
                        HStack {
                            Button(action: { viewModel.toggleTask(task: task) }) {
                                Image(systemName: task.isCompleted ? "checkmark.seal.fill" : "seal")
                                    .foregroundColor(task.isCompleted ? .green : .secondary)
                                    .font(.title3)
                            }
                            .buttonStyle(.plain)
                            
                            VStack(alignment: .leading, spacing: 2) {
                                Text(task.name)
                                    .font(.subheadline)
                                    .fontWeight(.bold)
                                    .foregroundColor(task.isCompleted ? .secondary : .primary)
                                Text(task.description)
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                            Spacer()
                        }
                        .padding(.vertical, 4)
                        .listRowBackground(Color.white)
                    }
                }
                .listStyle(.insetGrouped)
                .scrollContentBackground(.hidden)
            }
        }
        .navigationTitle("\(patient.name) Tasks")
    }
    
    private var patientTasks: [CareTask] {
        return viewModel.tasks.filter { $0.patientId == patient.id }
    }
}
