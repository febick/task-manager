package com.intuit.task.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.task.manager.dto.*;
import static org.assertj.core.api.Assertions.*;
import com.intuit.task.manager.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class TaskManagerApplicationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TaskService service;

    @Test
    @Order(1)
    @DisplayName("Adding a new task (Naive method)")
    void addNewNaiveTask() throws Exception {
        List<CreateRequestData> list = IntStream.of(1, 2, 3, 4)
                .mapToObj(id -> new CreateRequestData("Task " + id, "NAIVE", "HIGH"))
                .toList();

        // The first three additions should pass without errors
        for (int i = 0; i < 3; i++) {
            CreateRequestData requestData = list.get(i);
            postAction("/tasks", requestData)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.pid").isNumber())
                    .andExpect(jsonPath("$.task").value(requestData.getTask()));
        }

        // The following addition should cause an error of exceeding the capacity
        postAction("/tasks", list.get(3)).andExpect(status().isBadRequest());
    }

    @Test
    @Order(2)
    @DisplayName("Adding a new task (FIFO method)")
    void addNewFifoTask() throws Exception {

        // The addition with the FIFO method should pass without errors
        CreateRequestData requestData = new CreateRequestData("FIFO Task", "FIFO", "LOW");
        postAction("/tasks", requestData)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pid").isNumber())
                .andExpect(jsonPath("$.task").value(requestData.getTask()));
    }

    @Test
    @Order(3)
    @DisplayName("Adding a new task (Priority method)")
    void addNewPriorityTask() throws Exception {

        // The first addition of a priority task should pass without errors
        CreateRequestData requestData = new CreateRequestData("Priority Task", "PRIORITY", "MEDIUM");
        postAction("/tasks", requestData)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pid").isNumber())
                .andExpect(jsonPath("$.task").value(requestData.getTask()));

        // Another addition should return an error, because there are no more low priority tasks in the list
        postAction("/tasks", requestData).andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("Getting one task")
    void getOneTask() throws Exception {
        ProcessResponseData testingCase = listOfAllTasks().get(0);

        mockMvc.perform(get("/tasks/" + testingCase.getPid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pid").value(testingCase.getPid()))
                .andExpect(jsonPath("$.task").value(testingCase.getTask()));
    }

    @Test
    @Order(5)
    @DisplayName("Sorting by date (default)")
    void sortByDate() throws Exception {
        String responseJSONByDefault = getAction("/tasks/");
        String responseJSONByDate = getAction("/tasks/sortedBy/date");

        // The creation date of each object must be less than that of the next
        ProcessResponseData[] tasks = mapJsonToObjects(responseJSONByDate);
        assertThat(tasks[0].getCreated()).isBefore(tasks[1].getCreated());
        assertThat(tasks[1].getCreated()).isBefore(tasks[2].getCreated());

        // The default query should return the same result as by date
        assertThat(responseJSONByDefault).isEqualTo(responseJSONByDate);
    }

    @Test
    @Order(6)
    @DisplayName("Sorting by ID")
    void sortByPid() throws Exception {
        String responseJSON = getAction("/tasks/sortedBy/id");

        // The ID of each object must be less than that of the next
        ProcessResponseData[] tasks = mapJsonToObjects(responseJSON);
        assertThat(tasks[0].getCreated()).isBefore(tasks[1].getCreated());
        assertThat(tasks[1].getCreated()).isBefore(tasks[2].getCreated());
    }

    @Test
    @Order(7)
    @DisplayName("Deleting all the task")
    void removeAll() throws Exception {
        String responseJSON = deleteAction("/tasks/remove/all");
        ProcessResponseData[] tasks = objectMapper.readValue(responseJSON, ProcessResponseData[].class);

        // Three objects should be deleted, the database should be empty
        assertThat(tasks.length).isEqualTo(3);
        assertThat(listOfAllTasks()).isEmpty();
    }

    @Test
    @Order(8)
    @DisplayName("Sorting by priority")
    void sortByPriority() throws Exception {
        service.addProcess("HIGH", CreatingType.NAIVE, PriorityType.HIGH);
        service.addProcess("LOW", CreatingType.NAIVE, PriorityType.LOW);
        service.addProcess("MEDIUM", CreatingType.NAIVE, PriorityType.MEDIUM);

        String responseJSON = getAction("/tasks/sortedBy/priority");

        // Tasks should be placed from the lowest priority to the highest
        ProcessResponseData[] tasks = mapJsonToObjects(responseJSON);
        assertThat(tasks[0].getPriority()).isEqualTo(PriorityType.LOW);
        assertThat(tasks[1].getPriority()).isEqualTo(PriorityType.MEDIUM);
        assertThat(tasks[2].getPriority()).isEqualTo(PriorityType.HIGH);
    }

    @Test
    @Order(9)
    @DisplayName("Deleting one task")
    void removeOneTask() throws Exception {
        ProcessResponseData testingCase = listOfAllTasks().get(0);

        String responseJSON = deleteAction("/tasks/remove/" + testingCase.getPid());
        ProcessResponseData[] tasks = mapJsonToObjects(responseJSON);

        // The request should return the deleted object, two records should remain in the database
        assertThat(tasks[0].getPid()).isEqualTo(testingCase.getPid());
        assertThat(listOfAllTasks().size()).isEqualTo(2);
    }

    @Test
    @Order(10)
    @DisplayName("Deleting list of tasks")
    void removeListOfTasks() throws Exception {
        long[] ids = listOfAllTasks().stream().mapToLong(ProcessResponseData::getPid).toArray();
        RemoveRequestData requestData = new RemoveRequestData(ids);

        String responseJSON = mockMvc.perform(delete("/tasks/remove/")
                    .content(objectMapper.writeValueAsString(requestData))
                    .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();
        ProcessResponseData[] removedTasks = mapJsonToObjects(responseJSON);

        // The numbers of deleted tasks must match the parameters, the database must be empty
        assertThat(removedTasks[0].getPid()).isEqualTo(ids[0]);
        assertThat(removedTasks[1].getPid()).isEqualTo(ids[1]);
        assertThat(listOfAllTasks().size()).isEqualTo(0);
    }

    @Test
    @Order(11)
    @DisplayName("Changing the capacity")
    void changeCapacity() {
        try {
            // Getting access to a private method
            Method changeMethod = TaskServiceImpl.class.getDeclaredMethod("setMaxCapacity", int.class);
            changeMethod.setAccessible(true);

            // The increase in the maximum capacity should pass without error
            changeMethod.invoke(service, 5);

            // Should return an error when trying to enter a value less than the current one
            assertThatThrownBy(() -> changeMethod.invoke(service, 2))
                    .isInstanceOf(InvocationTargetException.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* HANDLING EXCEPTIONAL SITUATIONS */

    @Test
    @DisplayName("Adding invalid task")
    void addingInvalidTask() throws Exception {
        CreateRequestData requestData = new CreateRequestData("", "WRONG_TYPE", "WRONG_TYPE");
        postAction("/tasks", requestData).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Getting a non-existent task")
    void getNonExistentTask() throws Exception {
        mockMvc.perform(get("/tasks/99999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/tasks/qwerty")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Sorting by non-existent parameter")
    void sortByNonExistentParameter() throws Exception {
        mockMvc.perform(get("/tasks/sortedBy/something")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deleting a non-existent task")
    void removeNonExistentTask() throws Exception {

        // All requests should return errors

        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/remove/99999"))
                .andExpect(status().isNotFound());

        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/remove/qwerty"))
                .andExpect(status().isBadRequest());

        long[] list = {99999, 99998};
        RemoveRequestData requestData = new RemoveRequestData(list);
        mockMvc.perform(delete("/tasks/remove/")
                        .content(objectMapper.writeValueAsString(requestData))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /* HELPER METHODS */

    private ResultActions postAction(String URL, CreateRequestData requestData) throws Exception {
         return mockMvc.perform(post(URL)
                        .content(objectMapper.writeValueAsString(requestData))
                        .contentType(MediaType.APPLICATION_JSON));
    }

    private String getAction(String URL) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String deleteAction(String URL) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.delete(URL))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private ProcessResponseData[] mapJsonToObjects(String JSON) throws JsonProcessingException {
        return objectMapper.readValue(JSON, ProcessResponseData[].class);
    }

    private List<ProcessResponseData> listOfAllTasks() {
        return service.getAllProcesses(SortingType.DATE);
    }

}
