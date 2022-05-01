package com.intuit.task.manager.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class ProcessResponseData {

    private long pid;
    private String task;
    private PriorityType priority;
    private LocalDateTime created;

}