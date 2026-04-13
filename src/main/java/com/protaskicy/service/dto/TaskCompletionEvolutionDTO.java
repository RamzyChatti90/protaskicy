package com.protaskicy.service.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class TaskCompletionEvolutionDTO implements Serializable {

    private LocalDate date;
    private Long completedTasks;

    public TaskCompletionEvolutionDTO() {
        // Empty constructor needed for Jackson
    }

    public TaskCompletionEvolutionDTO(LocalDate date, Long completedTasks) {
        this.date = date;
        this.completedTasks = completedTasks;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(Long completedTasks) {
        this.completedTasks = completedTasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskCompletionEvolutionDTO that = (TaskCompletionEvolutionDTO) o;
        return Objects.equals(date, that.date) &&
               Objects.equals(completedTasks, that.completedTasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, completedTasks);
    }

    @Override
    public String toString() {
        return "TaskCompletionEvolutionDTO{" +
               "date=" + date +
               ", completedTasks=" + completedTasks +
               '}';
    }
}
