package com.intuit.task.manager.repo;

import com.intuit.task.manager.entities.Process;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * The interface Process repository.
 */
public interface ProcessRepository extends JpaRepository<Process, Long> {

    /**
     * Deletes the oldest record in the database
     */
    @Modifying
    @Query(value = "DELETE FROM processes WHERE pid = (SELECT pid FROM processes ORDER BY created LIMIT 1)", nativeQuery = true)
    void removeTheOldestProcess();

    /**
     * Getting the ID of the oldest record whose priority is lower than the requested one
     *
     * @param currentLevel the current Priority level
     * @see com.intuit.task.manager.dto.PriorityType
     * @return ID of the oldest task with less priority than current
     */
    @Query(value = "SELECT pid FROM processes WHERE priority < :level ORDER BY created LIMIT 1", nativeQuery = true)
    Long getTheOldestTaskIdWithLessPriorityThanCurrent(@Param("level") int currentLevel);

    /**
     * Getting the ID of all processes with a given priority
     *
     * @param currentLevel the current Priority level
     * @return list of all selected values
     */
    @Query(value = "SELECT pid FROM processes WHERE priority = :level", nativeQuery = true)
    long[] getAllIdsByPriority(@Param("level") int currentLevel);
    /**
     * Getting a list of all records (sorted by date)
     *
     * @return sorted list of all values
     */
    List<Process> getAllByOrderByCreated();

    /**
     * Getting a list of all records (sorted by priority)
     *
     * @return sorted list of all values
     */
    List<Process> getAllByOrderByPriority();

    /**
     * Getting a list of all records (sorted by pid)
     *
     * @return sorted list of all values
     */
    List<Process> getAllByOrderByPid();

    /**
     * Getting an entry by ID
     *
     * @param id is the value for the search
     * @return entry with specified ID
     */
    Process getByPid(Long id);


}