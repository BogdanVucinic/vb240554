package gui;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import controller.AppController;
import exceptions.AirTrafficException;
import model.Airport;

public class FlightFormPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final AppController controller;
    private final Runnable onFlightAdded;

    private final JComboBox<Airport> originCombo = new JComboBox<>();
    private final JComboBox<Airport> destinationCombo = new JComboBox<>();
    private final JTextField timeField = new JTextField();
    private final JTextField durationField = new JTextField();

    public FlightFormPanel(AppController controller, Runnable onFlightAdded) {
        this.controller = controller;
        this.onFlightAdded = onFlightAdded;
        buildLayout();
    }

    private void buildLayout() {
        setBorder(new TitledBorder("Novi let"));
        setLayout(new GridLayout(2, 5, 5, 5));

        add(new JLabel("Polazni aerodrom:"));
        add(new JLabel("Dolazni aerodrom:"));
        add(new JLabel("Vreme polaska (HH:mm):"));
        add(new JLabel("Trajanje (min):"));
        add(new JLabel(""));

        add(originCombo);
        add(destinationCombo);
        add(timeField);
        add(durationField);

        JButton addButton = new JButton("Dodaj let");
        addButton.addActionListener(e -> handleAdd());
        add(addButton);
    }

    private void handleAdd() {
        Airport origin = (Airport) originCombo.getSelectedItem();
        Airport destination = (Airport) destinationCombo.getSelectedItem();

        if (origin == null || destination == null) {
            ErrorDialogUtil.showError(this, "Prvo morate uneti bar dva aerodroma da biste dodali let.");
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationField.getText().trim());
        } catch (NumberFormatException e) {
            ErrorDialogUtil.showError(this, "Trajanje mora biti ceo broj minuta.");
            return;
        }

        try {
            controller.addFlight(origin.getCode(), destination.getCode(), timeField.getText(), duration);
            clearFields();
            onFlightAdded.run();
        } catch (AirTrafficException e) {
            ErrorDialogUtil.showError(this, e.getMessage());
        }
    }

    private void clearFields() {
        timeField.setText("");
        durationField.setText("");
    }

    public void refreshAirports() {
        Airport previousOrigin = (Airport) originCombo.getSelectedItem();
        Airport previousDestination = (Airport) destinationCombo.getSelectedItem();

        originCombo.removeAllItems();
        destinationCombo.removeAllItems();
        for (Airport airport : controller.getAirports()) {
            originCombo.addItem(airport);
            destinationCombo.addItem(airport);
        }

        // Pokusaj da zadrzi prethodni izbor ako aerodrom i dalje postoji.
        if (previousOrigin != null) {
            originCombo.setSelectedItem(previousOrigin);
        }
        if (previousDestination != null) {
            destinationCombo.setSelectedItem(previousDestination);
        }
    }
}