package exceptions;

public class InvalidCoordinateException extends AirTrafficException {

    private static final long serialVersionUID = 1L;

    public InvalidCoordinateException(double x, double y) {
        super("Koordinate (" + x + ", " + y + ") su van dozvoljenog opsega. "
                + "X mora biti u opsegu [-180, 180], Y u opsegu [-90, 90]. "
                + "Proverite unos i pokusajte ponovo.");
    }
}