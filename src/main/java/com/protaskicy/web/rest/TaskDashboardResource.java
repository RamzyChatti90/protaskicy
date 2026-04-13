package com.protaskicy.web.rest;

import com.protaskicy.security.SecurityUtils;
import com.protaskicy.service.TaskDashboardService;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing Task Dashboard related operations.
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
     * {@code GET /task-stats} : get task statistics for the current user.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with the task stats in body.
     */
    @GetMapping("/task-stats")
    public ResponseEntity<TaskStatsDTO> getTaskStats() {
        log.debug("REST request to get TaskStats for current user");
        Optional<String> userLogin = SecurityUtils.getCurrentUserLogin();
        if (userLogin.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        TaskStatsDTO taskStats = taskDashboardService.getTaskStats(userLogin.get());
        return ResponseEntity.ok().body(taskStats);
    }

    /**
     * {@code GET /task-status-distribution} : get task status distribution for the current user.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with the task status distribution in body.
     */
    @GetMapping("/task-status-distribution")
    public ResponseEntity<List<TaskStatusDistributionDTO>> getTaskStatusDistribution() {
        log.debug("REST request to get TaskStatusDistribution for current user");
        Optional<String> userLogin = SecurityUtils.getCurrentUserLogin();
        if (userLogin.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<TaskStatusDistributionDTO> distribution = taskDashboardService.getTaskStatusDistribution(userLogin.get());
        return ResponseEntity.ok().body(distribution);
    }

    /**
     * {@code GET /task-completion-evolution} : get task completion evolution for the current user over a period.
     *
     * @param periodInDays the number of days for the evolution.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with the task completion evolution in body.
     */
    @GetMapping("/task-completion-evolution")
    public ResponseEntity<List<TaskCompletionEvolutionDTO>> getTaskCompletionEvolution(@RequestParam(defaultValue = "7") int periodInDays) {
        log.debug("REST request to get TaskCompletionEvolution for current user over {} days", periodInDays);
        Optional<String> userLogin = SecurityUtils.getCurrentUserLogin();
        if (userLogin.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<TaskCompletionEvolutionDTO> evolution = taskDashboardService.getTaskCompletionEvolution(userLogin.get(), periodInDays);
        return ResponseEntity.ok().body(evolution);
    }
}
