package gui;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import model.Airport;
import model.Flight;

class SelectionManager {

    private final Set<Airport> selectedAirports = new HashSet<>();
    private final Set<Flight> selectedFlights = new HashSet<>();

    void toggleAirport(Airport airport) {
        if (!selectedAirports.remove(airport)) {
            selectedAirports.add(airport);
        }
    }

    void toggleFlight(Flight flight) {
        if (!selectedFlights.remove(flight)) {
            selectedFlights.add(flight);
        }
    }

    boolean isAirportSelected(Airport airport) {
        return selectedAirports.contains(airport);
    }

    boolean isFlightSelected(Flight flight) {
        return selectedFlights.contains(flight);
    }

    boolean isEmpty() {
        return selectedAirports.isEmpty() && selectedFlights.isEmpty();
    }

    void removeAirportsIf(Predicate<Airport> condition) {
        selectedAirports.removeIf(condition);
    }
}
