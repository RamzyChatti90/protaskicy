package com.protaskicy.web.rest;

import com.protaskicy.service.TaskDashboardService;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class TaskDashboardResource {

    private final Logger log = LoggerFactory.getLogger(TaskDashboardResource.class);

    private final TaskDashboardService taskDashboardService;

    public TaskDashboardResource(TaskDashboardService taskDashboardService) {
        this.taskDashboardService = taskDashboardService;
    }

    @GetMapping("/task-stats")
    public ResponseEntity<TaskStatsDTO> getTaskStats() {
        log.debug("REST request to get Task Stats for current user");
        TaskStatsDTO taskStats = taskDashboardService.getTaskStats();
        return ResponseEntity.ok(taskStats);
    }

    @GetMapping("/task-status-distribution")
    public ResponseEntity<List<TaskStatusDistributionDTO>> getTaskStatusDistribution() {
        log.debug("REST request to get Task Status Distribution for current user");
        List<TaskStatusDistributionDTO> distribution = taskDashboardService.getTaskStatusDistribution();
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/task-completion-evolution")
    public ResponseEntity<List<TaskCompletionEvolutionDTO>> getTaskCompletionEvolution() {
        log.debug("REST request to get Task Completion Evolution for current user");
        List<TaskCompletionEvolutionDTO> evolution = taskDashboardService.getTaskCompletionEvolution();
        return ResponseEntity.ok(evolution);
    }
}
