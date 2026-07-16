import Foundation
import UserNotifications

class NotificationsManager: ObservableObject {
    @Published var isPermissionGranted = false
    
    func requestPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            DispatchQueue.main.async {
                self.isPermissionGranted = granted
                if let error = error {
                    print("Notifications permission error: \(error.localizedDescription)")
                }
            }
        }
    }
    
    func scheduleMedicationReminder(reminder: MedicationReminder) {
        let content = UNMutableNotificationContent()
        content.title = "Medication Reminder"
        content.body = "It's time to take your \(reminder.name) (\(reminder.dosage))."
        content.sound = .default
        
        // Parse time string e.g. "08:00 AM"
        let formatter = DateFormatter()
        formatter.dateFormat = "hh:mm a"
        guard let date = formatter.date(from: reminder.time) else { return }
        
        let components = Calendar.current.dateComponents([.hour, .minute], from: date)
        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: true)
        
        let request = UNNotificationRequest(identifier: reminder.id.uuidString, content: content, trigger: trigger)
        
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("Error scheduling reminder: \(error.localizedDescription)")
            }
        }
    }
    
    func cancelReminder(reminder: MedicationReminder) {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [reminder.id.uuidString])
    }
    
    func triggerImmediateAlert(title: String, body: String) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default
        
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 0.1, repeats: false)
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger)
        
        UNUserNotificationCenter.current().add(request)
    }
    
    init() {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            DispatchQueue.main.async {
                self.isPermissionGranted = (settings.authorizationStatus == .authorized)
            }
        }
    }
}
