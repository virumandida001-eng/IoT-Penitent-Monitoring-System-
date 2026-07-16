import SwiftUI

struct HandoverNotesView: View {
    @ObservedObject var viewModel: GuardianViewModel
    let patient: LinkedPatient
    @State private var newNote = ""
    
    var body: some View {
        ZStack {
            Color(red: 0.95, green: 0.97, blue: 0.99).ignoresSafeArea()
            
            VStack(spacing: 16) {
                // Add note input card
                VStack(spacing: 12) {
                    TextField("Enter observation details...", text: $newNote, axis: .vertical)
                        .lineLimit(3...5)
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(12)
                    
                    Button(action: {
                        guard !newNote.trimmingCharacters(in: .whitespaces).isEmpty else { return }
                        viewModel.addHandoverNote(patientId: patient.id, text: newNote)
                        newNote = ""
                    }) {
                        Text("Post Observation Note")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue)
                            .cornerRadius(12)
                    }
                }
                .padding()
                .background(Color.white)
                .cornerRadius(20)
                .padding([.horizontal, .top])
                
                // Chronological notes feed
                List {
                    ForEach(patientNotes) { note in
                        VStack(alignment: .leading, spacing: 6) {
                            HStack {
                                Text(note.author)
                                    .fontWeight(.bold)
                                Spacer()
                                Text(note.timestamp.formatted(date: .abbreviated, time: .shortened))
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                            }
                            Divider()
                            Text(note.text)
                                .font(.subheadline)
                                .foregroundColor(.primary)
                        }
                        .padding(.vertical, 4)
                        .listRowBackground(Color.white)
                    }
                }
                .listStyle(.insetGrouped)
                .scrollContentBackground(.hidden)
            }
        }
        .navigationTitle("Handover Notes")
    }
    
    private var patientNotes: [HandoverNote] {
        return viewModel.notes.filter { $0.patientId == patient.id }
    }
}
