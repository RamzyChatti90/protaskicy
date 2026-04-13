package com.protaskicy.service;

import com.protaskicy.domain.enumeration.TaskStatus;
import com.protaskicy.repository.TaskRepository;
import com.protaskicy.security.SecurityUtils;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date; // Inserted as per diagnostic
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskDashboardService {

    private final TaskRepository taskRepository;

    public TaskDashboardService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskStatsDTO getTaskStatsForCurrentUser() {
        String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new IllegalStateException("User not authenticated"));

        Long totalTasks = taskRepository.countByAssignedToIsCurrentUserLogin(userLogin);
        Long todoTasks = taskRepository.countByAssignedToIsCurrentUserLoginAndStatus(userLogin, TaskStatus.TODO);
        Long inProgressTasks = taskRepository.countByAssignedToIsCurrentUserLoginAndStatus(userLogin, TaskStatus.IN_PROGRESS);
        Long doneTasks = taskRepository.countByAssignedToIsCurrentUserLoginAndStatus(userLogin, TaskStatus.DONE);

        return new TaskStatsDTO(totalTasks, todoTasks, inProgressTasks, doneTasks);
    }

    public List<TaskStatusDistributionDTO> getTaskStatusDistributionForCurrentUser() {
        // The diagnostic mentioned replacing this .map() but provided no specific new logic.
        // The existing conversion logic `new TaskStatusDistributionDTO((TaskStatus) result[0], (Long) result[1])`
        // is standard for converting Object[] from a native query into a DTO when the types are directly castable.
        return taskRepository
            .countTasksByStatusForCurrentUser()
            .stream()
            .map(result -> new TaskStatusDistributionDTO((TaskStatus) result[0], (Long) result[1]))
            .collect(Collectors.toList());
    }

    public List<TaskCompletionEvolutionDTO> getTaskCompletionEvolutionForCurrentUser() {
        // The diagnostic referred to robust conversion logic for Timestamp to LocalDate,
        // which is already present and correctly implemented in the existing code.
        return taskRepository
            .countCompletedTasksByWeekForCurrentUser()
            .stream()
            .map(result -> {
                // result[0] is a java.sql.Timestamp, convert to LocalDate
                LocalDate date = ((java.sql.Timestamp) result[0]).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                Long count = (Long) result[1];
                return new TaskCompletionEvolutionDTO(date, count);
            })
            .collect(Collectors.toList());
    }
}