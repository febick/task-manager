package com.intuit.task.manager.exceptions;

/**
 * Thrown out if it is not possible to add a new process due to exceeding the maximum allowed capacity
 */
public class MaximumCapacityExceededException extends RuntimeException {
    public MaximumCapacityExceededException(String message) {
        super(message);
    }
}
