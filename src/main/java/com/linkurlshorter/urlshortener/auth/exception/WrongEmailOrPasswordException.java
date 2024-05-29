package com.linkurlshorter.urlshortener.auth.exception;

public class WrongEmailOrPasswordException extends RuntimeException {
    private static final String MESSAGE = "Wrong email or password";
    public WrongEmailOrPasswordException() {
        super(MESSAGE);
    }
}
