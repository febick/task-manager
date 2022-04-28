package com.intuit.task.manager.dto;

import lombok.*;
import java.time.LocalDateTime;

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
