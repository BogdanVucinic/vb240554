package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import exceptions.AirTrafficException;
import exceptions.FileParsingException;
import model.Airport;
import model.Flight;

public class CsvDataRepo implements DataRepo {

    private static final String AIRPORTS_MARKER = "# AIRPORTS";
    private static final String FLIGHTS_MARKER = "# FLIGHTS";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private enum Section {
        NONE, AIRPORTS, FLIGHTS
    }

    @Override
    public LoadResult load(File file, List<Airport> existingAirports, List<Flight> existingFlights)
            throws FileParsingException, IOException {
        List<Airport> newAirports = new ArrayList<>();
        List<Flight> newFlights = new ArrayList<>();
        List<Airport> knownAirports = new ArrayList<>(existingAirports);

        int skippedDuplicateAirports = 0;
        int skippedDuplicateFlights = 0;

        Section currentSection = Section.NONE;
        int lineNumber = 0;
        boolean sawAirportsHeader = false;
        boolean sawFlightsHeader = false;
        boolean sawAnySection = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();

                if (trimmed.isEmpty()) {
                    continue;
                }
                if (trimmed.equalsIgnoreCase(AIRPORTS_MARKER)) {
                    currentSection = Section.AIRPORTS;
                    sawAirportsHeader = false;
                    sawAnySection = true;
                    continue;
                }
                if (trimmed.equalsIgnoreCase(FLIGHTS_MARKER)) {
                    currentSection = Section.FLIGHTS;
                    sawFlightsHeader = false;
                    sawAnySection = true;
                    continue;
                }
                //prva linija se preskace
                if (currentSection == Section.AIRPORTS && !sawAirportsHeader) {
                    sawAirportsHeader = true;
                    continue;
                }
                if (currentSection == Section.FLIGHTS && !sawFlightsHeader) {
                    sawFlightsHeader = true;
                    continue;
                }

                String[] columns = trimmed.split(",", -1);

                if (currentSection == Section.AIRPORTS) {
                    Airport airport = parseAirportRow(columns, lineNumber);
                    if (validation.AirportValidator.exists(airport.getCode(), knownAirports)) {
                        skippedDuplicateAirports++;
                    } else {
                        knownAirports.add(airport);
                        newAirports.add(airport);
                    }
                } else if (currentSection == Section.FLIGHTS) {
                    Flight flight = parseFlightRow(columns, lineNumber, knownAirports);
                    if (existingFlights.contains(flight) || newFlights.contains(flight)) {
                        skippedDuplicateFlights++;
                    } else {
                        newFlights.add(flight);
                    }
                } else {
                    throw FileParsingException.atLine(lineNumber,
                            "Podaci se pojavljuju pre sekcije '" + AIRPORTS_MARKER + "' ili '" + FLIGHTS_MARKER + "'.");
                }
            }
        } catch (FileNotFoundException e) {
            throw new FileParsingException("Fajl '" + file.getName() + "' ne postoji ili ne moze da se otvori. "
                    + "Proverite putanju i pokusajte ponovo.", e);
        }

        if (!sawAnySection) {
            throw FileParsingException.missingColumns(AIRPORTS_MARKER + " i/ili " + FLIGHTS_MARKER);
        }

        return new LoadResult(newAirports, newFlights, skippedDuplicateAirports, skippedDuplicateFlights);
    }

    private Airport parseAirportRow(String[] columns, int lineNumber) throws FileParsingException {
        if (columns.length != 4) {
            throw FileParsingException.atLine(lineNumber,
                    "Ocekivane su 4 kolone (CODE,NAME,X,Y), a pronadjeno je " + columns.length + ".");
        }
        try {
            String code = columns[0].trim().toUpperCase();
            String name = columns[1].trim();
            double x = Double.parseDouble(columns[2].trim());
            double y = Double.parseDouble(columns[3].trim());

            validation.AirportValidator.validateCode(code);
            validation.AirportValidator.validateCoordinates(x, y);

            return new Airport(code, name, x, y);
        } catch (NumberFormatException e) {
            throw FileParsingException.atLine(lineNumber,
                    "Koordinate X i Y moraju biti brojevi.");
        } catch (AirTrafficException e) {
            throw FileParsingException.atLine(lineNumber, e.getMessage());
        }
    }

    private Flight parseFlightRow(String[] columns, int lineNumber, List<Airport> knownAirports)
            throws FileParsingException {
        if (columns.length != 4) {
            throw FileParsingException.atLine(lineNumber,
                    "Ocekivane su 4 kolone (FROM,TO,DEPARTURE,DURATION), a pronadjeno je " + columns.length + ".");
        }
        try {
            String from = columns[0].trim();
            String to = columns[1].trim();
            String departure = columns[2].trim();
            int duration = Integer.parseInt(columns[3].trim());

            return validation.FlightValidator.validateAndBuildFlight(from, to, departure, duration, knownAirports);
        } catch (NumberFormatException e) {
            throw FileParsingException.atLine(lineNumber, "Trajanje leta mora biti ceo broj.");
        } catch (AirTrafficException e) {
            throw FileParsingException.atLine(lineNumber, e.getMessage());
        }
    }

    @Override
    public void save(List<Airport> airports, List<Flight> flights, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(AIRPORTS_MARKER + "\n");
            writer.write("CODE,NAME,X,Y\n");
            for (Airport a : airports) {
                writer.write(a.getCode() + "," + a.getName() + "," + a.getX() + "," + a.getY() + "\n");
            }

            writer.write("\n" + FLIGHTS_MARKER + "\n");
            writer.write("FROM,TO,DEPARTURE,DURATION\n");
            for (Flight f : flights) {
                writer.write(f.getOrigin().getCode() + "," + f.getDestination().getCode() + ","
                        + f.getDepartureTime().format(TIME_FORMAT) + "," + f.getDurationMinutes() + "\n");
            }
        }
    }
}
