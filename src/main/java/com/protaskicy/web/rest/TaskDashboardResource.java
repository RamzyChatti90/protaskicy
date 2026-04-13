package com.protaskicy.web.rest;

import com.protaskicy.service.TaskDashboardService;
import com.protaskicy.service.dto.TaskCompletionEvolutionDTO;
import com.protaskicy.service.dto.TaskStatsDTO;
import com.protaskicy.service.dto.TaskStatusDistributionDTO;
import java.util.List;
import java.util.Optional;
import com.protaskicy.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing Task dashboard data.
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
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the {@code TaskStatsDTO} in body, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/task-stats")
    public ResponseEntity<TaskStatsDTO> getTaskStats() {
        log.debug("REST request to get Task Stats");
        Optional<TaskStatsDTO> taskStatsDTO = taskDashboardService.getTaskStats();
        return ResponseUtil.wrapOrNotFound(taskStatsDTO);
    }

    /**
     * {@code GET /task-status-distribution} : get task status distribution for the current user.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of {@code TaskStatusDistributionDTO} in body, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/task-status-distribution")
    public ResponseEntity<List<TaskStatusDistributionDTO>> getTaskStatusDistribution() {
        log.debug("REST request to get Task Status Distribution");
        Optional<List<TaskStatusDistributionDTO>> distribution = taskDashboardService.getTaskStatusDistribution();
        return ResponseUtil.wrapOrNotFound(distribution);
    }

    /**
     * {@code GET /task-completion-evolution} : get task completion evolution for the current user.
     *
     * @param days the number of days to look back.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of {@code TaskCompletionEvolutionDTO} in body, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/task-completion-evolution")
    public ResponseEntity<List<TaskCompletionEvolutionDTO>> getTaskCompletionEvolution(@RequestParam(defaultValue = "7") int days) {
        log.debug("REST request to get Task Completion Evolution for {} days", days);
        List<TaskCompletionEvolutionDTO> evolution = taskDashboardService.getTaskCompletionEvolution(SecurityUtils.getCurrentUserLogin().orElseThrow(), days);
        return ResponseEntity.ok().body(evolution);
    }
}
