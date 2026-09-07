package exceptions;

public class InvalidDurationException extends AirTrafficException {

    private static final long serialVersionUID = 1L;

    public InvalidDurationException(int duration) {
        super("Trajanje leta (" + duration + " min) nije ispravno. "
                + "Trajanje mora biti pozitivan broj minuta.");
    }
}