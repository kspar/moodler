package exceptions;

/**
 * Created by Kaspar on 24/12/2015.
 */
public class NoCredentialsProvidedException extends RuntimeException {

    public NoCredentialsProvidedException(String message) {
        super(message);
    }
}
