package com.protaskicy.repository;

import com.protaskicy.domain.Task;
import java.util.List;
import com.protaskicy.domain.enumeration.TaskStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Task entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("select task from Task task where task.assignedTo.login = ?#{authentication.name}")
    List<Task> findByAssignedToIsCurrentUser();

    Long countByAssignedToIsCurrentUserLogin(String login);

    Long countByAssignedToIsCurrentUserLoginAndStatus(String login, TaskStatus status);

    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.assignedTo.login = ?#{authentication.name} GROUP BY t.status")
    List<Object[]> countTasksByStatusForCurrentUser();

    @Query(
        "SELECT FUNCTION('DATE_TRUNC', 'week', t.createdAt), COUNT(t) FROM Task t WHERE t.assignedTo.login = ?#{authentication.name} AND t.status = 'DONE' GROUP BY FUNCTION('DATE_TRUNC', 'week', t.createdAt) ORDER BY FUNCTION('DATE_TRUNC', 'week', t.createdAt) ASC"
    )
    List<Object[]> countCompletedTasksByWeekForCurrentUser();
}
