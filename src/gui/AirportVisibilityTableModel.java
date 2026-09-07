package gui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.table.AbstractTableModel;

import model.Airport;

public class AirportVisibilityTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private static final String[] COLUMN_NAMES = {"Prikazi", "Kod", "Naziv", "X", "Y"};

    private final List<Airport> airports;
    private final Set<String> hiddenCodes = new HashSet<>();

    private Runnable onVisibilityChanged = () -> { };

    public AirportVisibilityTableModel(List<Airport> airports) {
        this.airports = airports;
    }
    
    public void setOnVisibilityChanged(Runnable callback) {
        this.onVisibilityChanged = callback;
    }

    public Predicate<Airport> getVisibilityFilter() {
        return airport -> !hiddenCodes.contains(airport.getCode());
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
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Boolean.class;
            case 3:
            case 4: return Double.class; // da bi sortiranje bilo numericko, ne leksikografsko
            default: return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Airport airport = airports.get(rowIndex);
        switch (columnIndex) {
            case 0: return !hiddenCodes.contains(airport.getCode());
            case 1: return airport.getCode();
            case 2: return airport.getName();
            case 3: return airport.getX();
            case 4: return airport.getY();
            default: throw new IllegalArgumentException("Nepoznata kolona: " + columnIndex);
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex != 0) {
            return;
        }
        Airport airport = airports.get(rowIndex);
        boolean visible = Boolean.TRUE.equals(value);

        if (visible) {
            hiddenCodes.remove(airport.getCode());
        } else {
            hiddenCodes.add(airport.getCode());
        }

        fireTableCellUpdated(rowIndex, columnIndex);
        onVisibilityChanged.run();
    }

     //Poziva se posle dodavanja novog aerodroma ili ucitavanja fajla.
     //Novi aerodromi su podrazumevano vidljivi (nisu u hiddenCodes).
    public void refresh() {
        fireTableDataChanged();
    }
}