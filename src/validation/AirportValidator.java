package validation;

import java.util.List;

import exceptions.DuplicateAirportCodeException;
import exceptions.InvalidAirportCodeException;
import exceptions.InvalidCoordinateException;
import model.Airport;

public final class AirportValidator {

    private static final int CODE_LENGTH = 3;
    private static final double MIN_X = -180.0;
    private static final double MAX_X = 180.0;
    private static final double MIN_Y = -90.0;
    private static final double MAX_Y = 90.0;

    private AirportValidator() {
        // ne treba instancirati 
    }
    
    public static void validateCode(String code) throws InvalidAirportCodeException {
        if (code == null || code.length() != CODE_LENGTH || !code.equals(code.toUpperCase())
                || !code.chars().allMatch(Character::isLetter)) {
            throw new InvalidAirportCodeException(code);
        }
    }

    public static void validateCoordinates(double x, double y) throws InvalidCoordinateException {
        if (x < MIN_X || x > MAX_X || y < MIN_Y || y > MAX_Y) {
            throw new InvalidCoordinateException(x, y);
        }
    }
    
    public static boolean exists(String code, List<Airport> existingAirports) {
        return existingAirports.stream().anyMatch(a -> a.getCode().equalsIgnoreCase(code));
    }

    public static void checkDuplicate(String code, List<Airport> existingAirports)
            throws DuplicateAirportCodeException {
        if (exists(code, existingAirports)) {
            throw new DuplicateAirportCodeException(code);
        }
    }
    
    public static void validateNewAirport(String code, double x, double y, List<Airport> existingAirports)
            throws InvalidAirportCodeException, InvalidCoordinateException, DuplicateAirportCodeException {
        validateCode(code);
        validateCoordinates(x, y);
        checkDuplicate(code, existingAirports);
    }
}