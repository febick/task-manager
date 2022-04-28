package com.intuit.task.manager.service;

import com.intuit.task.manager.dto.*;

import java.util.List;

public interface TaskService {

    ProcessResponseData addProcess(String task, CreatingType type, PriorityType priority);
    List<ProcessResponseData> getAllProcesses(SortingType sort);
    ProcessResponseData getProcess(long id);
    List<ProcessResponseData> killAllProcesses();
    List<ProcessResponseData> killProcess(long... list);

}
