package gui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import model.Airport;

public class AirportTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private static final String[] COLUMN_NAMES = {"Kod", "Naziv", "X", "Y"};

    private final List<Airport> airports;

    public AirportTableModel(List<Airport> airports) {
        this.airports = airports;
    }

    @Override
    public int getRowCount() {
        return airports.size();
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
        Airport airport = airports.get(rowIndex);
        switch (columnIndex) {
            case 0: return airport.getCode();
            case 1: return airport.getName();
            case 2: return airport.getX();
            case 3: return airport.getY();
            default: throw new IllegalArgumentException("Nepoznata kolona: " + columnIndex);
        }
    }

    public Airport getAirportAt(int rowIndex) {
        return airports.get(rowIndex);
    }

    public void refresh() {
        fireTableDataChanged();
    }
}