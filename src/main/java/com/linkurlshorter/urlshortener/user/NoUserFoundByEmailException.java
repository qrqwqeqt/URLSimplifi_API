package com.linkurlshorter.urlshortener.user;
/**
 * Exception indicating that no user was found based on the provided email.
 * This exception is typically thrown when attempting to retrieve a user
 * entity from the database using an email-based query method, and no matching
 * user is found.
 * <p>
 * This exception can provide a custom message to describe the specific
 * circumstance in which it was thrown.
 * </p>
 *
 * @version 1.0
 * @author Artem Poliakov
 */
public class NoUserFoundByEmailException extends RuntimeException {
    private static final String DEFAULT_MSG = "No user by provided email found";

    public NoUserFoundByEmailException() {
        super(DEFAULT_MSG);
    }

    public NoUserFoundByEmailException(String msg) {
        super(msg);
    }
}
