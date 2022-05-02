package com.intuit.task.manager.dto;

import lombok.*;
import javax.validation.constraints.NotEmpty;

/**
 * The DTO is used to send a request to delete a list of objects
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoveRequestData {

    @NotEmpty
    private long[] list;

}
