package io;

import java.util.List;

import model.Airport;
import model.Flight;

public class LoadResult {

    private final List<Airport> airports;
    private final List<Flight> flights;
    private final int skippedDuplicateAirports;
    private final int skippedDuplicateFlights;

    public LoadResult(List<Airport> airports, List<Flight> flights,
            int skippedDuplicateAirports, int skippedDuplicateFlights) {
        this.airports = airports;
        this.flights = flights;
        this.skippedDuplicateAirports = skippedDuplicateAirports;
        this.skippedDuplicateFlights = skippedDuplicateFlights;
    }

    public List<Airport> getAirports() {
        return airports;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public int getSkippedDuplicateAirports() {
        return skippedDuplicateAirports;
    }

    public int getSkippedDuplicateFlights() {
        return skippedDuplicateFlights;
    }
}
