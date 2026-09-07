package gui;

import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import model.Flight;

public class FlightTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private static final String[] COLUMN_NAMES = {"Polazni aerodrom", "Dolazni aerodrom", "Vreme polaska", "Trajanje (min)"};
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final List<Flight> flights;

    public FlightTableModel(List<Flight> flights) {
        this.flights = flights;
    }

    @Override
    public int getRowCount() {
        return flights.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Flight flight = flights.get(rowIndex);
        switch (columnIndex) {
            case 0: return flight.getOrigin().getCode();
            case 1: return flight.getDestination().getCode();
            case 2: return flight.getDepartureTime().format(TIME_FORMAT);
            case 3: return flight.getDurationMinutes();
            default: throw new IllegalArgumentException("Nepoznata kolona: " + columnIndex);
        }
    }

    public Flight getFlightAt(int rowIndex) {
        return flights.get(rowIndex);
    }

    public void refresh() {
        fireTableDataChanged();
    }
}