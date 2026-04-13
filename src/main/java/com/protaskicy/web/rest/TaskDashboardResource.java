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

/**
 * REST controller for managing Task Dashboard data.
 */
@RestController
@RequestMapping("/api/dashboard")
public class TaskDashboardResource {

    private final Logger log = LoggerFactory.getLogger(TaskDashboardResource.class);

    private final TaskDashboardService taskDashboardService;

    public TaskDashboardResource(TaskDashboardService taskDashboardService) {
        this.taskDashboardService = taskDashboardService;
    }

    /**
     * {@code GET /dashboard/stats} : get current user task statistics.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the taskStatsDTO.
     */
    @GetMapping("/stats")
    public ResponseEntity<TaskStatsDTO> getTaskStats() {
        log.debug("REST request to get TaskStatsDTO for current user");
        TaskStatsDTO taskStats = taskDashboardService.getTaskStatsForCurrentUser();
        return ResponseEntity.ok().body(taskStats);
    }

    /**
     * {@code GET /dashboard/status-distribution} : get current user task status distribution.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the list of taskStatusDistributionDTOs.
     */
    @GetMapping("/status-distribution")
    public ResponseEntity<List<TaskStatusDistributionDTO>> getTaskStatusDistribution() {
        log.debug("REST request to get TaskStatusDistributionDTO for current user");
        List<TaskStatusDistributionDTO> distribution = taskDashboardService.getTaskStatusDistributionForCurrentUser();
        return ResponseEntity.ok().body(distribution);
    }

    /**
     * {@code GET /dashboard/completion-evolution} : get current user task completion evolution.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the list of taskCompletionEvolutionDTOs.
     */
    @GetMapping("/completion-evolution")
    public ResponseEntity<List<TaskCompletionEvolutionDTO>> getTaskCompletionEvolution() {
        log.debug("REST request to get TaskCompletionEvolutionDTO for current user");
        List<TaskCompletionEvolutionDTO> evolution = taskDashboardService.getTaskCompletionEvolutionForCurrentUser();
        return ResponseEntity.ok().body(evolution);
    }
}
