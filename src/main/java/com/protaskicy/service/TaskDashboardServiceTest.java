package com.protaskicy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.protaskicy.domain.enumeration.TaskStatus;
import com.protaskicy.repository.TaskRepository;
import com.protaskicy.security.SecurityUtils;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

    private static final String TEST_LOGIN = "user";

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(taskRepository);
    }

    @Test
    void getTaskStats_shouldReturnCorrectStats() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(TEST_LOGIN));

            when(taskRepository.countByAssignedTo_Login(TEST_LOGIN)).thenReturn(10L);
            when(taskRepository.countByAssignedTo_LoginAndStatus(TEST_LOGIN, TaskStatus.TODO)).thenReturn(3L);
            when(taskRepository.countByAssignedTo_LoginAndStatus(TEST_LOGIN, TaskStatus.IN_PROGRESS)).thenReturn(5L);
            when(taskRepository.countByAssignedTo_LoginAndStatus(TEST_LOGIN, TaskStatus.DONE)).thenReturn(2L);
            when(taskRepository.countByAssignedTo_LoginAndStatus(TEST_LOGIN, TaskStatus.CANCELLED)).thenReturn(0L);

            Optional<TaskStatsDTO> result = taskDashboardService.getTaskStats();

            assertThat(result).isPresent();
            TaskStatsDTO dto = result.get();
            assertThat(dto.getTotalTasks()).isEqualTo(10L);
            assertThat(dto.getTodoTasks()).isEqualTo(3L);
            assertThat(dto.getInProgressTasks()).isEqualTo(5L);
            assertThat(dto.getDoneTasks()).isEqualTo(2L);
            assertThat(dto.getCancelledTasks()).isEqualTo(0L);

            verify(taskRepository, times(1)).countByAssignedTo_Login(TEST_LOGIN);
            verify(taskRepository, times(4)).countByAssignedTo_LoginAndStatus(any(String.class), any(TaskStatus.class));
        }
    }

    @Test
    void getTaskStats_shouldReturnEmptyIfNoUserLogin() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.empty());

            Optional<TaskStatsDTO> result = taskDashboardService.getTaskStats();

            assertThat(result).isNotPresent();
            verifyNoInteractions(taskRepository);
        }
    }

    @Test
    void getTaskStatusDistribution_shouldReturnCorrectDistribution() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(TEST_LOGIN));

            when(taskRepository.countByAssignedTo_LoginAndStatus(TEST_LOGIN, TaskStatus.TODO)).thenReturn(3L);
            when(taskRepository.countByAssignedTo_LoginAndStatus(TEST_LOGIN, TaskStatus.IN_PROGRESS)).thenReturn(5L);
            when(taskRepository.countByAssignedTo_LoginAndStatus(TEST_LOGIN, TaskStatus.DONE)).thenReturn(2L);
            when(taskRepository.countByAssignedTo_LoginAndStatus(TEST_LOGIN, TaskStatus.CANCELLED)).thenReturn(0L);

            Optional<List<TaskStatusDistributionDTO>> result = taskDashboardService.getTaskStatusDistribution();

            assertThat(result).isPresent();
            List<TaskStatusDistributionDTO> distribution = result.get();
            assertThat(distribution).hasSize(TaskStatus.values().length);
            assertThat(distribution).containsExactlyInAnyOrder(
                new TaskStatusDistributionDTO(TaskStatus.TODO, 3L),
                new TaskStatusDistributionDTO(TaskStatus.IN_PROGRESS, 5L),
                new TaskStatusDistributionDTO(TaskStatus.DONE, 2L),
                new TaskStatusDistributionDTO(TaskStatus.CANCELLED, 0L)
            );
        }
    }

    @Test
    void getTaskStatusDistribution_shouldReturnEmptyIfNoUserLogin() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.empty());

            Optional<List<TaskStatusDistributionDTO>> result = taskDashboardService.getTaskStatusDistribution();

            assertThat(result).isNotPresent();
            verifyNoInteractions(taskRepository);
        }
    }

    @Test
    void getTaskCompletionEvolution_shouldReturnCorrectEvolution() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(TEST_LOGIN));

            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            // Mock the repository call for countCompletedTasksByDayForUser
            when(taskRepository.countCompletedTasksByDayForUser(
                eq(TEST_LOGIN),
                eq(TaskStatus.DONE),
                any(Instant.class),
                any(Instant.class)
            ))
                .thenReturn(Arrays.asList(
                    new Object[] { yesterday, 1L },
                    new Object[] { today, 2L }
                ));

            Optional<List<TaskCompletionEvolutionDTO>> result = taskDashboardService.getTaskCompletionEvolution(2);

            assertThat(result).isPresent();
            List<TaskCompletionEvolutionDTO> evolution = result.get();
            assertThat(evolution).hasSize(2);
            assertThat(evolution).containsExactlyInAnyOrder(
                new TaskCompletionEvolutionDTO(yesterday, 1L),
                new TaskCompletionEvolutionDTO(today, 2L)
            );

            verify(taskRepository, times(1)).countCompletedTasksByDayForUser(
                eq(TEST_LOGIN),
                eq(TaskStatus.DONE),
                any(Instant.class),
                any(Instant.class)
            );
        }
    }

    @Test
    void getTaskCompletionEvolution_shouldReturnEmptyIfNoUserLogin() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.empty());

            Optional<List<TaskCompletionEvolutionDTO>> result = taskDashboardService.getTaskCompletionEvolution(7);

            assertThat(result).isNotPresent();
            verifyNoInteractions(taskRepository);
        }
    }
}
