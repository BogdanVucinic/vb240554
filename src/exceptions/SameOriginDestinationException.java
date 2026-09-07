package exceptions;

public class SameOriginDestinationException extends AirTrafficException {

    private static final long serialVersionUID = 1L;

    public SameOriginDestinationException(String code) {
        super("Polazni i dolazni aerodrom ne mogu biti isti ('" + code + "'). "
                + "Izaberite razlicite aerodrome za polazak i dolazak.");
    }
}