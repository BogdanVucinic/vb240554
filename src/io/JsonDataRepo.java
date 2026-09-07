package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exceptions.AirTrafficException;
import exceptions.FileParsingException;
import model.Airport;
import model.Flight;

public class JsonDataRepo implements DataRepo {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public LoadResult load(File file, List<Airport> existingAirports, List<Flight> existingFlights)
            throws FileParsingException, IOException {
        String content;
        try {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (FileNotFoundException | java.nio.file.NoSuchFileException e) {
            throw new FileParsingException("Fajl '" + file.getName() + "' ne postoji ili ne moze da se otvori. "
                    + "Proverite putanju i pokusajte ponovo.", e);
        }

        String airportsArray = extractArrayContent(content, "airports");
        String flightsArray = extractArrayContent(content, "flights");

        if (airportsArray == null && flightsArray == null) {
            throw FileParsingException.missingColumns("\"airports\" i/ili \"flights\"");
        }

        List<Airport> newAirports = new ArrayList<>();
        List<Airport> knownAirports = new ArrayList<>(existingAirports);
        int skippedDuplicateAirports = 0;

        if (airportsArray != null) {
            List<String> objects = splitObjects(airportsArray);
            for (int i = 0; i < objects.size(); i++) {
                Airport airport = parseAirportObject(objects.get(i), i + 1);
                if (validation.AirportValidator.exists(airport.getCode(), knownAirports)) {
                    skippedDuplicateAirports++;
                } else {
                    knownAirports.add(airport);
                    newAirports.add(airport);
                }
            }
        }

        List<Flight> newFlights = new ArrayList<>();
        int skippedDuplicateFlights = 0;

        if (flightsArray != null) {
            List<String> objects = splitObjects(flightsArray);
            for (int i = 0; i < objects.size(); i++) {
                Flight flight = parseFlightObject(objects.get(i), i + 1, knownAirports);
                if (existingFlights.contains(flight) || newFlights.contains(flight)) {
                    skippedDuplicateFlights++;
                } else {
                    newFlights.add(flight);
                }
            }
        }

        return new LoadResult(newAirports, newFlights, skippedDuplicateAirports, skippedDuplicateFlights);
    }

    private String extractArrayContent(String content, String key) throws FileParsingException {
        Pattern keyPattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[");
        Matcher matcher = keyPattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        int start = matcher.end(); // pozicija odmah posle '['
        int depth = 1;
        int i = start;
        while (i < content.length() && depth > 0) {
            char c = content.charAt(i);
            if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
            }
            i++;
        }
        if (depth != 0) {
            throw FileParsingException.missingColumns("zatvorena uglasta zagrada za \"" + key + "\"");
        }
        return content.substring(start, i - 1);
    }

    private List<String> splitObjects(String arrayContent) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int objectStart = -1;
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    objectStart = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objectStart != -1) {
                    result.add(arrayContent.substring(objectStart, i + 1));
                    objectStart = -1;
                }
            }
        }
        return result;
    }

    private String extractString(String object, String key, int objectIndex, String context)
            throws FileParsingException {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(object);
        if (!m.find()) {
            throw FileParsingException.atLine(objectIndex,
                    "U " + context + " nedostaje polje \"" + key + "\".");
        }
        return m.group(1);
    }

    private double extractNumber(String object, String key, int objectIndex, String context)
            throws FileParsingException {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+(\\.\\d+)?)").matcher(object);
        if (!m.find()) {
            throw FileParsingException.atLine(objectIndex,
                    "U " + context + " nedostaje ili je neispravno polje \"" + key + "\".");
        }
        return Double.parseDouble(m.group(1));
    }

    private Airport parseAirportObject(String object, int objectIndex) throws FileParsingException {
        String context = "aerodromu br. " + objectIndex;
        String code = extractString(object, "code", objectIndex, context).toUpperCase();
        String name = extractString(object, "name", objectIndex, context);
        double x = extractNumber(object, "x", objectIndex, context);
        double y = extractNumber(object, "y", objectIndex, context);

        try {
            validation.AirportValidator.validateCode(code);
            validation.AirportValidator.validateCoordinates(x, y);
        } catch (AirTrafficException e) {
            throw FileParsingException.atLine(objectIndex, e.getMessage());
        }

        return new Airport(code, name, x, y);
    }

    private Flight parseFlightObject(String object, int objectIndex, List<Airport> knownAirports)
            throws FileParsingException {
        String context = "letu br. " + objectIndex;
        String from = extractString(object, "from", objectIndex, context);
        String to = extractString(object, "to", objectIndex, context);
        String departure = extractString(object, "departure", objectIndex, context);
        double duration = extractNumber(object, "duration", objectIndex, context);

        try {
            return validation.FlightValidator.validateAndBuildFlight(
                    from, to, departure, (int) duration, knownAirports);
        } catch (AirTrafficException e) {
            throw FileParsingException.atLine(objectIndex, e.getMessage());
        }
    }

    @Override
    public void save(List<Airport> airports, List<Flight> flights, File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        sb.append("  \"airports\": [\n");
        for (int i = 0; i < airports.size(); i++) {
            Airport a = airports.get(i);
            sb.append("    {\"code\":\"").append(a.getCode())
              .append("\",\"name\":\"").append(a.getName())
              .append("\",\"x\":").append(a.getX())
              .append(",\"y\":").append(a.getY()).append("}");
            sb.append(i < airports.size() - 1 ? ",\n" : "\n");
        }
        sb.append("  ],\n");

        sb.append("  \"flights\": [\n");
        for (int i = 0; i < flights.size(); i++) {
            Flight f = flights.get(i);
            sb.append("    {\"from\":\"").append(f.getOrigin().getCode())
              .append("\",\"to\":\"").append(f.getDestination().getCode())
              .append("\",\"departure\":\"").append(f.getDepartureTime().format(TIME_FORMAT))
              .append("\",\"duration\":").append(f.getDurationMinutes()).append("}");
            sb.append(i < flights.size() - 1 ? ",\n" : "\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");

        Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8);
    }
}