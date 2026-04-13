package com.protaskicy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.protaskicy.domain.enumeration.TaskStatus;
import com.protaskicy.repository.TaskRepository;
import com.protaskicy.security.SecurityUtils;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskDashboardServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskDashboardService taskDashboardService;

    private String currentUserLogin;

    @BeforeEach
    void setUp() {
        currentUserLogin = "testuser";
    }

    @Test
    void getTaskStats() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(currentUserLogin));

            when(taskRepository.countByAssignedTo(currentUserLogin)).thenReturn(5L);
            when(taskRepository.countByAssignedToAndStatus(currentUserLogin, TaskStatus.TODO)).thenReturn(2L);
            when(taskRepository.countByAssignedToAndStatus(currentUserLogin, TaskStatus.IN_PROGRESS)).thenReturn(1L);
            when(taskRepository.countByAssignedToAndStatus(currentUserLogin, TaskStatus.DONE)).thenReturn(1L);
            when(taskRepository.countByAssignedToAndStatus(currentUserLogin, TaskStatus.CANCELLED)).thenReturn(1L);

            TaskStatsDTO result = taskDashboardService.getTaskStats();

            assertThat(result.getTotalTasks()).isEqualTo(5L);
            assertThat(result.getTodoTasks()).isEqualTo(2L);
            assertThat(result.getInProgressTasks()).isEqualTo(1L);
            assertThat(result.getDoneTasks()).isEqualTo(1L);
            assertThat(result.getCancelledTasks()).isEqualTo(1L);

            verify(taskRepository, times(1)).countByAssignedTo(currentUserLogin);
            verify(taskRepository, times(1)).countByAssignedToAndStatus(currentUserLogin, TaskStatus.TODO);
            verify(taskRepository, times(1)).countByAssignedToAndStatus(currentUserLogin, TaskStatus.IN_PROGRESS);
            verify(taskRepository, times(1)).countByAssignedToAndStatus(currentUserLogin, TaskStatus.DONE);
            verify(taskRepository, times(1)).countByAssignedToAndStatus(currentUserLogin, TaskStatus.CANCELLED);
        }
    }

    @Test
    void getTaskStatusDistribution() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(currentUserLogin));

            List<Object[]> mockDistribution = Arrays.asList(
                new Object[] { TaskStatus.TODO, 2L },
                new Object[] { TaskStatus.DONE, 3L }
            );
            // Assuming countTasksByStatusForCurrentUser() is correctly implemented in TaskRepository
            when(taskRepository.countTasksByStatusForCurrentUser()).thenReturn(mockDistribution);

            List<TaskStatusDistributionDTO> result = taskDashboardService.getTaskStatusDistribution();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(
                new TaskStatusDistributionDTO(TaskStatus.TODO, 2L),
                new TaskStatusDistributionDTO(TaskStatus.DONE, 3L)
            );

            verify(taskRepository, times(1)).countTasksByStatusForCurrentUser();
        }
    }

    @Test
    void getTaskCompletionEvolution() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(currentUserLogin));

            Instant now = Instant.now().truncatedTo(ChronoUnit.DAYS);
            List<Object[]> mockEvolution = Arrays.asList(
                new Object[] { now.minus(7, ChronoUnit.DAYS), 5L },
                new Object[] { now, 10L }
            );
            // Assuming countCompletedTasksByWeekForCurrentUser() is correctly implemented in TaskRepository
            when(taskRepository.countCompletedTasksByWeekForCurrentUser()).thenReturn(mockEvolution);

            List<TaskCompletionEvolutionDTO> result = taskDashboardService.getTaskCompletionEvolution();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(
                new TaskCompletionEvolutionDTO(now.minus(7, ChronoUnit.DAYS), 5L),
                new TaskCompletionEvolutionDTO(now, 10L)
            );

            verify(taskRepository, times(1)).countCompletedTasksByWeekForCurrentUser();
        }
    }
}