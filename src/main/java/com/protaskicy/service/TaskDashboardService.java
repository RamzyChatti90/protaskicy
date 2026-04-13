package com.protaskicy.service;

import com.protaskicy.domain.enumeration.TaskStatus;
import com.protaskicy.repository.TaskRepository;
import com.protaskicy.security.SecurityUtils;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

        Long totalTasks = taskRepository.countByAssignedToIsCurrentUserLogin(userLogin);
        Long todoTasks = taskRepository.countByAssignedToIsCurrentUserLoginAndStatus(userLogin, TaskStatus.TODO);
        Long inProgressTasks = taskRepository.countByAssignedToIsCurrentUserLoginAndStatus(userLogin, TaskStatus.IN_PROGRESS);
        Long doneTasks = taskRepository.countByAssignedToIsCurrentUserLoginAndStatus(userLogin, TaskStatus.DONE);
        Long cancelledTasks = taskRepository.countByAssignedToIsCurrentUserLoginAndStatus(userLogin, TaskStatus.CANCELLED);

        TaskStatsDTO taskStatsDTO = new TaskStatsDTO();
        taskStatsDTO.setTotalTasks(totalTasks);
        taskStatsDTO.setTodoTasks(todoTasks);
        taskStatsDTO.setInProgressTasks(inProgressTasks);
        taskStatsDTO.setDoneTasks(doneTasks);
        taskStatsDTO.setCancelledTasks(cancelledTasks);

        return taskStatsDTO;
    }

    public List<TaskStatusDistributionDTO> getTaskStatusDistribution() {
        List<Object[]> distribution = taskRepository.countTasksByStatusForCurrentUser();
        return distribution
            .stream()
            .map(obj -> new TaskStatusDistributionDTO((TaskStatus) obj[0], (Long) obj[1]))
            .collect(Collectors.toList());
    }

    public List<TaskCompletionEvolutionDTO> getTaskCompletionEvolution() {
        List<Object[]> evolution = taskRepository.countCompletedTasksByWeekForCurrentUser();
        return evolution
            .stream()
            .map(obj -> new TaskCompletionEvolutionDTO(((Instant) obj[0]).truncatedTo(ChronoUnit.DAYS), (Long) obj[1]))
            .collect(Collectors.toList());
    }
}
