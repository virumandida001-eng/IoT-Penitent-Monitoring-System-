import Foundation
import Combine

class BluetoothDevice: Identifiable, ObservableObject {
    let id = UUID()
    let name: String
    let rssi: Int
    
    init(name: String, rssi: Int) {
        self.name = name
        self.rssi = rssi
    }
}

class BluetoothManager: ObservableObject {
    @Published var isScanning = false
    @Published var isConnected = false
    @Published var isConnecting = false
    @Published var pairedDeviceName: String? = nil
    @Published var discoveredDevices: [BluetoothDevice] = []
    
    private var scanTimer: Timer?
    private var connectionTimer: Timer?
    
    func startScanning() {
        guard !isScanning && !isConnected else { return }
        isScanning = true
        discoveredDevices.removeAll()
        
        // Populate mock devices over time
        var count = 0
        scanTimer = Timer.scheduledTimer(withTimeInterval: 0.8, repeats: true) { [weak self] timer in
            guard let self = self else { return }
            count += 1
            if count == 1 {
                self.discoveredDevices.append(BluetoothDevice(name: "MedSentry Band-4F90", rssi: -62))
            } else if count == 2 {
                self.discoveredDevices.append(BluetoothDevice(name: "ESP32 Vitals Node-C2", rssi: -78))
            } else if count == 3 {
                self.discoveredDevices.append(BluetoothDevice(name: "Smart Ring S5", rssi: -89))
                timer.invalidate()
                self.isScanning = false
            }
        }
    }
    
    func stopScanning() {
        scanTimer?.invalidate()
        isScanning = false
    }
    
    func connect(to device: BluetoothDevice) {
        stopScanning()
        isConnecting = true
        
        connectionTimer = Timer.scheduledTimer(withTimeInterval: 2.0, repeats: false) { [weak self] _ in
            guard let self = self else { return }
            self.isConnecting = false
            self.isConnected = true
            self.pairedDeviceName = device.name
            // Save state in UserDefaults to bypass onboarding on next launches
            UserDefaults.standard.set(true, forKey: "isDevicePaired")
            UserDefaults.standard.set(device.name, forKey: "pairedDeviceName")
        }
    }
    
    func disconnect() {
        isConnected = false
        pairedDeviceName = nil
        UserDefaults.standard.removeObject(forKey: "isDevicePaired")
        UserDefaults.standard.removeObject(forKey: "pairedDeviceName")
    }
    
    init() {
        if UserDefaults.standard.bool(forKey: "isDevicePaired") {
            self.isConnected = true
            self.pairedDeviceName = UserDefaults.standard.string(forKey: "pairedDeviceName") ?? "MedSentry Band-4F90"
        }
    }
}
