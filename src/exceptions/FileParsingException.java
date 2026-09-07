package exceptions;

public class FileParsingException extends AirTrafficException {

    private static final long serialVersionUID = 1L;

    public FileParsingException(String message) {
        super(message);
    }

    public FileParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static FileParsingException atLine(int lineNumber, String detail) {
        return new FileParsingException(
                "Greska u fajlu, linija " + lineNumber + ": " + detail
                        + " Proverite format linije ili ispravite/unesite novi fajl.");
    }

    public static FileParsingException missingColumns(String expected) {
        return new FileParsingException(
                "Fajl ne sadrzi ocekivane kolone (" + expected + "). "
                        + "Proverite format fajla ili unesite novi fajl.");
    }
}