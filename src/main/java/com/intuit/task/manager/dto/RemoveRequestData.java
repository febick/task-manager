package com.intuit.task.manager.dto;

import lombok.*;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoveRequestData {

    @NotEmpty
    private long[] list;

}
