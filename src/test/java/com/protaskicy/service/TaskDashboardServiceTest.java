package com.protaskicy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.protaskicy.domain.enumeration.TaskStatus;
import com.protaskicy.repository.TaskRepository;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class TaskDashboardServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskDashboardService taskDashboardService;

    private static final String USER_LOGIN = "testuser";

    @BeforeEach
    void setUp() {
        SecurityContextHolder
            .getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(USER_LOGIN, USER_LOGIN));
    }

    @Test
    void getTaskStats_shouldReturnCorrectStatistics() {
        when(taskRepository.countByAssignedTo_Login(USER_LOGIN)).thenReturn(10L);
        when(taskRepository.countByAssignedTo_LoginAndStatus(USER_LOGIN, TaskStatus.TODO)).thenReturn(3L);
        when(taskRepository.countByAssignedTo_LoginAndStatus(USER_LOGIN, TaskStatus.IN_PROGRESS)).thenReturn(5L);
        when(taskRepository.countByAssignedTo_LoginAndStatus(USER_LOGIN, TaskStatus.DONE)).thenReturn(2L);

        TaskStatsDTO result = taskDashboardService.getTaskStats().orElseThrow();

        assertThat(result.getTotalTasks()).isEqualTo(10L);
        assertThat(result.getTodoTasks()).isEqualTo(3L);
        assertThat(result.getInProgressTasks()).isEqualTo(5L);
        assertThat(result.getDoneTasks()).isEqualTo(2L);
    }

    @Test
    void getTaskStatusDistribution_shouldReturnCorrectDistribution() {
        when(taskRepository.countByAssignedTo_LoginAndStatus(USER_LOGIN, TaskStatus.TODO)).thenReturn(3L);
        when(taskRepository.countByAssignedTo_LoginAndStatus(USER_LOGIN, TaskStatus.IN_PROGRESS)).thenReturn(5L);
        when(taskRepository.countByAssignedTo_LoginAndStatus(USER_LOGIN, TaskStatus.DONE)).thenReturn(2L);

        List<TaskStatusDistributionDTO> result = taskDashboardService.getTaskStatusDistribution().orElseThrow();

        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder(
            new TaskStatusDistributionDTO(TaskStatus.TODO, 3L),
            new TaskStatusDistributionDTO(TaskStatus.IN_PROGRESS, 5L),
            new TaskStatusDistributionDTO(TaskStatus.DONE, 2L)
        );
    }

    @Test
    void getTaskCompletionEvolution_shouldReturnCorrectEvolution() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        when(
            taskRepository.countCompletedTasksByDayForUser(
                eq(USER_LOGIN),
                eq(TaskStatus.DONE),
                any(Instant.class),
                any(Instant.class)
            )
            )
        )
            .thenReturn(
                Arrays.asList(
                    new Object[] { yesterday, 1L },
                    new Object[] { today, 2L }
                )
        List<TaskCompletionEvolutionDTO> result = taskDashboardService.getTaskCompletionEvolution(2).orElseThrow();

        List<TaskCompletionEvolutionDTO> result = taskDashboardService.getTaskCompletionEvolution(USER_LOGIN, 2);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(
            new TaskCompletionEvolutionDTO(yesterday, 1L),
            new TaskCompletionEvolutionDTO(today, 2L)
        );
    }

    @Test
            taskRepository.countCompletedTasksByDayForUser(
                eq(USER_LOGIN),
                eq(TaskStatus.DONE),
                any(Instant.class),
                any(Instant.class)
            )
                any(Instant.class),
                any(Instant.class)
        List<TaskCompletionEvolutionDTO> result = taskDashboardService.getTaskCompletionEvolution(7).orElseThrow();
        )
            .thenReturn(Collections.emptyList());

        List<TaskCompletionEvolutionDTO> result = taskDashboardService.getTaskCompletionEvolution(USER_LOGIN, 7);

        assertThat(result).isEmpty();
    }
}
