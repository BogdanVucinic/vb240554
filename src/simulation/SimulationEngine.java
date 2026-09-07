package simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Timer;

import model.Airplane;
import model.Airport;
import model.Flight;
import model.FlightState;

public class SimulationEngine {

    private static final int TICK_INTERVAL_MS = 200;
    private static final int MINUTES_PER_SECOND = 10; // 1s realnog = 10 min simuliranog (pri brzini 1x)
    private static final int BASE_MINUTES_PER_TICK = TICK_INTERVAL_MS * MINUTES_PER_SECOND / 1000; // = 2

    private final List<Flight> flights;
    private final Runnable onTick;

    private final SimulationClock clock = new SimulationClock();
    private final RunwayScheduler scheduler = new RunwayScheduler();
    private final Timer tickTimer;

    private List<Airplane> airplanes = new ArrayList<>();
    private int speedMultiplier = 1;

    public SimulationEngine(List<Flight> flights, Runnable onTick) { 
        this.flights = flights;
        this.onTick = onTick;
        this.tickTimer = new Timer(TICK_INTERVAL_MS, e -> tick());
        buildAirplanes();
    }

    private void tick() {
        clock.advance(BASE_MINUTES_PER_TICK * speedMultiplier);

        for (Airplane airplane : airplanes) {
            FlightPositionCalculator.updatePosition(airplane, clock.getCurrentMinute());
        }

        onTick.run();
    }

    public void setSpeedMultiplier(int speedMultiplier) {
        this.speedMultiplier = Math.max(1, speedMultiplier);
    }

    public void redirectFlight(Flight flight, Airport target) {
        for (Airplane airplane : airplanes) {
            if (airplane.getFlight().equals(flight) && airplane.getState() == FlightState.IN_FLIGHT) {
                airplane.redirectTo(target, clock.getCurrentMinute());
                return;
            }
        }
    }

    private void buildAirplanes() {
        List<Flight> sortedByDeparture = new ArrayList<>(flights);
        sortedByDeparture.sort(Comparator.comparing(Flight::getDepartureTime));

        airplanes = new ArrayList<>();
        for (Flight flight : sortedByDeparture) {
            int desiredMinute = flight.getDepartureTime().getHour() * 60 + flight.getDepartureTime().getMinute();
            int actualMinute = scheduler.requestTakeoffSlot(flight.getOrigin().getCode(), desiredMinute);
            airplanes.add(new Airplane(flight, actualMinute));
        }
    }

    public void start() {
        if (clock.getCurrentMinute() == 0 && !tickTimer.isRunning()) {
            scheduler.reset();
            buildAirplanes();
        }
        tickTimer.start();
    }

    public void pause() {
        tickTimer.stop();
    }

    public void reset() {
        tickTimer.stop();
        clock.reset();
        scheduler.reset();
        buildAirplanes();
        onTick.run();
    }

    public boolean isRunning() {
        return tickTimer.isRunning();
    }

    public String getFormattedTime() {
        return clock.getFormattedTime();
    }

    public List<Airplane> getAirplanes() {
        return Collections.unmodifiableList(airplanes);
    }
}
