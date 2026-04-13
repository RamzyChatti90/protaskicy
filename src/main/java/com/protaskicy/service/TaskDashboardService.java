package com.protaskicy.service;

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
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskDashboardService {

    private final Logger log = LoggerFactory.getLogger(TaskDashboardService.class);

    private final TaskRepository taskRepository;

    public TaskDashboardService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Get overall task statistics for the current user.
     *
     * @return the TaskStatsDTO containing task counts by status.
     */
    public Optional<TaskStatsDTO> getTaskStats() {
        return SecurityUtils.getCurrentUserLogin().map(login -> {
            Long totalTasks = taskRepository.countByAssignedTo_Login(login);
            Long todoTasks = taskRepository.countByAssignedTo_LoginAndStatus(login, TaskStatus.TODO);
            Long inProgressTasks = taskRepository.countByAssignedTo_LoginAndStatus(login, TaskStatus.IN_PROGRESS);
            Long doneTasks = taskRepository.countByAssignedTo_LoginAndStatus(login, TaskStatus.DONE);
            Long cancelledTasks = taskRepository.countByAssignedTo_LoginAndStatus(login, TaskStatus.CANCELLED);

            return new TaskStatsDTO(totalTasks, todoTasks, inProgressTasks, doneTasks, cancelledTasks);
        });
    }

    /**
     * Get task status distribution for the current user.
     *
     * @return a list of TaskStatusDistributionDTO.
     */
    public Optional<List<TaskStatusDistributionDTO>> getTaskStatusDistribution() {
        return SecurityUtils.getCurrentUserLogin().map(login ->
            Arrays.stream(TaskStatus.values())
                .map(status -> new TaskStatusDistributionDTO(status, taskRepository.countByAssignedTo_LoginAndStatus(login, status)))
                .collect(Collectors.toList())
        );
    }

    /**
     * Get task completion evolution for the current user over a given number of days.
     *
     * @param days the number of days to look back.
     * @return a list of TaskCompletionEvolutionDTO.
     */
    public List<TaskCompletionEvolutionDTO> getTaskCompletionEvolution(String login, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).minusNanos(1).toInstant();

        List<Object[]> results = taskRepository.countCompletedTasksByDayForUser(login, TaskStatus.DONE, startInstant, endInstant);

        return results.stream()
            .map(result -> new TaskCompletionEvolutionDTO((LocalDate) result[0], ((Number) result[1]).longValue()))
            .collect(Collectors.toList());
    }
}
