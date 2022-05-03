package com.intuit.task.manager.service;

import com.intuit.task.manager.dto.*;
import java.util.List;

/**
 * The interface is responsible for the basic methods needed to create, receive and delete processes.
 */
public interface TaskService {

    /**
     * Adding a new process.
     *
     * @param task The process title.
     * @param type The process creation type.
     * @see CreatingType
     * @param priority The process priority type.
     * @see PriorityType
     * @return Process-DTO with assigned identifier
     * @see ProcessResponseData
     */
    ProcessResponseData addProcess(String task, CreatingType type, PriorityType priority);

    /**
     * Gets all processes.
     *
     * @param sort indicates the sorting type of the list.
     * @see SortingType
     * @return sorted list of all processes
     * @see ProcessResponseData
     */
    List<ProcessResponseData> getAllProcesses(SortingType sort);

    /**
     * Get process.
     *
     * @param id is the unique process id
     * @return the process with specified ID
     * @see ProcessResponseData
     */
    ProcessResponseData getProcess(long id);

    /**
     * Kills all processes.
     *
     * @return the list of killed processes
     * @see ProcessResponseData
     */
    List<ProcessResponseData> killAllProcesses();

    /**
     * Kills a process or list of processes
     *
     * @param list process ID or list of IDs
     * @return the list of killed processes
     * @see ProcessResponseData
     */
    List<ProcessResponseData> killProcess(long... list);

    /**
     * Kills all processes with the specified priority
     *
     * @param type is PriorityType
     * @see PriorityType
     * @return the list of killed processes
     */
    List<ProcessResponseData> killProcessByPriority(PriorityType type);

}
