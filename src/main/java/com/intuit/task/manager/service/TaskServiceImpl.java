package com.intuit.task.manager.service;

import com.intuit.task.manager.dto.*;
import com.intuit.task.manager.entities.Process;
import com.intuit.task.manager.exceptions.MaximumCapacityExceededException;
import com.intuit.task.manager.exceptions.ProcessNotFoundException;
import com.intuit.task.manager.exceptions.UnableToApplyPriorityOrderException;
import com.intuit.task.manager.repo.ProcessRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@ManagedResource
public class TaskServiceImpl implements TaskService {

    private final ProcessRepository repository;

    public TaskServiceImpl(ProcessRepository repository) {
        this.repository = repository;
    }

    @Value("${app.task.manager.capacity.max:25}")
    private int maxCapacity;

    @Override
    @Transactional
    public ProcessResponseData addProcess(String task, CreatingType creatingType, PriorityType priority) {
        Process process = new Process(task, priority);
        try {
            Method method = this.getClass().getDeclaredMethod("addBy_" + creatingType.name().toLowerCase(), Process.class);
            return (ProcessResponseData) method.invoke(this, process);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ProcessResponseData addBy_naive(Process process) {
        if (checkCapacity()) return saveAndReturn(process);
        String excMessage = String.format("The task manager has already accepted the maximum number of tasks: %d", maxCapacity);
        log.error(excMessage);
        throw new MaximumCapacityExceededException(excMessage);
    }

    private ProcessResponseData addBy_fifo(Process process) {
        repository.removeTheOldestProcess();
        return saveAndReturn(process);
    }

    private ProcessResponseData addBy_priority(Process process) {
        if (!checkCapacity()) {
            Long oldestTaskId = repository.getTheOldestTaskIdWithLessPriorityThanCurrent(process.getPriority());
            if (oldestTaskId != null) {
                repository.deleteById(oldestTaskId);
            } else {
                String excMessage = String.format("The task manager has already accepted the maximum number of tasks (%d) " +
                        "and none of them has a lower priority than the current one.", maxCapacity);
                log.error(excMessage);
                throw new UnableToApplyPriorityOrderException(excMessage);
            }
        }
        return saveAndReturn(process);
    }

    private ProcessResponseData saveAndReturn(Process process) {
        repository.save(process);
        log.debug("Task with title \"{}\" and PID {} was created at {}", process.getTask(), process.getPid(), process.getCreated());
        return entityToDto(process);
    }

    @Override
    public List<ProcessResponseData> getAllProcesses(SortingType sort) {
        List<Process> result = switch (sort) {
            case ID -> repository.getAllByOrderByPid();
            case DATE -> repository.getAllByOrderByCreated();
            case PRIORITY -> repository.getAllByOrderByPriority();
        };
        log.debug("Returned a list of all processes ({}). Sorted by {}.", result.size(), sort.name());
        return result.stream().map(this::entityToDto).toList();
    }

    @Override
    public ProcessResponseData getProcess(long id) {
        Process process = repository.getByPid(id);
        if (process == null) {
            String excMessage = String.format("Process with id %d wasn't found", id);
            log.error(excMessage);
            throw new ProcessNotFoundException(excMessage);
        }
        log.debug("Returned a process with id {}", id);
        return entityToDto(process);
    }

    @Override
    public List<ProcessResponseData> killAllProcesses() {
        List<ProcessResponseData> tasksToRemove = getAllProcesses(SortingType.DATE);
        repository.deleteAll();
        log.debug("Deleted all ({}) processes", tasksToRemove.size());
        return tasksToRemove;
    }

    @Override
    public List<ProcessResponseData> killProcess(long... list) {
        List<ProcessResponseData> result = new ArrayList<>();
        List<Process> remove = new ArrayList<>();

        for (long pid : list ) {
            Process task = repository.getById(pid);
            remove.add(task);
            result.add(entityToDto(task));
            log.trace("A process with ID {} has been marked for deletion", task.getPid());
        }
        repository.deleteAllInBatch(remove);
        log.debug("Processes deleted: {}", remove.size());
        return result;
    }

    private ProcessResponseData entityToDto(Process data) {
        log.trace("Process with id {} was converted to DTO for Response", data.getPid());
        return ProcessResponseData.builder()
                .pid(data.getPid())
                .task(data.getTask())
                .created(data.getCreated())
                .priority(PriorityType.values()[data.getPriority()])
                .build();
    }

    private boolean checkCapacity() {
        return repository.count() < maxCapacity;
    }

    @ManagedOperation
    public int getMaxCapacity() {
        return maxCapacity;
    }

    @ManagedOperation
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

}
