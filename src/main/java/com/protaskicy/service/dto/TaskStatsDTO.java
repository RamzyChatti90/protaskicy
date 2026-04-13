package com.protaskicy.service.dto;

import java.io.Serializable;
import java.util.Objects;

public class TaskStatsDTO implements Serializable {

    private Long totalTasks;
    private Long todoTasks;
    private Long inProgressTasks;
    private Long doneTasks;

    public TaskStatsDTO() {
        // Empty constructor needed for Jackson
    }

    public TaskStatsDTO(Long totalTasks, Long todoTasks, Long inProgressTasks, Long doneTasks) {
        this.totalTasks = totalTasks;
        this.todoTasks = todoTasks;
        this.inProgressTasks = inProgressTasks;
        this.doneTasks = doneTasks;
    }

    public Long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Long getTodoTasks() {
        return todoTasks;
    }

    public void setTodoTasks(Long todoTasks) {
        this.todoTasks = todoTasks;
    }

    public Long getInProgressTasks() {
        return inProgressTasks;
    }

    public void setInProgressTasks(Long inProgressTasks) {
        this.inProgressTasks = inProgressTasks;
    }

    public Long getDoneTasks() {
        return doneTasks;
    }

    public void setDoneTasks(Long doneTasks) {
        this.doneTasks = doneTasks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskStatsDTO that = (TaskStatsDTO) o;
        return (
            Objects.equals(totalTasks, that.totalTasks) &&
            Objects.equals(todoTasks, that.todoTasks) &&
            Objects.equals(inProgressTasks, that.inProgressTasks) &&
            Objects.equals(doneTasks, that.doneTasks)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalTasks, todoTasks, inProgressTasks, doneTasks);
    }

    @Override
    public String toString() {
        return "TaskStatsDTO{" +
            "totalTasks=" + totalTasks +
            ", todoTasks=" + todoTasks +
            ", inProgressTasks=" + inProgressTasks +
            ", doneTasks=" + doneTasks +
            '}';
    }
}
