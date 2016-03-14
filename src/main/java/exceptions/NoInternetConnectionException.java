package exceptions;

public class NoInternetConnectionException extends RuntimeException {
    public NoInternetConnectionException(String message) {
        super(message);
    }

    public NoInternetConnectionException() {
    }
}
