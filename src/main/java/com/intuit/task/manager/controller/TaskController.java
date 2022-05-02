package com.intuit.task.manager.controller;

import com.intuit.task.manager.dto.*;
import com.intuit.task.manager.service.TaskService;
import com.intuit.task.manager.validation.ValueInEnum;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * The Controller for working with TaskService
 * @see TaskService
 */
@RestController
@AllArgsConstructor
@Validated
public class TaskController {

    private TaskService service;

    /**
     * Adds a new process to the database
     *
     * @param data is the CreateRequestData-DTO with information about new task
     * @see CreateRequestData
     * @return the ProcessResponseData with Created status if adding was successful or an error message
     * @see ProcessResponseData
     */
    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public ProcessResponseData addTask(@RequestBody @Valid CreateRequestData data) {
        return service.addProcess(data.getTask(),
                CreatingType.valueOf(data.getType().toUpperCase()),
                PriorityType.valueOf(data.getPriority().toUpperCase()));
    }

    /**
     * Getting a sorted list of all processes
     *
     * @param sortType is the value by which the list should be sorted
     * @return the list of all tasks sorted by specified value or an error is the value isn't valid
     */
    @GetMapping("/tasks/sortedBy/{sort-type}")
    public List<ProcessResponseData> getAllTasksSortedByParam(
            @PathVariable(name = "sort-type")
            @ValueInEnum(enumType = SortingType.class)
            String sortType) {
        return service.getAllProcesses(SortingType.valueOf(sortType.toUpperCase()));
    }

    /**
     * Getting a default sorted list of all processes
     *
     * @return the list of all tasks sorted by default (by DATE)
     */
    @GetMapping("/tasks")
    public List<ProcessResponseData> getAllTasksSortedByDefault() {
        return service.getAllProcesses(SortingType.DATE);
    }

    /**
     * Getting one process
     *
     * @param id is the process id
     * @return the DTO with information about Process or an error if process with specified ID wasn't found
     */
    @GetMapping("/tasks/{id}")
    public ProcessResponseData getTaskByPid(@PathVariable long id) {
        return service.getProcess(id);
    }

    /**
     * Remove task by id
     *
     * @param id is the process id
     * @return the list consisting of a single removed object,
     * or an error if a process with that ID was not found
     */
    @DeleteMapping("/tasks/remove/{id}")
    public List<ProcessResponseData> removeTaskById(@PathVariable long id) {
        return service.killProcess(id);
    }

    /**
     * Remove list of tasks by IDs.
     *
     * @param data is the DTO with the list of IDs
     * @see RemoveRequestData
     * @return the list consisting of removed objects
     * or an error if at least one of process with specified ID wasn't found
     */
    @DeleteMapping("/tasks/remove/")
    public List<ProcessResponseData> removeListOfTasks(@RequestBody @Valid RemoveRequestData data) {
        return service.killProcess(data.getList());
    }

    /**
     * Remove all tasks
     *
     * @return the list of all removed processes
     */
    @DeleteMapping("/tasks/remove/all")
    public List<ProcessResponseData> removeAllTasks() {
        return service.killAllProcesses();
    }


}
