package com.rest.webservices.flights.exception;

public class InvalidInputException extends Exception {

    /**
     * Creates a new exception with no detail message.
     */
    public InvalidInputException() {
        super();
    }

    /**
     * Creates a new exception with the given detail message.
     *
     * @param message the detail message
     */
    public InvalidInputException(String message) {
        super(message);
    }
}
