package com.protaskicy.service;

import com.protaskicy.domain.enumeration.TaskStatus;
import com.protaskicy.repository.TaskRepository;
import com.protaskicy.security.SecurityUtils;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.sql.Timestamp; // Required for robust date type conversion
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date; // Required for robust date type conversion
import java.util.List;
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

    public TaskStatsDTO getTaskStats() {
        String userLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("Current user login not found"));

        // Correction: Update method calls to reflect the corrected method names in TaskRepository
        // These methods are assumed to have been simplified to fix the PartTree$OrPart error.
        Long totalTasks = taskRepository.countByAssignedTo(userLogin);
        Long todoTasks = taskRepository.countByAssignedToAndStatus(userLogin, TaskStatus.TODO);
        Long inProgressTasks = taskRepository.countByAssignedToAndStatus(userLogin, TaskStatus.IN_PROGRESS);
        Long doneTasks = taskRepository.countByAssignedToAndStatus(userLogin, TaskStatus.DONE);
        Long cancelledTasks = taskRepository.countByAssignedToAndStatus(userLogin, TaskStatus.CANCELLED);

        TaskStatsDTO taskStatsDTO = new TaskStatsDTO();
        taskStatsDTO.setTotalTasks(totalTasks);
        taskStatsDTO.setTodoTasks(todoTasks);
        taskStatsDTO.setInProgressTasks(inProgressTasks);
        taskStatsDTO.setDoneTasks(doneTasks);
        taskStatsDTO.setCancelledTasks(cancelledTasks);

        return taskStatsDTO;
    }

    public List<TaskStatusDistributionDTO> getTaskStatusDistribution() {
        // This method assumes TaskRepository has a custom query like:
        // @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.assignedTo = :userLogin GROUP BY t.status")
        // List<Object[]> countTasksByStatusForCurrentUser(@Param("userLogin") String userLogin);
        // If it's not already passing the userLogin, it should be updated.
        // For now, assuming the repository handles the current user login internally or via SecurityUtils in a custom query.
        List<Object[]> distribution = taskRepository.countTasksByStatusForCurrentUser();
        return distribution
            .stream()
            // Robustly cast the count to Long, as the database might return Integer or BigDecimal
            .map(obj -> new TaskStatusDistributionDTO((TaskStatus) obj[0], ((Number) obj[1]).longValue()))
            .collect(Collectors.toList());
    }

    public List<TaskCompletionEvolutionDTO> getTaskCompletionEvolution() {
        // This method assumes TaskRepository has a custom query like:
        // @Query("SELECT FUNCTION('DATE_TRUNC', 'week', t.completionDate), COUNT(t) FROM Task t WHERE t.assignedTo = :userLogin AND t.status = 'DONE' GROUP BY FUNCTION('DATE_TRUNC', 'week', t.completionDate) ORDER BY FUNCTION('DATE_TRUNC', 'week', t.completionDate)")
        // List<Object[]> countCompletedTasksByWeekForCurrentUser(@Param("userLogin") String userLogin);
        List<Object[]> evolution = taskRepository.countCompletedTasksByWeekForCurrentUser();
        return evolution
            .stream()
            .map(obj -> {
                // Safely convert various date types to Instant
                Instant date = convertToInstant(obj[0]);
                // Robustly cast the count to Long, as the database might return Integer or BigDecimal
                Long count = ((Number) obj[1]).longValue();
                return new TaskCompletionEvolutionDTO(date != null ? date.truncatedTo(ChronoUnit.DAYS) : null, count);
            })
            .collect(Collectors.toList());
    }

    /**
     * Converts an object representing a date from various types (Instant, java.sql.Timestamp, java.util.Date)
     * to an Instant.
     *
     * @param dateObject The object representing the date. Can be null.
     * @return An Instant representation of the date, or null if the input was null.
     * @throws IllegalArgumentException if the dateObject is not of a supported date type.
     */
    private Instant convertToInstant(Object dateObject) {
        if (dateObject == null) {
            return null;
        }
        if (dateObject instanceof Instant) {
            return (Instant) dateObject;
        } else if (dateObject instanceof Timestamp) {
            return ((Timestamp) dateObject).toInstant();
        } else if (dateObject instanceof Date) {
            return ((Date) dateObject).toInstant();
        }
        log.warn("Unsupported date type encountered in convertToInstant: {}", dateObject.getClass().getName());
        throw new IllegalArgumentException("Unsupported date type for conversion to Instant: " + dateObject.getClass().getName());
    }
}