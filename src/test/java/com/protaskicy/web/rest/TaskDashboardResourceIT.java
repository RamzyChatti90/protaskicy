package com.protaskicy.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.protaskicy.IntegrationTest;
import com.protaskicy.domain.Task;
import com.protaskicy.domain.User;
import com.protaskicy.domain.enumeration.TaskStatus;
import com.protaskicy.repository.TaskRepository;
import com.protaskicy.repository.UserRepository;
import com.protaskicy.security.AuthoritiesConstants;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TaskDashboardResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", authorities = { AuthoritiesConstants.USER })
class TaskDashboardResourceIT {

    private static final String DEFAULT_LOGIN = "testuser";
    private static final String OTHER_USER_LOGIN = "otheruser";

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTaskDashboardMockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User currentUser;
    private User otherUser;

    @BeforeEach
    public void initTest() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        currentUser = new User();
        currentUser.setLogin(DEFAULT_LOGIN);
        currentUser.setPassword("$2a$10$VEjxo0jq2YG9Rbk2HmX9S.DQpQfQJ0PqXgWDq.g4faH5B0D.g4faH5B0D"); // user
        currentUser.setEmail(DEFAULT_LOGIN + "@localhost");
        currentUser.setActivated(true);
        currentUser.setFirstName("Test");
        currentUser.setLastName("User");
        currentUser.setLangKey("en");
        userRepository.save(currentUser);

        otherUser = new User();
        otherUser.setLogin(OTHER_USER_LOGIN);
        otherUser.setPassword("$2a$10$VEjxo0jq2YG9Rbk2HmX9S.DQpQfQJ0PqXgWDq.g4faH5B0D.g4faH5B0D"); // user
        otherUser.setEmail(OTHER_USER_LOGIN + "@localhost");
        otherUser.setActivated(true);
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        otherUser.setLangKey("en");
        userRepository.save(otherUser);
    }

    private Task createTask(String name, TaskStatus status, User assignedTo, Instant createdAt) {
        Task task = new Task();
        task.setName(name);
        task.setStatus(status);
        task.setAssignedTo(assignedTo);
        task.setCreatedAt(createdAt);
        return taskRepository.save(task);
    }

    @Test
    @Transactional
    void getTaskStats() throws Exception {
        // Initialize the database
        createTask("Task 1", TaskStatus.TODO, currentUser, Instant.now());
        createTask("Task 2", TaskStatus.IN_PROGRESS, currentUser, Instant.now());
        createTask("Task 3", TaskStatus.DONE, currentUser, Instant.now());
        createTask("Task 4", TaskStatus.DONE, currentUser, Instant.now());
        createTask("Other User Task", TaskStatus.TODO, otherUser, Instant.now());

        // Get the task stats
        restTaskDashboardMockMvc
            .perform(get("/api/dashboard/stats").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.totalTasks").value(4L))
            .andExpect(jsonPath("$.todoTasks").value(1L))
            .andExpect(jsonPath("$.inProgressTasks").value(1L))
            .andExpect(jsonPath("$.doneTasks").value(2L));
    }

    @Test
    @Transactional
    void getTaskStatusDistribution() throws Exception {
        // Initialize the database
        createTask("Task 1", TaskStatus.TODO, currentUser, Instant.now());
        createTask("Task 2", TaskStatus.IN_PROGRESS, currentUser, Instant.now());
        createTask("Task 3", TaskStatus.DONE, currentUser, Instant.now());
        createTask("Task 4", TaskStatus.DONE, currentUser, Instant.now());
        createTask("Other User Task", TaskStatus.TODO, otherUser, Instant.now());

        // Get the task status distribution
        restTaskDashboardMockMvc
            .perform(get("/api/dashboard/status-distribution").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].status").value(hasItems(TaskStatus.TODO.name(), TaskStatus.IN_PROGRESS.name(), TaskStatus.DONE.name())))
            .andExpect(jsonPath("$.[?(@.status == 'TODO')].count").value(hasItem(1)))
            .andExpect(jsonPath("$.[?(@.status == 'IN_PROGRESS')].count").value(hasItem(1)))
            .andExpect(jsonPath("$.[?(@.status == 'DONE')].count").value(hasItem(2)));
    }

    @Test
    @Transactional
    void getTaskCompletionEvolution() throws Exception {
        // Initialize the database
        Instant now = Instant.now();
        createTask("Task 1", TaskStatus.DONE, currentUser, now.minus(3, ChronoUnit.WEEKS));
        createTask("Task 2", TaskStatus.DONE, currentUser, now.minus(3, ChronoUnit.WEEKS));
        createTask("Task 3", TaskStatus.DONE, currentUser, now.minus(2, ChronoUnit.WEEKS));
        createTask("Task 4", TaskStatus.DONE, currentUser, now.minus(2, ChronoUnit.WEEKS));
        createTask("Task 5", TaskStatus.DONE, currentUser, now.minus(1, ChronoUnit.WEEKS));
        createTask("Other User Task", TaskStatus.DONE, otherUser, now.minus(1, ChronoUnit.WEEKS));

        // Get the task completion evolution
        String responseContent = restTaskDashboardMockMvc
            .perform(get("/api/dashboard/completion-evolution").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<TaskCompletionEvolutionDTO> evolution = objectMapper.readValue(responseContent, new com.fasterxml.jackson.core.type.TypeReference<List<TaskCompletionEvolutionDTO>>() {});

        assertThat(evolution).hasSizeGreaterThanOrEqualTo(3);

        // Assert that counts are correct for recent weeks (exact dates depend on 'now')
        // This test is a bit fragile due to DATE_TRUNC('week') returning the start of the week
        // We'll check for the counts without being overly strict on the exact dates
        assertThat(evolution.stream().mapToLong(TaskCompletionEvolutionDTO::getCompletedTasksCount).sum()).isEqualTo(5L);
    }
}
