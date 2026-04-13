package com.protaskicy.repository;

import com.protaskicy.domain.Task;
import com.protaskicy.domain.enumeration.TaskStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Task entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedToIsCurrentUser();

    long countByAssignedTo_Login(String login);

    long countByAssignedTo_LoginAndStatus(String login, TaskStatus status);

    @Query("SELECT CAST(t.createdAt as LocalDate), COUNT(t) FROM Task t WHERE t.assignedTo.login = :login AND t.status = 'DONE' AND t.createdAt >= :startDate AND t.createdAt <= :endDate GROUP BY CAST(t.createdAt as LocalDate) ORDER BY CAST(t.createdAt as LocalDate)")
    List<Object[]> countCompletedTasksByDayForUser(@Param("login") String login, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}
