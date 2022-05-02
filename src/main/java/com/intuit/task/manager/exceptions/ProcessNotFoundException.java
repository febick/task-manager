package com.intuit.task.manager.exceptions;

/**
 * Thrown if an attempt is made to get a non-existent process.
 */
public class ProcessNotFoundException extends RuntimeException {
    public ProcessNotFoundException(String message) {
        super(message);
    }
}
