package com.intuit.task.manager.repo;

import com.intuit.task.manager.entities.Process;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessRepository extends JpaRepository<Process, Long> {
}