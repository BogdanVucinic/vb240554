package exceptions;

//Baca se kada kod aerodroma nije tacno tri karaktera ili nije ispisan velikim slovima 
public class InvalidAirportCodeException extends AirTrafficException {

    private static final long serialVersionUID = 1L;

    public InvalidAirportCodeException(String code) {
        super("Kod aerodroma '" + code + "' nije ispravan. "
                + "Kod mora imati tacno tri karaktera i sadrzati samo velika slova (npr. 'BEG').");
    }
}