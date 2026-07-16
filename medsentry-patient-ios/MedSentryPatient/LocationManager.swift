import Foundation
import CoreLocation
import Combine

class LocationManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    private let locationManager = CLLocationManager()
    
    @Published var authorizationStatus: CLAuthorizationStatus = .notDetermined
    @Published var lastLocation: CLLocation?
    @Published var coordinatesString: String = "Acquiring GPS..."
    
    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = kCLHeaderGPSUsageTimeDouble // standard filter
        self.authorizationStatus = locationManager.authorizationStatus
    }
    
    func requestPermission() {
        locationManager.requestAlwaysAuthorization()
    }
    
    func startUpdatingLocation() {
        locationManager.startUpdatingLocation()
    }
    
    func stopUpdatingLocation() {
        locationManager.stopUpdatingLocation()
    }
    
    func simulateSOSLocation() {
        // In case of permission issues or simulator use, fallback to a clean simulation coordinate
        let mockLat = 37.7749 + (Double.random(in: -0.001...0.001))
        let mockLng = -122.4194 + (Double.random(in: -0.001...0.001))
        self.lastLocation = CLLocation(latitude: mockLat, longitude: mockLng)
        self.coordinatesString = String(format: "Lat: %.5f, Lng: %.5f", mockLat, mockLng)
    }
    
    // Delegate Methods
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        self.authorizationStatus = manager.authorizationStatus
        if manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways {
            locationManager.startUpdatingLocation()
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        self.lastLocation = location
        self.coordinatesString = String(format: "Lat: %.5f, Lng: %.5f", location.coordinate.latitude, location.coordinate.longitude)
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("Location manager failed: \(error.localizedDescription)")
        simulateSOSLocation()
    }
}
