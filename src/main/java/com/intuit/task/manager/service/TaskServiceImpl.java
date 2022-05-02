package com.intuit.task.manager.service;

import com.intuit.task.manager.dto.*;
import com.intuit.task.manager.entities.Process;
import com.intuit.task.manager.exceptions.*;
import com.intuit.task.manager.repo.ProcessRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.lang.reflect.*;
import java.util.*;

/**
 * The implementation of TaskService
 * @see TaskService
 *
 * Allows to execute the methods described by the interface.
 */
@Service
@Slf4j
@ManagedResource
public class TaskServiceImpl implements TaskService {

    private final ProcessRepository repository;

    /**
     * Instantiates a new TaskServiceImpl.
     * During initialization, it compares the current number of saved processes and
     * the maximum allowed and resets the database if it is exceeded.
     *
     * @param repository the implementation of ProcessRepository
     * @see ProcessRepository
     */
    public TaskServiceImpl(ProcessRepository repository) {
        this.repository = repository;
        afterInitCheck();
    }

    /**
     *  A parameter that limits the maximum possible number of processes.
     *  It is a @ManagedResource and can be changed on the fly.
     *  @see TaskServiceImpl#setMaxCapacity
     */
    @Value("${app.task.manager.capacity.max:25}")
    private int maxCapacity;

