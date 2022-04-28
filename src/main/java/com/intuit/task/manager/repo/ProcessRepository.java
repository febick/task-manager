package com.intuit.task.manager.repo;

import com.intuit.task.manager.entities.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProcessRepository extends JpaRepository<Process, Long> {


    @Modifying
    @Query(value = "DELETE FROM processes WHERE pid = (SELECT pid FROM processes ORDER BY created LIMIT 1)", nativeQuery = true)
    void removeTheOldestProcess();

    @Query(value = "SELECT pid FROM processes WHERE priority < :level ORDER BY created LIMIT 1", nativeQuery = true)
    Long getTheOldestTaskIdWithLessPriorityThanCurrent(@Param("level") int currentLevel);

    List<Process> getAllByOrderByCreated();

    List<Process> getAllByOrderByPriority();

    List<Process> getAllByOrderByPid();

    Process getByPid(Long id);


}