package simulation;

import model.Airplane;
import model.Airport;
import model.Flight;
import model.FlightState;

public final class FlightPositionCalculator {

    private FlightPositionCalculator() {
        // Utility klasa
    }

    public static void updatePosition(Airplane airplane, int currentSimulationMinute) {
        if (airplane.isRedirected()) {
            updateRedirectedPosition(airplane, currentSimulationMinute);
            return;
        }

        Flight flight = airplane.getFlight();
        int start = airplane.getActualDepartureMinute();
        int end = start + flight.getDurationMinutes();

        if (currentSimulationMinute < start) {
            airplane.setState(FlightState.WAITING);
            airplane.setCurrentX(flight.getOrigin().getX());
            airplane.setCurrentY(flight.getOrigin().getY());
            return;
        }

        if (currentSimulationMinute >= end) {
            airplane.setState(FlightState.LANDED);
            airplane.setCurrentX(flight.getDestination().getX());
            airplane.setCurrentY(flight.getDestination().getY());
            return;
        }

        airplane.setState(FlightState.IN_FLIGHT);
        double progress = (currentSimulationMinute - start) / (double) flight.getDurationMinutes();

        double originX = flight.getOrigin().getX();
        double originY = flight.getOrigin().getY();

        double dx = wrappedDelta(originX, flight.getDestination().getX(), X_SPAN);
        double dy = wrappedDelta(originY, flight.getDestination().getY(), Y_SPAN);

        double rawX = originX + progress * dx;
        double rawY = originY + progress * dy;

        airplane.setCurrentX(wrap(rawX, X_SPAN, -180));
        airplane.setCurrentY(wrap(rawY, Y_SPAN, -90));
    }

    // Preusmerena deonica ide "obavijenim" putem preko ivice mape kad je to krace: dx/dy se
    // racunaju sveze svaki tick iz redirectStart i cilja (ne menjaju se tokom leta), pa nema
    // potrebe da se bilo sta pamti na Airplane-u.
    private static void updateRedirectedPosition(Airplane airplane, int currentSimulationMinute) {
        Flight flight = airplane.getFlight();
        Airport target = airplane.getRedirectTarget();

        double originalDx = wrappedDelta(flight.getOrigin().getX(), flight.getDestination().getX(), X_SPAN);
        double originalDy = wrappedDelta(flight.getOrigin().getY(), flight.getDestination().getY(), Y_SPAN);
        double originalSpeed = Math.hypot(originalDx, originalDy) / flight.getDurationMinutes();

        double dx = wrappedDelta(airplane.getRedirectStartX(), target.getX(), X_SPAN);
        double dy = wrappedDelta(airplane.getRedirectStartY(), target.getY(), Y_SPAN);
        double remainingDistance = Math.hypot(dx, dy);
        int redirectDuration = Math.max(1, (int) Math.round(remainingDistance / originalSpeed));

        int elapsed = currentSimulationMinute - airplane.getRedirectStartMinute();

        if (elapsed >= redirectDuration) {
            airplane.setState(FlightState.LANDED);
            airplane.setCurrentX(target.getX());
            airplane.setCurrentY(target.getY());
            return;
        }

        airplane.setState(FlightState.IN_FLIGHT);
        double progress = elapsed / (double) redirectDuration;

        double rawX = airplane.getRedirectStartX() + progress * dx;
        double rawY = airplane.getRedirectStartY() + progress * dy;

        airplane.setCurrentX(wrap(rawX, X_SPAN, -180));
        airplane.setCurrentY(wrap(rawY, Y_SPAN, -90));
    }

    private static final double X_SPAN = 360; // opseg X: -180..180
    private static final double Y_SPAN = 180; // opseg Y: -90..90

    // Najkraca "obavijena" razlika unutar zadatog opsega (npr. 175 -> -175 daje 10, ne -350).
    private static double wrappedDelta(double from, double to, double span) {
        double d = to - from;
        double half = span / 2.0;
        if (d > half) {
            d -= span;
        }
        if (d < -half) {
            d += span;
        }
        return d;
    }

    // Vraca vrednost nazad u opseg [min, min+span), "namotavajuci" je oko ivice.
    private static double wrap(double value, double span, double min) {
        double w = (value - min) % span;
        if (w < 0) {
            w += span;
        }
        return w + min;
    }
}
