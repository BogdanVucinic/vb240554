package gui;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import controller.AppController;
import exceptions.AirTrafficException;

public class AirportFormPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final AppController controller;
    private final Runnable onAirportAdded;

    private final JTextField codeField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField xField = new JTextField();
    private final JTextField yField = new JTextField();

    public AirportFormPanel(AppController controller, Runnable onAirportAdded) {
        this.controller = controller;
        this.onAirportAdded = onAirportAdded;
        buildLayout();
    }

    private void buildLayout() {
        setBorder(new TitledBorder("Novi aerodrom"));
        setLayout(new GridLayout(2, 5, 5, 5));

        add(new javax.swing.JLabel("Kod (npr. BEG):"));
        add(new javax.swing.JLabel("Naziv:"));
        add(new javax.swing.JLabel("X (-180..180):"));
        add(new javax.swing.JLabel("Y (-90..90):"));
        add(new javax.swing.JLabel(""));

        add(codeField);
        add(nameField);
        add(xField);
        add(yField);

        JButton addButton = new JButton("Dodaj aerodrom");
        addButton.addActionListener(e -> handleAdd());
        add(addButton);
    }

    private void handleAdd() {
        String code = codeField.getText();
        String name = nameField.getText();

        double x;
        double y;
        try {
            x = Double.parseDouble(xField.getText().trim());
            y = Double.parseDouble(yField.getText().trim());
        } catch (NumberFormatException e) {
            ErrorDialogUtil.showError(this, "X i Y moraju biti brojevi (npr. 45 ili -23.5).");
            return;
        }

        try {
            controller.addAirport(code, name, x, y);
            clearFields();
            onAirportAdded.run();
        } catch (AirTrafficException e) {
            ErrorDialogUtil.showError(this, e.getMessage());
        }
    }

    private void clearFields() {
        codeField.setText("");
        nameField.setText("");
        xField.setText("");
        yField.setText("");
    }
}