package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JPanel;
import javax.swing.Timer;

import model.Airplane;
import model.Airport;
import model.Flight;
import model.FlightState;

public class MapPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Color PATH_COLOR = new Color(70, 130, 180); // stil "steel blue", tanka linija putanje

    private static final int SQUARE_SIZE = 10;
    private static final int HIT_PADDING = 5;
    private static final int BLINK_INTERVAL_MS = 400;
    private static final int PLANE_DIAMETER = 8;
    private static final int CLICK_THRESHOLD = 5; // px, iznad ovoga se prevlacenje tretira kao selekcija okvirom

    private static final Color NORMAL_COLOR = Color.GRAY;
    private static final Color SELECTED_COLOR = Color.RED;
    private static final Color PLANE_COLOR = Color.BLUE;
    private static final Color SELECTION_BOX_FILL = new Color(0, 120, 215, 40);
    private static final Color SELECTION_BOX_BORDER = new Color(0, 120, 215);

    private final List<Airport> airports;
    private final CoordinateMapper mapper = new CoordinateMapper(0, 0);
    private final SelectionManager selectionManager = new SelectionManager();
    private final Timer blinkTimer;

    private List<Airplane> airplanes = Collections.emptyList();

    private boolean blinkOn = true;

    private Runnable selectionListener = () -> { };
    private Consumer<Flight> flightRedirectListener = flight -> { };
    private BooleanSupplier isSimulationRunning = () -> false;

    private Predicate<Airport> visibilityFilter = airport -> true;

    private Point pressPoint;
    private Point dragPoint;

    public MapPanel(List<Airport> airports) {
        this.airports = airports;
        setBackground(Color.WHITE);

        blinkTimer = new Timer(BLINK_INTERVAL_MS, e -> {
            blinkOn = !blinkOn;
            repaint();
        });

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mapper.updatePanelSize(getWidth(), getHeight());
                pressPoint = e.getPoint();
                dragPoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                dragPoint = e.getPoint();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (pressPoint != null) {
                    Point releasePoint = e.getPoint();
                    if (pressPoint.distance(releasePoint) < CLICK_THRESHOLD) {
                        handleClick(releasePoint);
                    } else {
                        handleBoxSelect(pressPoint, releasePoint);
                    }
                }
                pressPoint = null;
                dragPoint = null;
                repaint();
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        addMouseWheelListener(e -> {
            mapper.updatePanelSize(getWidth(), getHeight());
            if (e.getWheelRotation() < 0) {
                mapper.zoomIn(e.getX(), e.getY());
            } else {
                mapper.zoomOut(e.getX(), e.getY());
            }
            repaint();
        });
    }

    public void setSelectionListener(Runnable listener) {
        this.selectionListener = listener;
    }

    public void setFlightRedirectListener(Consumer<Flight> listener) {
        this.flightRedirectListener = listener;
    }

    public void setSimulationRunningSupplier(BooleanSupplier isSimulationRunning) {
        this.isSimulationRunning = isSimulationRunning;
    }

    public void setAirplanes(List<Airplane> airplanes) {
        this.airplanes = airplanes;
        repaint();
    }

    public void setVisibilityFilter(Predicate<Airport> visibilityFilter) {
        this.visibilityFilter = visibilityFilter;
        selectionManager.removeAirportsIf(a -> !visibilityFilter.test(a));
        onSelectionChanged();
    }

    public boolean hasSelection() {
        return !selectionManager.isEmpty();
    }

    public void resetZoom() {
        mapper.resetZoom();
        repaint();
    }

    private void handleClick(Point clickPoint) {
        Airport airport = findAirportAt(clickPoint);
        if (airport != null) {
            selectionManager.toggleAirport(airport);
            onSelectionChanged();
            return;
        }

        Airplane airplane = findAirplaneAt(clickPoint);
        if (airplane != null) {
            if (isSimulationRunning.getAsBoolean()) {
                flightRedirectListener.accept(airplane.getFlight());
            } else {
                selectionManager.toggleFlight(airplane.getFlight());
                onSelectionChanged();
            }
        }
    }

    private void handleBoxSelect(Point corner1, Point corner2) {
        int minX = Math.min(corner1.x, corner2.x);
        int maxX = Math.max(corner1.x, corner2.x);
        int minY = Math.min(corner1.y, corner2.y);
        int maxY = Math.max(corner1.y, corner2.y);

        for (Airport airport : airports) {
            if (!visibilityFilter.test(airport)) {
                continue;
            }
            Point p = mapper.toScreen(airport.getX(), airport.getY());
            if (p.x >= minX && p.x <= maxX && p.y >= minY && p.y <= maxY) {
                selectionManager.toggleAirport(airport);
            }
        }

        for (Airplane airplane : airplanes) {
            if (airplane.getState() != FlightState.IN_FLIGHT) {
                continue;
            }
            Point p = mapper.toScreen(airplane.getCurrentX(), airplane.getCurrentY());
            if (p.x >= minX && p.x <= maxX && p.y >= minY && p.y <= maxY) {
                selectionManager.toggleFlight(airplane.getFlight());
            }
        }

        onSelectionChanged();
    }

    private void onSelectionChanged() {
        if (selectionManager.isEmpty()) {
            blinkTimer.stop();
        } else {
            blinkOn = true;
            if (!blinkTimer.isRunning()) {
                blinkTimer.start();
            }
        }
        repaint();
        selectionListener.run();
    }

    private Airport findAirportAt(Point clickPoint) {
        int halfHit = SQUARE_SIZE / 2 + HIT_PADDING;

        Airport closest = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (Airport airport : airports) {
            if (!visibilityFilter.test(airport)) {
                continue;
            }
            Point p = mapper.toScreen(airport.getX(), airport.getY());
            boolean withinHitBox = Math.abs(clickPoint.x - p.x) <= halfHit
                    && Math.abs(clickPoint.y - p.y) <= halfHit;

            if (withinHitBox) {
                double distanceSq = clickPoint.distanceSq(p);
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closest = airport;
                }
            }
        }
        return closest;
    }

    private Airplane findAirplaneAt(Point clickPoint) {
        int halfHit = PLANE_DIAMETER / 2 + HIT_PADDING;

        Airplane closest = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (Airplane airplane : airplanes) {
            if (airplane.getState() != FlightState.IN_FLIGHT) {
                continue;
            }
            Point p = mapper.toScreen(airplane.getCurrentX(), airplane.getCurrentY());
            boolean withinHitBox = Math.abs(clickPoint.x - p.x) <= halfHit
                    && Math.abs(clickPoint.y - p.y) <= halfHit;

            if (withinHitBox) {
                double distanceSq = clickPoint.distanceSq(p);
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closest = airplane;
                }
            }
        }
        return closest;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        mapper.updatePanelSize(getWidth(), getHeight());

        if (airports.isEmpty()) {
            return;
        }

        FontMetrics metrics = g.getFontMetrics();

        for (Airport airport : airports) {
            if (!visibilityFilter.test(airport)) {
                continue;
            }
            Point p = mapper.toScreen(airport.getX(), airport.getY());

            boolean isSelected = selectionManager.isAirportSelected(airport);
            Color squareColor = (isSelected && blinkOn) ? SELECTED_COLOR : NORMAL_COLOR;

            g.setColor(squareColor);
            g.fillRect(p.x - SQUARE_SIZE / 2, p.y - SQUARE_SIZE / 2, SQUARE_SIZE, SQUARE_SIZE);

            g.setColor(Color.BLACK);
            g.drawString(airport.getCode(), p.x + SQUARE_SIZE, p.y + metrics.getAscent() / 2);
        }

        Graphics2D g2 = (Graphics2D) g;
        Stroke defaultStroke = g2.getStroke();
        Stroke pathStroke = new BasicStroke(1f);

        for (Airplane airplane : airplanes) {
            if (airplane.getState() != FlightState.IN_FLIGHT) {
                continue;
            }

            Flight flight = airplane.getFlight();
            Point destination = airplane.isRedirected()
                    ? mapper.toScreen(airplane.getRedirectTarget().getX(), airplane.getRedirectTarget().getY())
                    : mapper.toScreen(flight.getDestination().getX(), flight.getDestination().getY());

            Point p = mapper.toScreen(airplane.getCurrentX(), airplane.getCurrentY());

            g2.setColor(PATH_COLOR);
            g2.setStroke(pathStroke);
            g2.drawLine(p.x, p.y, destination.x, destination.y);

            boolean isSelected = selectionManager.isFlightSelected(flight);
            g2.setColor((isSelected && blinkOn) ? SELECTED_COLOR : PLANE_COLOR);
            g2.fillOval(p.x - PLANE_DIAMETER / 2, p.y - PLANE_DIAMETER / 2, PLANE_DIAMETER, PLANE_DIAMETER);
        }

        g2.setStroke(defaultStroke);

        drawSelectionRectangle(g2);
    }

    private void drawSelectionRectangle(Graphics2D g2) {
        if (pressPoint == null || dragPoint == null) {
            return;
        }
        int x = Math.min(pressPoint.x, dragPoint.x);
        int y = Math.min(pressPoint.y, dragPoint.y);
        int width = Math.abs(dragPoint.x - pressPoint.x);
        int height = Math.abs(dragPoint.y - pressPoint.y);

        Color originalColor = g2.getColor();
        Stroke originalStroke = g2.getStroke();

        g2.setColor(SELECTION_BOX_FILL);
        g2.fillRect(x, y, width, height);
        g2.setColor(SELECTION_BOX_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(x, y, width, height);

        g2.setColor(originalColor);
        g2.setStroke(originalStroke);
    }

    public void refresh() {
        selectionManager.removeAirportsIf(a -> !airports.contains(a));
        onSelectionChanged();
    }
}
