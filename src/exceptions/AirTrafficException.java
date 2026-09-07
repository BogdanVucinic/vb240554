package exceptions;


//Bazna klasa za sve izuzetke
public class AirTrafficException extends Exception {

    private static final long serialVersionUID = 1L;

    public AirTrafficException(String message) {
        super(message);
    }

    public AirTrafficException(String message, Throwable cause) {
        super(message, cause);
    }
}