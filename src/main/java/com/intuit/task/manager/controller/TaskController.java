package com.intuit.task.manager.controller;

import com.intuit.task.manager.dto.*;
import com.intuit.task.manager.service.TaskService;
import com.intuit.task.manager.validation.ValueInEnum;
import lombok.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@Validated
public class TaskController {

    private TaskService service;

    @PostMapping("/tasks")
    public ProcessResponseData addTask(@RequestBody @Valid CreateRequestData data) {
        return service.addProcess(data.getTask(),
                CreatingType.valueOf(data.getType().toUpperCase()),
                PriorityType.valueOf(data.getPriority().toUpperCase()));
    }

    @GetMapping("/tasks/sortedBy/{sort-type}")
    public List<ProcessResponseData> getAllTasksSortedByParam(@PathVariable(name = "sort-type") @ValueInEnum(enumType = SortingType.class) String sortType) {
        return service.getAllProcesses(SortingType.valueOf(sortType.toUpperCase()));
    }

    @GetMapping("/tasks")
    public List<ProcessResponseData> getAllTasksSortedByDefault() {
        return service.getAllProcesses(SortingType.DATE);
    }

    @GetMapping("/tasks/{id}")
    public ProcessResponseData getTaskByPid(@PathVariable long id) {
        return service.getProcess(id);
    }

    @DeleteMapping("/tasks/remove/{id}")
    public List<ProcessResponseData> removeTaskById(@PathVariable long id) {
        return service.killProcess(id);
    }

    @DeleteMapping("/tasks/remove/")
    public List<ProcessResponseData> removeListOfTasks(@RequestBody @Valid RemoveRequestData data) {
        return service.killProcess(data.getList());
    }

    @DeleteMapping("/tasks/remove/all")
    public List<ProcessResponseData> removeAllTasks() {
        return service.killAllProcesses();
    }


}
