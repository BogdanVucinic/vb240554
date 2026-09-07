package simulation;

public class SimulationClock {

    private static final int MINUTES_PER_DAY = 24 * 60;

    private int currentMinute = 0;

    public void advance(int minutes) {
        currentMinute += minutes;
    }

    public void reset() {
        currentMinute = 0;
    }

    public int getCurrentMinute() {
        return currentMinute;
    }
    
    public String getFormattedTime() {
        int minuteOfDay = currentMinute % MINUTES_PER_DAY;
        int hours = minuteOfDay / 60;
        int minutes = minuteOfDay % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}