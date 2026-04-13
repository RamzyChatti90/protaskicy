package com.protaskicy.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.protaskicy.ProtaskicyApp;
import com.protaskicy.domain.Task;
import com.protaskicy.domain.User;
import com.protaskicy.domain.enumeration.TaskStatus;
import com.protaskicy.repository.TaskRepository;
import com.protaskicy.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TaskDashboardResource} REST controller.
 */
@SpringBootTest(classes = ProtaskicyApp.class)
@AutoConfigureMockMvc
@WithMockUser(username = "testuser", roles = { "USER" })
class TaskDashboardResourceIT {

    private static final String DEFAULT_LOGIN = "testuser";

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTaskDashboardMockMvc;

    private User user;

    @BeforeEach
    public void initTest() {
        user = userRepository.findOneByLogin(DEFAULT_LOGIN).orElseGet(() -> {
            User newUser = new User();
            newUser.setLogin(DEFAULT_LOGIN);
            newUser.setPassword("$2a$10$gSAhZrxMllrbgj/kkT9kDugMHseADwekGHd5zHUKXsmittelW4/Ej.oZZDQWGx.i"); // test
            newUser.setActivated(true);
            newUser.setEmail("testuser@localhost");
            newUser.setFirstName("testuser");
            newUser.setLastName("testuser");
            newUser.setLangKey("en");
            em.persist(newUser);
            em.flush();
            return newUser;
        });

        taskRepository.deleteAll();

        // Create some tasks for testing
        Task task1 = new Task();
        task1.setName("Task 1");
        task1.setStatus(TaskStatus.TODO);
        task1.setAssignedTo(user);
        task1.setCreatedAt(Instant.now().minus(2, ChronoUnit.DAYS));
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setName("Task 2");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.setAssignedTo(user);
        task2.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        taskRepository.save(task2);

        Task task3 = new Task();
        task3.setName("Task 3");
        task3.setStatus(TaskStatus.DONE);
        task3.setAssignedTo(user);
        task3.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        taskRepository.save(task3);

        Task task4 = new Task();
        task4.setName("Task 4");
        task4.setStatus(TaskStatus.DONE);
        task4.setAssignedTo(user);
        task4.setCreatedAt(Instant.now());
        taskRepository.save(task4);
    }

    @Test
    @Transactional
    void getTaskStats() throws Exception {
        restTaskDashboardMockMvc
            .perform(get("/api/dashboard/task-stats").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath(".totalTasks").value(4))
            .andExpect(jsonPath(".todoTasks").value(1))
            .andExpect(jsonPath(".inProgressTasks").value(1))
            .andExpect(jsonPath(".doneTasks").value(2));
    }

    @Test
    @Transactional
    void getTaskStatusDistribution() throws Exception {
        restTaskDashboardMockMvc
            .perform(get("/api/dashboard/task-status-distribution").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[?(@.statusName == 'TODO')].count").value(1))
            .andExpect(jsonPath("$[?(@.statusName == 'IN_PROGRESS')].count").value(1))
            .andExpect(jsonPath("$[?(@.statusName == 'DONE')].count").value(2));
    }

    @Test
    @Transactional
    void getTaskCompletionEvolution() throws Exception {
        restTaskDashboardMockMvc
            .perform(get("/api/dashboard/task-completion-evolution").param("periodInDays", "7").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(3)); // Expecting 3 days with completed tasks
    }
}
