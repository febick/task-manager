package com.intuit.task.manager.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;

@Data
public class RemoveRequestData {

    @NotEmpty
    private long[] list;

}
