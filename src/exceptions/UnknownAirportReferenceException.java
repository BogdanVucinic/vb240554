package exceptions;

public class UnknownAirportReferenceException extends AirTrafficException {

    private static final long serialVersionUID = 1L;

    public UnknownAirportReferenceException(String code) {
        super("Aerodrom sa kodom '" + code + "' ne postoji. "
                + "Prvo dodajte aerodrom sa ovim kodom, ili proverite da niste pogresno uneli kod.");
    }
}