package validation;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import exceptions.InvalidDurationException;
import exceptions.InvalidTimeFormatException;
import exceptions.SameOriginDestinationException;
import exceptions.UnknownAirportReferenceException;
import model.Airport;
import model.Flight;

public final class FlightValidator {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private FlightValidator() {
        // Utility klasa
    }

    public static LocalTime parseTime(String rawValue) throws InvalidTimeFormatException {
        if (rawValue == null || rawValue.isBlank()) {
            throw new InvalidTimeFormatException(rawValue);
        }
        try {
            return LocalTime.parse(rawValue.trim(), TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new InvalidTimeFormatException(rawValue);
        }
    }

    public static void validateDuration(int durationMinutes) throws InvalidDurationException {
        if (durationMinutes <= 0) {
            throw new InvalidDurationException(durationMinutes);
        }
    }

    public static void validateDifferentAirports(String originCode, String destinationCode)
            throws SameOriginDestinationException {
        if (originCode != null && originCode.equalsIgnoreCase(destinationCode)) {
            throw new SameOriginDestinationException(originCode);
        }
    }

    public static Airport findAirport(String code, List<Airport> existingAirports)
            throws UnknownAirportReferenceException {
        return existingAirports.stream()
                .filter(a -> a.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new UnknownAirportReferenceException(code));
    }

    public static Flight validateAndBuildFlight(String originCode, String destinationCode,
            String rawDepartureTime, int durationMinutes, List<Airport> existingAirports)
            throws SameOriginDestinationException, UnknownAirportReferenceException,
            InvalidTimeFormatException, InvalidDurationException {

        validateDifferentAirports(originCode, destinationCode);
        Airport origin = findAirport(originCode, existingAirports);
        Airport destination = findAirport(destinationCode, existingAirports);
        LocalTime departureTime = parseTime(rawDepartureTime);
        validateDuration(durationMinutes);

        return new Flight(origin, destination, departureTime, durationMinutes);
    }
}