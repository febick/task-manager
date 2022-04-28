package com.intuit.task.manager.exceptions;

public class MaximumCapacityExceededException extends RuntimeException {
    public MaximumCapacityExceededException(String message) {
        super(message);
    }
}
