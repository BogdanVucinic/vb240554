package model;

import java.time.LocalTime;
import java.util.Objects;

public class Flight {

    private Airport origin;
    private Airport destination;
    private LocalTime departureTime;
    private int durationMinutes;

    public Flight(Airport origin, Airport destination, LocalTime departureTime, int durationMinutes) {
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.durationMinutes = durationMinutes;
    }

    public Airport getOrigin() {
        return origin;
    }

    public void setOrigin(Airport origin) {
        this.origin = origin;
    }

    public Airport getDestination() {
        return destination;
    }

    public void setDestination(Airport destination) {
        this.destination = destination;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public LocalTime getArrivalTime() {
        return departureTime.plusMinutes(durationMinutes);
    }

    //Ako je relacija i vreme polaska isto onda su isti
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Flight)) {
            return false;
        }
        Flight other = (Flight) obj;
        return Objects.equals(origin, other.origin)
                && Objects.equals(destination, other.destination)
                && Objects.equals(departureTime, other.departureTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination, departureTime);
    }

    @Override
    public String toString() {
        return origin.getCode() + " -> " + destination.getCode()
                + " (polazak " + departureTime + ", trajanje " + durationMinutes + " min)";
    }
}