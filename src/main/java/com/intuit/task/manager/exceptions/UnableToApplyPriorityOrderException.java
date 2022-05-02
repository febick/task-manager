package com.intuit.task.manager.exceptions;

/**
 * Thrown out if it is not possible to add a new process because capacity exceeded
 * and process with lower priority to remove is missing
 */
public class UnableToApplyPriorityOrderException extends RuntimeException {
    public UnableToApplyPriorityOrderException(String message) {
        super(message);
    }
}
