package gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import controller.AppController;
import exceptions.FileParsingException;
import io.LoadResult;
import model.Airplane;
import model.Airport;
import model.Flight;
import simulation.SimulationEngine;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AppController controller = new AppController();

    private final AirportTableModel airportTableModel = new AirportTableModel(controller.getAirports());
    private final FlightTableModel flightTableModel = new FlightTableModel(controller.getFlights());
    private FlightFormPanel flightFormPanel;
    private MapPanel mapPanel;
    private AirportListSidePanel airportListSidePanel;
    private SimulationControlPanel simulationControlPanel;
    private SimulationEngine simulationEngine;
    private final InactivityTimer inactivityTimer = new InactivityTimer(this, this::handleInactivityTimeout);

    public MainFrame() {
        super("Simulacija avionskog saobracaja");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        setJMenuBar(buildMenuBar());
        add(buildTabs(), BorderLayout.CENTER);

        registerGlobalActivityListener();
        inactivityTimer.start();
    }

    private void registerGlobalActivityListener() {
        Toolkit.getDefaultToolkit().addAWTEventListener(
                event -> inactivityTimer.reset(),
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    private void handleInactivityTimeout() {
        System.exit(0);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Fajl");

        JMenuItem saveCsv = new JMenuItem("Sacuvaj kao CSV...");
        saveCsv.addActionListener(e -> handleSave(".csv"));

        JMenuItem saveJson = new JMenuItem("Sacuvaj kao JSON...");
        saveJson.addActionListener(e -> handleSave(".json"));

        JMenuItem load = new JMenuItem("Ucitaj...");
        load.addActionListener(e -> handleLoad());

        fileMenu.add(saveCsv);
        fileMenu.add(saveJson);
        fileMenu.addSeparator();
        fileMenu.add(load);
        menuBar.add(fileMenu);
        return menuBar;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Aerodromi", buildAirportsTab());
        tabs.addTab("Letovi", buildFlightsTab());
        tabs.addTab("Mapa", buildMapTab());
        return tabs;
    }

    private JPanel buildMapTab() {
        JPanel panel = new JPanel(new BorderLayout());
        mapPanel = new MapPanel(controller.getAirports());

        mapPanel.setSelectionListener(() -> {
            if (mapPanel.hasSelection()) {
                inactivityTimer.pause();
            } else {
                inactivityTimer.resume();
            }
        });

        airportListSidePanel = new AirportListSidePanel(controller.getAirports());
        airportListSidePanel.setOnVisibilityChanged(
                () -> mapPanel.setVisibilityFilter(airportListSidePanel.getVisibilityFilter()));
        mapPanel.setVisibilityFilter(airportListSidePanel.getVisibilityFilter());

        simulationEngine = new SimulationEngine(controller.getFlights(), this::onSimulationTick);
        simulationControlPanel = new SimulationControlPanel(
                this::handleSimulationStart, this::handleSimulationPause, this::handleSimulationReset,
                simulationEngine::setSpeedMultiplier, mapPanel::resetZoom);
        onSimulationTick();

        mapPanel.setSimulationRunningSupplier(simulationEngine::isRunning);
        mapPanel.setFlightRedirectListener(this::handleFlightRedirect);

        panel.add(mapPanel, BorderLayout.CENTER);
        panel.add(airportListSidePanel, BorderLayout.EAST);
        panel.add(simulationControlPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void handleSimulationStart() {
        if (controller.getFlights().isEmpty()) {
            ErrorDialogUtil.showError(this, "Nema unetih letova za simulaciju. Prvo dodajte bar jedan let.");
            return;
        }
        simulationEngine.start();
        inactivityTimer.pause();
    }

    private void handleSimulationPause() {
        simulationEngine.pause();
        inactivityTimer.resume();
    }

    private void handleSimulationReset() {
        simulationEngine.reset();
        inactivityTimer.resume();
    }

    private void handleFlightRedirect(Flight flight) {
        Airplane airplane = findAirplaneForFlight(flight);
        if (airplane == null) {
            return;
        }

        Airport nearest = findNearestOtherVisibleAirport(
                airplane.getCurrentX(), airplane.getCurrentY(), flight.getDestination());
        if (nearest != null) {
            simulationEngine.redirectFlight(flight, nearest);
        }
    }

    private Airplane findAirplaneForFlight(Flight flight) {
        for (Airplane airplane : simulationEngine.getAirplanes()) {
            if (airplane.getFlight().equals(flight)) {
                return airplane;
            }
        }
        return null;
    }

    private Airport findNearestOtherVisibleAirport(double fromX, double fromY, Airport excluded) {
        Airport nearest = null;
        double nearestDistanceSq = Double.MAX_VALUE;

        for (Airport candidate : controller.getAirports()) {
            if (candidate.equals(excluded) || !airportListSidePanel.getVisibilityFilter().test(candidate)) {
                continue;
            }
            double dx = candidate.getX() - fromX;
            double dy = candidate.getY() - fromY;
            double distanceSq = dx * dx + dy * dy;
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq;
                nearest = candidate;
            }
        }
        return nearest;
    }

    private void onSimulationTick() {
        mapPanel.setAirplanes(simulationEngine.getAirplanes());
        simulationControlPanel.setClockText(simulationEngine.getFormattedTime());
    }

    private JPanel buildAirportsTab() {
        JPanel panel = new JPanel(new BorderLayout());

        AirportFormPanel formPanel = new AirportFormPanel(controller, this::onAirportAdded);
        JTable table = new JTable(airportTableModel);

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFlightsTab() {
        JPanel panel = new JPanel(new BorderLayout());

        flightFormPanel = new FlightFormPanel(controller, this::onFlightAdded);
        JTable table = new JTable(flightTableModel);

        panel.add(flightFormPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void onAirportAdded() {
        airportTableModel.refresh();
        flightFormPanel.refreshAirports();
        mapPanel.refresh();
        airportListSidePanel.refresh();
    }

    private void onFlightAdded() {
        flightTableModel.refresh();
    } 

    private void refreshAll() {
        airportTableModel.refresh();
        flightTableModel.refresh();
        flightFormPanel.refreshAirports();
        mapPanel.refresh();
        airportListSidePanel.refresh();
    }

    private void handleSave(String requiredExtension) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                requiredExtension.equals(".csv") ? "CSV fajlovi" : "JSON fajlovi",
                requiredExtension.substring(1)));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(requiredExtension)) {
            file = new File(file.getParentFile(), file.getName() + requiredExtension);
        }

        try {
            controller.saveToFile(file);
            ErrorDialogUtil.showInfo(this, "Podaci su uspesno sacuvani u '" + file.getName() + "'.");
        } catch (IOException e) {
            ErrorDialogUtil.showError(this, "Fajl nije moguce sacuvati: " + e.getMessage());
        }
    }

    private void handleLoad() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV i JSON fajlovi", "csv", "json"));

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try {
            LoadResult loadResult = controller.loadFromFile(file);
            refreshAll();
            ErrorDialogUtil.showInfo(this, buildLoadSummary(file, loadResult));
        } catch (FileParsingException e) {
            ErrorDialogUtil.showError(this, e.getMessage());
        } catch (IOException e) {
            ErrorDialogUtil.showError(this, "Fajl nije moguce procitati: " + e.getMessage());
        }
    }

    private String buildLoadSummary(File file, LoadResult loadResult) {
        StringBuilder summary = new StringBuilder();
        summary.append("Podaci su uspesno ucitani iz '").append(file.getName()).append("'. ");
        summary.append("Dodato: ").append(loadResult.getAirports().size()).append(" aerodrom(a), ");
        summary.append(loadResult.getFlights().size()).append(" let(ova).");

        if (loadResult.getSkippedDuplicateAirports() > 0 || loadResult.getSkippedDuplicateFlights() > 0) {
            summary.append(" Preskoceno kao duplikat: ");
            summary.append(loadResult.getSkippedDuplicateAirports()).append(" aerodrom(a), ");
            summary.append(loadResult.getSkippedDuplicateFlights()).append(" let(ova).");
        }
        return summary.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}