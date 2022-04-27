package com.intuit.task.manager.entities;

import com.intuit.task.manager.dto.PriorityType;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "processes")
@NoArgsConstructor
@Getter
public class Process {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long pid;
    @Enumerated(EnumType.STRING)
    private PriorityType priority;
    private LocalDateTime created;

    public Process(PriorityType priority) {
        this.priority = priority;
        this.created = LocalDateTime.now();
    }

}
