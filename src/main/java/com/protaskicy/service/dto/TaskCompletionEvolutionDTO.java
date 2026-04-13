package com.protaskicy.service.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class TaskCompletionEvolutionDTO implements Serializable {

    private Instant date;
    private Long completedTasksCount;

    public TaskCompletionEvolutionDTO() {}

    public TaskCompletionEvolutionDTO(Instant date, Long completedTasksCount) {
        this.date = date;
        this.completedTasksCount = completedTasksCount;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Long getCompletedTasksCount() {
        return completedTasksCount;
    }

    public void setCompletedTasksCount(Long completedTasksCount) {
        this.completedTasksCount = completedTasksCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskCompletionEvolutionDTO that = (TaskCompletionEvolutionDTO) o;
        return Objects.equals(date, that.date) && Objects.equals(completedTasksCount, that.completedTasksCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, completedTasksCount);
    }

    @Override
    public String toString() {
        return "TaskCompletionEvolutionDTO{" +
            "date=" + date +
            ", completedTasksCount=" + completedTasksCount +
            '}';
    }
}
