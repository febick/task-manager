package com.intuit.task.manager.dto;

import com.intuit.task.manager.validation.ValueInEnum;
import lombok.Data;
import javax.validation.constraints.*;

@Data
public class CreateRequestData {

    @NotEmpty
    private String task;

    @ValueInEnum(enumType = CreatingType.class)
    private String type;

    @ValueInEnum(enumType = PriorityType.class)
    private String priority;

}
