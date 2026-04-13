package com.protaskicy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.protaskicy.domain.Task;
import com.protaskicy.domain.User;
import com.protaskicy.domain.enumeration.TaskStatus;
import com.protaskicy.repository.TaskRepository;
import com.protaskicy.security.SecurityUtils;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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

    private static final String CURRENT_USER_LOGIN = "testuser";


    @Test
    void getTaskStatsForCurrentUser_shouldReturnCorrectStats() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(CURRENT_USER_LOGIN));
            // Arrange
            when(taskRepository.countByAssignedToIsCurrentUserLogin(CURRENT_USER_LOGIN)).thenReturn(10L);
            when(taskRepository.countByAssignedToIsCurrentUserLoginAndStatus(CURRENT_USER_LOGIN, TaskStatus.TODO)).thenReturn(3L);
            when(taskRepository.countByAssignedToIsCurrentUserLoginAndStatus(CURRENT_USER_LOGIN, TaskStatus.IN_PROGRESS)).thenReturn(5L);
            when(taskRepository.countByAssignedToIsCurrentUserLoginAndStatus(CURRENT_USER_LOGIN, TaskStatus.DONE)).thenReturn(2L);

            // Act
            TaskStatsDTO result = taskDashboardService.getTaskStatsForCurrentUser();

            // Assert
            assertThat(result.getTotalTasks()).isEqualTo(10L);
            assertThat(result.getTodoTasks()).isEqualTo(3L);
            assertThat(result.getInProgressTasks()).isEqualTo(5L);
            assertThat(result.getDoneTasks()).isEqualTo(2L);
            verify(taskRepository, times(1)).countByAssignedToIsCurrentUserLogin(CURRENT_USER_LOGIN);
            verify(taskRepository, times(1)).countByAssignedToIsCurrentUserLoginAndStatus(CURRENT_USER_LOGIN, TaskStatus.TODO);
            verify(taskRepository, times(1)).countByAssignedToIsCurrentUserLoginAndStatus(CURRENT_USER_LOGIN, TaskStatus.IN_PROGRESS);
            verify(taskRepository, times(1)).countByAssignedToIsCurrentUserLoginAndStatus(CURRENT_USER_LOGIN, TaskStatus.DONE);
        }
    }

    @Test
    void getTaskStatusDistributionForCurrentUser_shouldReturnCorrectDistribution() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(CURRENT_USER_LOGIN));
            // Arrange
            List<Object[]> mockData = Arrays.asList(
                new Object[] { TaskStatus.TODO, 3L },
                new Object[] { TaskStatus.IN_PROGRESS, 5L },
                new Object[] { TaskStatus.DONE, 2L }
            );
            when(taskRepository.countTasksByStatusForCurrentUser()).thenReturn(mockData);

            // Act
            List<TaskStatusDistributionDTO> result = taskDashboardService.getTaskStatusDistributionForCurrentUser();

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyInAnyOrder(
                new TaskStatusDistributionDTO(TaskStatus.TODO, 3L),
                new TaskStatusDistributionDTO(TaskStatus.IN_PROGRESS, 5L),
                new TaskStatusDistributionDTO(TaskStatus.DONE, 2L)
            );
            verify(taskRepository, times(1)).countTasksByStatusForCurrentUser();
        }
    }

    @Test
    void getTaskCompletionEvolutionForCurrentUser_shouldReturnCorrectEvolution() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(CURRENT_USER_LOGIN));
            // Arrange
            // Simulate java.sql.Timestamp for DATE_TRUNC('week', ...) results
            Instant week1 = LocalDate.of(2023, 1, 2).atStartOfDay(ZoneOffset.UTC).toInstant(); // Monday of week 1
            Instant week2 = LocalDate.of(2023, 1, 9).atStartOfDay(ZoneOffset.UTC).toInstant(); // Monday of week 2

            List<Object[]> mockData = Arrays.asList(
                new Object[] { java.sql.Timestamp.from(week1), 5L },
                new Object[] { java.sql.Timestamp.from(week2), 8L }
            );
            when(taskRepository.countCompletedTasksByWeekForCurrentUser()).thenReturn(mockData);

            // Act
            List<TaskCompletionEvolutionDTO> result = taskDashboardService.getTaskCompletionEvolutionForCurrentUser();

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(
                new TaskCompletionEvolutionDTO(LocalDate.of(2023, 1, 2), 5L),
                new TaskCompletionEvolutionDTO(LocalDate.of(2023, 1, 9), 8L)
            );
            verify(taskRepository, times(1)).countCompletedTasksByWeekForCurrentUser();
        }
    }
    }
}
