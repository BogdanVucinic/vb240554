package simulation;

import java.util.HashMap;
import java.util.Map;

public class RunwayScheduler {

    private static final int SLOT_SIZE_MINUTES = 10;

    private final Map<String, Integer> lastAssignedSlotPerAirport = new HashMap<>();

    public int requestTakeoffSlot(String airportCode, int desiredMinute) {
        int desiredSlot = roundDownToTen(desiredMinute);

        Integer lastAssigned = lastAssignedSlotPerAirport.get(airportCode);
        int assignedSlot = desiredSlot;

        if (lastAssigned != null && assignedSlot <= lastAssigned) {
            assignedSlot = lastAssigned + SLOT_SIZE_MINUTES;
        }

        lastAssignedSlotPerAirport.put(airportCode, assignedSlot);
        return assignedSlot;
    }

    private int roundDownToTen(int minute) {
        return (minute / SLOT_SIZE_MINUTES) * SLOT_SIZE_MINUTES;
    }

    public void reset() {
        lastAssignedSlotPerAirport.clear();
    }
}