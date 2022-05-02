package com.intuit.task.manager.dto;

import com.intuit.task.manager.validation.ValueInEnum;
import lombok.*;
import javax.validation.constraints.*;

/**
 * The DTO is used to send a request to create a new process
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRequestData {

    @NotEmpty
    private String task;

    @ValueInEnum(enumType = CreatingType.class)
    private String type;

    @ValueInEnum(enumType = PriorityType.class)
    private String priority;

}
