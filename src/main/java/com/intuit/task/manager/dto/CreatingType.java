package com.intuit.task.manager.dto;

/**
 * Describes possible ways to add a new process.
 * Each value corresponds to a method in the service implementation.
 */
public enum CreatingType {
    NAIVE, FIFO, PRIORITY
}
