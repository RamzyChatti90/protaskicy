package com.protaskicy.web.rest;

import static com.protaskicy.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.protaskicy.IntegrationTest;
import com.protaskicy.domain.enumeration.TaskStatus;
import com.protaskicy.service.TaskDashboardService;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the {@link TaskDashboardResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TaskDashboardResourceIT {

    private static final String API_URL = "/api/dashboard";

    @MockBean
    private TaskDashboardService taskDashboardService;

    @Autowired
    private MockMvc restTaskDashboardMockMvc;

    private TaskStatsDTO taskStatsDTO;
    private List<TaskStatusDistributionDTO> taskStatusDistributionDTOList;
    private List<TaskCompletionEvolutionDTO> taskCompletionEvolutionDTOList;

    @BeforeEach
    void setUp() {
        taskStatsDTO = new TaskStatsDTO();
        taskStatsDTO.setTotalTasks(10L);
        taskStatsDTO.setTodoTasks(5L);
        taskStatsDTO.setInProgressTasks(3L);
        taskStatsDTO.setDoneTasks(1L);
        taskStatsDTO.setCancelledTasks(1L);

        taskStatusDistributionDTOList = new ArrayList<>();
        taskStatusDistributionDTOList.add(new TaskStatusDistributionDTO(TaskStatus.TODO, 5L));
        taskStatusDistributionDTOList.add(new TaskStatusDistributionDTO(TaskStatus.DONE, 1L));

        Instant now = Instant.now().truncatedTo(ChronoUnit.DAYS);
        taskCompletionEvolutionDTOList = new ArrayList<>();
        taskCompletionEvolutionDTOList.add(new TaskCompletionEvolutionDTO(now.minus(7, ChronoUnit.DAYS), 5L));
        taskCompletionEvolutionDTOList.add(new TaskCompletionEvolutionDTO(now, 10L));
    }

    @Test
    void getTaskStats() throws Exception {
        when(taskDashboardService.getTaskStats()).thenReturn(taskStatsDTO);

        restTaskDashboardMockMvc
            .perform(get(API_URL + "/task-stats").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.totalTasks").value(taskStatsDTO.getTotalTasks().intValue()))
            .andExpect(jsonPath("$.todoTasks").value(taskStatsDTO.getTodoTasks().intValue()))
            .andExpect(jsonPath("$.inProgressTasks").value(taskStatsDTO.getInProgressTasks().intValue()))
            .andExpect(jsonPath("$.doneTasks").value(taskStatsDTO.getDoneTasks().intValue()))
            .andExpect(jsonPath("$.cancelledTasks").value(taskStatsDTO.getCancelledTasks().intValue()));

        verify(taskDashboardService, times(1)).getTaskStats();
    }

    @Test
    void getTaskStatusDistribution() throws Exception {
        when(taskDashboardService.getTaskStatusDistribution()).thenReturn(taskStatusDistributionDTOList);

        restTaskDashboardMockMvc
            .perform(get(API_URL + "/task-status-distribution").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].status").value(hasItem(TaskStatus.TODO.toString())))
            .andExpect(jsonPath("$.[*].count").value(hasItem(5)));

        verify(taskDashboardService, times(1)).getTaskStatusDistribution();
    }

    @Test
    void getTaskCompletionEvolution() throws Exception {
        when(taskDashboardService.getTaskCompletionEvolution()).thenReturn(taskCompletionEvolutionDTOList);

        restTaskDashboardMockMvc
            .perform(get(API_URL + "/task-completion-evolution").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].date").value(hasItem(sameInstant(taskCompletionEvolutionDTOList.get(0).getDate().atZone(ZoneId.systemDefault())))))
            .andExpect(jsonPath("$.[*].completedTasksCount").value(hasItem(taskCompletionEvolutionDTOList.get(0).getCompletedTasksCount().intValue())));

        verify(taskDashboardService, times(1)).getTaskCompletionEvolution();
    }
}
