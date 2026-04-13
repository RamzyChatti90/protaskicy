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
    /**
     * Correction 1: The method `findByAssignedToIsCurrentUser()` is not a standard Spring Data JPA derived query pattern
     * and would typically cause a `PartTreeJpaQuery` error during application startup.
     * Spring Data JPA's query parser cannot interpret "IsCurrentUser" as a property or keyword in this context.
     *
     * To find tasks assigned to the current user, the standard approach is to use a derived query based on the
     * `assignedTo` entity's `login` field, and then pass the current user's login from the security context
     * (e.g., using `SecurityUtils.getCurrentUserLogin()`) in the service layer.
     */
    List<Task> findByAssignedTo_Login(String login);

    long countByAssignedTo_Login(String login);

    long countByAssignedTo_LoginAndStatus(String login, TaskStatus status);

    /**
     * Correction 2: For robustness and type safety, it's generally better to pass enum values as parameters
     * rather than embedding string literals for enum fields directly in the JPQL query (`t.status = 'DONE'`).
     * This avoids potential issues with how the JPA provider maps enum strings to their internal representation
     * and makes the query more readable and less error-prone.
     */
    @Query("SELECT CAST(t.createdAt as LocalDate), COUNT(t) FROM Task t WHERE t.assignedTo.login = :login AND t.status = :statusDone AND t.createdAt >= :startDate AND t.createdAt <= :endDate GROUP BY CAST(t.createdAt as LocalDate) ORDER BY CAST(t.createdAt as LocalDate)")
    List<Object[]> countCompletedTasksByDayForUser(
        @Param("login") String login,
        @Param("statusDone") TaskStatus statusDone, // Added parameter for the TaskStatus enum
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate
    );
}