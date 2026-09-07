package model;

public class Airplane {

    private final Flight flight;
    private final int actualDepartureMinute;

    private FlightState state = FlightState.WAITING;
    private double currentX;
    private double currentY;

    private Airport redirectTarget;
    private int redirectStartMinute;
    private double redirectStartX;
    private double redirectStartY;

    public Airplane(Flight flight, int actualDepartureMinute) {
        this.flight = flight;
        this.actualDepartureMinute = actualDepartureMinute;
        this.currentX = flight.getOrigin().getX();
        this.currentY = flight.getOrigin().getY();
    }

    public void redirectTo(Airport target, int currentSimulationMinute) {
        this.redirectTarget = target;
        this.redirectStartMinute = currentSimulationMinute;
        this.redirectStartX = currentX;
        this.redirectStartY = currentY;
    }

    public boolean isRedirected() {
        return redirectTarget != null;
    }

    public Airport getRedirectTarget() {
        return redirectTarget;
    }

    public int getRedirectStartMinute() {
        return redirectStartMinute;
    }

    public double getRedirectStartX() {
        return redirectStartX;
    }

    public double getRedirectStartY() {
        return redirectStartY;
    }

    public Flight getFlight() {
        return flight;
    }

    public int getActualDepartureMinute() {
        return actualDepartureMinute;
    }

    public FlightState getState() {
        return state;
    }

    public void setState(FlightState state) {
        this.state = state;
    }

    public double getCurrentX() {
        return currentX;
    }

    public void setCurrentX(double currentX) {
        this.currentX = currentX;
    }

    public double getCurrentY() {
        return currentY;
    }

    public void setCurrentY(double currentY) {
        this.currentY = currentY;
    }

    @Override
    public String toString() {
        return flight.getOrigin().getCode() + "->" + flight.getDestination().getCode()
                + " [" + state + "] pozicija=(" + currentX + "," + currentY + ")";
    }
}
