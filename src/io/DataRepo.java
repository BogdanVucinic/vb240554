package io;

import java.io.File;
import java.io.IOException;
import java.util.List;

import exceptions.FileParsingException;
import model.Airport;
import model.Flight;

public interface DataRepo {

    void save(List<Airport> airports, List<Flight> flights, File file) throws IOException;

    LoadResult load(File file, List<Airport> existingAirports, List<Flight> existingFlights)
            throws FileParsingException, IOException;
}