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
            .countCompletedTasksByDayForUser(userLogin, startInstant, endInstant)
            .stream()
            .map(obj -> {
                LocalDate date = null;
                if (obj[0] instanceof Date) {
                    date = ((Date) obj[0]).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                } else if (obj[0] instanceof LocalDate) {
                    date = (LocalDate) obj[0];
                }
                Long count = (Long) obj[1];
                return new TaskCompletionEvolutionDTO(date, count);
            })
            .collect(Collectors.toList());
    }
}