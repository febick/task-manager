package com.intuit.task.manager.entities;

import com.intuit.task.manager.dto.PriorityType;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * An entity for collect in a database
 */
@Entity
@Table(name = "processes")
@NoArgsConstructor
@Getter
public class Process {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long pid;

    private int priority;
    private LocalDateTime created;
    private String task;

    public Process(String task, PriorityType priority) {
        this.task = task;
        this.priority = priority.ordinal();
        this.created = LocalDateTime.now();
    }

}
