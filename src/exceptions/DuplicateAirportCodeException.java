package exceptions;
//Kod aerodroma mora biti unikatan
public class DuplicateAirportCodeException extends AirTrafficException {

    private static final long serialVersionUID = 1L;

    public DuplicateAirportCodeException(String code) {
        super("Aerodrom sa kodom '" + code + "' vec postoji. "
                + "Izaberite drugi, jedinstveni troslovni kod.");
    }
}