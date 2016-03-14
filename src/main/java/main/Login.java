package main;

import java.util.List;

/**
 * An object representing a login attempt
 */
public class Login {

    private final boolean successful;
    private final List<String> errorMessage;

    /**
     * @param successful    true iff <code>this</code> represents a successful login
     * @param errorMessages contains error messages iff <code>successful == false</code>,
     *                      otherwise is <code>null</code>
     */
    public Login(boolean successful, List<String> errorMessages) {

        if (successful && errorMessages != null) {
            throw new ExceptionInInitializerError("Expected 'errorMessages' to be null since 'successful' is true.");
        } else if (!successful && errorMessages == null) {
            throw new ExceptionInInitializerError("'errorMessages' cannot be null if 'successful' is false.");
        }

        this.successful = successful;
        this.errorMessage = errorMessages;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public List<String> getErrorMessage() {
        return errorMessage;
    }
}