    /**
     * Create a new process
     * Calls the required save method based on the passed CreatingType parameter.
     *
     * @param task The process title.
     * @param creatingType The process creation type.
     * @see CreatingType
     * @param priority The process priority type.
     * @see PriorityType
     * @return Process-DTO of the saving object
     * @see ProcessResponseData
     */
    @Override
    @Transactional
    public ProcessResponseData addProcess(String task, CreatingType creatingType, PriorityType priority) {
        Process process = new Process(task, priority);
        try {
            Method method = this.getClass().getDeclaredMethod("addBy_" + creatingType.name().toLowerCase(), Process.class);
            return (ProcessResponseData) method.invoke(this, process);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    /**
     * Adding a process with type NAIVE
     * @see CreatingType
     * Allows new processes to be added as long as the capacity is not exceeded.
     *
     * @throws MaximumCapacityExceededException if the capacity does not allow adding the next process.
     * @param process is the entity to store in the database
     * @see Process
     * @return Process-DTO of the saving object
     * @see ProcessResponseData
     */
    private ProcessResponseData addBy_naive(Process process) {
        if (checkCapacity()) return saveAndReturn(process);
        String excMessage = String.format("The task manager has already accepted the maximum number of tasks: %d", maxCapacity);
        throw new MaximumCapacityExceededException(excMessage);
    }

    /**
     * Adding a process with type FIFO
     * @see CreatingType
     * If the capacity is exceeded — before adding a new object, deletes the oldest of the previously created
     *
     * @param process is the entity to store in the database
     * @see Process
     * @return Process-DTO of the saving object
     * @see ProcessResponseData
     */
    private ProcessResponseData addBy_fifo(Process process) {
        if (!checkCapacity()) repository.removeTheOldestProcess();
        return saveAndReturn(process);
    }

    /**
     * Adding a process with type PRIORITY
     * @see CreatingType
     * If the capacity is exceeded — before adding a new object, deletes the oldest of the previously created
     * objects, whose priority is less than the priority of the new one.
     *
     * @throws UnableToApplyPriorityOrderException if there is no process with a lower priority.
     * @param process is the entity to store in the database
     * @see Process
     * @return Process-DTO of the saving object
     * @see ProcessResponseData
     */
    private ProcessResponseData addBy_priority(Process process) {
        if (!checkCapacity()) {
            Long oldestTaskId = repository.getTheOldestTaskIdWithLessPriorityThanCurrent(process.getPriority());
            if (oldestTaskId != null) {
                repository.deleteById(oldestTaskId);
            } else {
                String excMessage = String.format("The task manager has already accepted the maximum number of tasks (%d) " +
                        "and none of them has a lower priority than the current one.", maxCapacity);
                throw new UnableToApplyPriorityOrderException(excMessage);
            }
        }
        return saveAndReturn(process);
    }

    /**
     * Saves an object to the database
     *
     * @param process is the entity to store in the database
     * @see Process
     * @return Process-DTO of the saving object
     * @see ProcessResponseData
     */
    private ProcessResponseData saveAndReturn(Process process) {
        repository.save(process);
        log.debug("Task with title \"{}\" and PID {} was created at {}", process.getTask(), process.getPid(), process.getCreated());
        return entityToDto(process);
    }

    /**
     * Getting a list of all processes
     *
     * @param sort indicates the sorting type of the list.
     * @see SortingType
     * @return a sorted list of all processes
     * @see ProcessResponseData
     */
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

    /**
     * Getting a process by its number
     *
     * @throws ProcessNotFoundException if the process with the specified number was not found
     * @param id is the unique process id
     * @return Process-DTO of the saving object
     * @see ProcessResponseData
     */
    @Override
    public ProcessResponseData getProcess(long id) {
        Process process = repository.getByPid(id);
        if (process == null) throw new ProcessNotFoundException(String.format("Process with id %d wasn't found", id));
        log.debug("Returned a process with id {}", id);
        return entityToDto(process);
    }

    /**
     * Removing all objects from the database
     *
     * @return a list of all deleted objects
     */
    @Override
    @Transactional
    public List<ProcessResponseData> killAllProcesses() {
        List<ProcessResponseData> tasksToRemove = getAllProcesses(SortingType.DATE);
        repository.deleteAll();
        log.debug("Deleted all ({}) processes", tasksToRemove.size());
        return tasksToRemove;
    }

    /**
     * Deleting a single process or a list of processes
     *
     * @throws ProcessNotFoundException if at least one of the process wasn't found
     * @param list process ID or list of IDs
     * @return a list of all deleted objects
     */
    @Override
    @Transactional
    public List<ProcessResponseData> killProcess(long... list) {
        List<ProcessResponseData> result = new ArrayList<>();
        List<Process> remove = new ArrayList<>();

        for (long pid : list) {
            Optional<Process> optionalTask = repository.findById(pid);
            if (optionalTask.isEmpty()) throw new ProcessNotFoundException(String.format("Process with id %d wasn't found", pid));
            Process task = optionalTask.get();
            remove.add(task);
            result.add(entityToDto(task));
            log.trace("A process with ID {} has been marked for deletion", task.getPid());
        }
        repository.deleteAllInBatch(remove);

        log.debug("Processes deleted: {}", remove.size());
        return result;
    }

    /**
     * Converts an entity to an DTO
     *
     * @param data is a Process entity
     * @return a Process-DTO @see ProcessResponseData
     */
    private ProcessResponseData entityToDto(Process data) {
        log.trace("Process with id {} was converted to DTO for Response", data.getPid());
        return ProcessResponseData.builder()
                .pid(data.getPid())
                .task(data.getTask())
                .created(data.getCreated())
                .priority(PriorityType.values()[data.getPriority()])
                .build();
    }

    /**
     * Check if the maximum capacity is exceeded
     *
     * @return current capacity status
     */
    private boolean checkCapacity() {
        return repository.count() < getMaxCapacity();
    }

    /**
     * Getting the current capacity size
     * Is a @ManagedResource and can be retrieved on the fly
     *
     * @return the current capacity size
     */
    @ManagedOperation
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Sets a new capacity value
     * It is a @ManagedResource and can be changed on the fly.
     *
     * @throws IllegalArgumentException if the new value is less than the current one
     * @param maxCapacity is a new capacity value
     */
    @ManagedOperation
    public void setMaxCapacity(int maxCapacity) {
        if (this.maxCapacity < maxCapacity) {
            this.maxCapacity = maxCapacity;
            log.info("The capacity has been changed. The new value is {}, the previous value is {}.", maxCapacity, this.maxCapacity);
        } else {
            String excMessage = String.format("The new capacity (%d) cannot be less than the current one (%d)", maxCapacity, this.maxCapacity);
            log.error(excMessage);
            throw new IllegalArgumentException(excMessage);
        }
    }

    /**
     * Capacity check after initialization
     *
     * In case of launching an application with a set capacity parameter, whose level is lower than
     * the current number of processes, the deletion method is called.
     */
    private void afterInitCheck() {
        if (!checkCapacity()) {
            List<ProcessResponseData> removed = killAllProcesses();
            log.info("The number of processes saved before restarting the application {} exceeds the " +
                    "current maximum allowed setting {}. All previously created processes have been removed.", removed.size(), getMaxCapacity());
        }
    }

}
