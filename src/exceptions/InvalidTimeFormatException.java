package exceptions;

public class InvalidTimeFormatException extends AirTrafficException {

    private static final long serialVersionUID = 1L;

    public InvalidTimeFormatException(String rawValue) {
        super("Vreme '" + rawValue + "' nije u ispravnom formatu. "
                + "Ocekivan format je HH:mm, gde su sati u opsegu 00-23, a minuti 00-59 (npr. '08:30').");
    }
}