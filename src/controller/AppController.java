package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import exceptions.DuplicateAirportCodeException;
import exceptions.FileParsingException;
import exceptions.InvalidAirportCodeException;
import exceptions.InvalidCoordinateException;
import exceptions.InvalidDurationException;
import exceptions.InvalidTimeFormatException;
import exceptions.SameOriginDestinationException;
import exceptions.UnknownAirportReferenceException;
import io.DataRepo;
import io.DataRepoFactory;
import io.LoadResult;
import model.Airport;
import model.Flight;
import validation.AirportValidator;
import validation.FlightValidator;

public class AppController {

    private final List<Airport> airports = new ArrayList<>();
    private final List<Flight> flights = new ArrayList<>();

    public List<Airport> getAirports() {
        return Collections.unmodifiableList(airports);
    }

    public List<Flight> getFlights() {
        return Collections.unmodifiableList(flights);
    }

    public Airport addAirport(String code, String name, double x, double y)
            throws InvalidAirportCodeException, InvalidCoordinateException, DuplicateAirportCodeException {

        String normalizedCode = code == null ? null : code.trim().toUpperCase();
        String normalizedName = name == null ? "" : name.trim();

        AirportValidator.validateNewAirport(normalizedCode, x, y, airports);

        Airport airport = new Airport(normalizedCode, normalizedName, x, y);
        airports.add(airport);
        return airport;
    }

    public Flight addFlight(String originCode, String destinationCode, String rawDepartureTime, int durationMinutes)
            throws SameOriginDestinationException, UnknownAirportReferenceException,
            InvalidTimeFormatException, InvalidDurationException {

        Flight flight = FlightValidator.validateAndBuildFlight(
                originCode, destinationCode, rawDepartureTime, durationMinutes, airports);
        flights.add(flight);
        return flight;
    }

    /*public boolean isAirportInUse(Airport airport) {
        return flights.stream()
                .anyMatch(f -> f.getOrigin().equals(airport) || f.getDestination().equals(airport));
    }*/

    public void saveToFile(File file) throws IOException {
        DataRepo repository = DataRepoFactory.forFile(file);
        repository.save(airports, flights, file);
    }

    public LoadResult loadFromFile(File file) throws FileParsingException, IOException {
        DataRepo repository = DataRepoFactory.forFile(file);
        LoadResult result = repository.load(file, airports, flights);

        airports.addAll(result.getAirports());
        flights.addAll(result.getFlights());
        return result;
    }
}