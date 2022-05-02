package com.intuit.task.manager.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * The DTO is used to return an error response in a more readable format.
 */
@NoArgsConstructor
public class ErrorResponseData {

    @Getter
    private String error;

    @Getter
    private LocalDateTime timestamp;

    public ErrorResponseData(String error) {
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }
}
