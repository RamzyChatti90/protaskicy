package com.protaskicy.web.rest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.protaskicy.ProtaskicyApp;
import com.protaskicy.domain.enumeration.TaskStatus;
import com.protaskicy.service.TaskDashboardService;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link TaskDashboardResource} REST controller.
 */
@SpringBootTest(classes = ProtaskicyApp.class)
@AutoConfigureMockMvc
@WithMockUser
class TaskDashboardResourceIT {

    @Autowired
    private MockMvc restTaskDashboardMockMvc;

    @MockBean
    private TaskDashboardService mockTaskDashboardService;

    @BeforeEach
    void setUp() {
        reset(mockTaskDashboardService);
    }

    @Test
    void getTaskStats_shouldReturnStats() throws Exception {
        TaskStatsDTO statsDTO = new TaskStatsDTO(10L, 3L, 5L, 2L, 0L);
        when(mockTaskDashboardService.getTaskStats()).thenReturn(Optional.of(statsDTO));

        restTaskDashboardMockMvc
            .perform(get("/api/dashboard/task-stats").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalTasks").value(10L))
            .andExpect(jsonPath("$.todoTasks").value(3L))
            .andExpect(jsonPath("$.inProgressTasks").value(5L))
            .andExpect(jsonPath("$.doneTasks").value(2L))
            .andExpect(jsonPath("$.cancelledTasks").value(0L));

        verify(mockTaskDashboardService, times(1)).getTaskStats();
    }

    @Test
    void getTaskStats_shouldReturnNotFoundWhenNoStats() throws Exception {
        when(mockTaskDashboardService.getTaskStats()).thenReturn(Optional.empty());

        restTaskDashboardMockMvc
            .perform(get("/api/dashboard/task-stats").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(mockTaskDashboardService, times(1)).getTaskStats();
    }

    @Test
    void getTaskStatusDistribution_shouldReturnDistribution() throws Exception {
        List<TaskStatusDistributionDTO> distribution = Arrays.asList(
            new TaskStatusDistributionDTO(TaskStatus.TODO, 3L),
            new TaskStatusDistributionDTO(TaskStatus.IN_PROGRESS, 5L)
        );
        when(mockTaskDashboardService.getTaskStatusDistribution()).thenReturn(Optional.of(distribution));

        restTaskDashboardMockMvc
            .perform(get("/api/dashboard/task-status-distribution").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].status").value("TODO"))
            .andExpect(jsonPath("$[0].count").value(3L))
            .andExpect(jsonPath("$[1].status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$[1].count").value(5L));

        verify(mockTaskDashboardService, times(1)).getTaskStatusDistribution();
    }

    @Test
    void getTaskStatusDistribution_shouldReturnNotFoundWhenNoDistribution() throws Exception {
        when(mockTaskDashboardService.getTaskStatusDistribution()).thenReturn(Optional.empty());

        restTaskDashboardMockMvc
            .perform(get("/api/dashboard/task-status-distribution").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(mockTaskDashboardService, times(1)).getTaskStatusDistribution();
    }

    @Test
    void getTaskCompletionEvolution_shouldReturnEvolution() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        List<TaskCompletionEvolutionDTO> evolution = Arrays.asList(
            new TaskCompletionEvolutionDTO(yesterday, 1L),
            new TaskCompletionEvolutionDTO(today, 2L)
        );
        when(mockTaskDashboardService.getTaskCompletionEvolution(anyInt())).thenReturn(Optional.of(evolution));

        restTaskDashboardMockMvc
            .perform(get("/api/dashboard/task-completion-evolution?days=2").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].date").value(yesterday.toString()))
            .andExpect(jsonPath("$[0].completedTasksCount").value(1L))
            .andExpect(jsonPath("$[1].date").value(today.toString()))
            .andExpect(jsonPath("$[1].completedTasksCount").value(2L));

        verify(mockTaskDashboardService, times(1)).getTaskCompletionEvolution(2);
    }

    @Test
    void getTaskCompletionEvolution_shouldReturnNotFoundWhenNoEvolution() throws Exception {
        when(mockTaskDashboardService.getTaskCompletionEvolution(anyInt())).thenReturn(Optional.empty());

        restTaskDashboardMockMvc
            .perform(get("/api/dashboard/task-completion-evolution?days=7").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(mockTaskDashboardService, times(1)).getTaskCompletionEvolution(7);
    }

    @Test
    void getTaskCompletionEvolution_shouldUseDefaultDaysWhenNotProvided() throws Exception {
        when(mockTaskDashboardService.getTaskCompletionEvolution(eq(7))).thenReturn(Optional.of(Collections.emptyList()));

        restTaskDashboardMockMvc
            .perform(get("/api/dashboard/task-completion-evolution").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(mockTaskDashboardService, times(1)).getTaskCompletionEvolution(7);
    }
}
