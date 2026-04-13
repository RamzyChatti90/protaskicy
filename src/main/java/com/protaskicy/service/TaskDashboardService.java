package com.protaskicy.service;

import com.protaskicy.domain.enumeration.TaskStatus;
import com.protaskicy.repository.TaskRepository;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;
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

    public TaskStatsDTO getTaskStats(String userLogin) {
        log.debug("Request to get task statistics for user: {}", userLogin);

        Long totalTasks = taskRepository.countByAssignedTo_Login(userLogin);
        Long todoTasks = taskRepository.countByAssignedTo_LoginAndStatus(userLogin, TaskStatus.TODO);
        Long inProgressTasks = taskRepository.countByAssignedTo_LoginAndStatus(userLogin, TaskStatus.IN_PROGRESS);
        Long doneTasks = taskRepository.countByAssignedTo_LoginAndStatus(userLogin, TaskStatus.DONE);

        return new TaskStatsDTO(totalTasks, todoTasks, inProgressTasks, doneTasks);
    }

    public List<TaskStatusDistributionDTO> getTaskStatusDistribution(String userLogin) {
        log.debug("Request to get task status distribution for user: {}", userLogin);

        return Arrays
            .stream(TaskStatus.values())
            .map(status -> new TaskStatusDistributionDTO(status.name(), taskRepository.countByAssignedTo_LoginAndStatus(userLogin, status)))
            .collect(Collectors.toList());
    }

    public List<TaskCompletionEvolutionDTO> getTaskCompletionEvolution(String userLogin, int periodInDays) {
        log.debug("Request to get task completion evolution for user: {} over {} days", userLogin, periodInDays);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(periodInDays);

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // Include the end date fully

        return taskRepository
            .countCompletedTasksByDayForUser(userLogin, TaskStatus.DONE, startInstant, endInstant) // FIX: Added TaskStatus.DONE
            .stream()
            .map(obj -> {
                LocalDate date = null;
                if (obj[0] != null) {
                    if (obj[0] instanceof LocalDate) {
                        date = (LocalDate) obj[0];
                    } else if (obj[0] instanceof Date) { // Covers java.util.Date, java.sql.Date, java.sql.Timestamp
                        date = ((Date) obj[0]).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    } else if (obj[0] instanceof String) {
                        try {
                            date = LocalDate.parse((String) obj[0]);
                        } catch (java.time.format.DateTimeParseException e) {
                            log.warn("Failed to parse date string '{}' from database result. Setting date to null.", obj[0], e);
                            // date remains null
                        }
                    } else {
                        log.warn("Unexpected type for date object from database: {}. Value: {}. Setting date to null.", obj[0].getClass().getName(), obj[0]);
                        // date remains null
                    }
                }

                Long count = 0L; // Default value if null or unparseable
                if (obj[1] != null) {
                    if (obj[1] instanceof Long) {
                        count = (Long) obj[1];
                    } else if (obj[1] instanceof Integer) {
                        count = ((Integer) obj[1]).longValue();
                    } else if (obj[1] instanceof java.math.BigDecimal) {
                        count = ((java.math.BigDecimal) obj[1]).longValue();
                    } else if (obj[1] instanceof java.math.BigInteger) {
                        count = ((java.math.BigInteger) obj[1]).longValue();
                    } else if (obj[1] instanceof Double) {
                        count = ((Double) obj[1]).longValue();
                    } else {
                        log.warn("Unexpected type for count object from database: {}. Value: {}. Setting count to 0L.", obj[1].getClass().getName(), obj[1]);
                        // count remains 0L
                    }
                }
                return new TaskCompletionEvolutionDTO(date, count);
            })
            .collect(Collectors.toList());
    }
}