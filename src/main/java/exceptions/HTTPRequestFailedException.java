package exceptions;

public class HTTPRequestFailedException extends RuntimeException {
    public HTTPRequestFailedException(String message) {
        super(message);
    }
}
