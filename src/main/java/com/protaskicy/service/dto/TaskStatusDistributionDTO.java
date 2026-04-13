package com.protaskicy.service.dto;

import com.protaskicy.domain.enumeration.TaskStatus;
import java.io.Serializable;
import java.util.Objects;

public class TaskStatusDistributionDTO implements Serializable {

    private TaskStatus status;
    private Long count;

    public TaskStatusDistributionDTO() {}

    public TaskStatusDistributionDTO(TaskStatus status, Long count) {
        this.status = status;
        this.count = count;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskStatusDistributionDTO that = (TaskStatusDistributionDTO) o;
        return Objects.equals(status, that.status) && Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, count);
    }

    @Override
    public String toString() {
        return "TaskStatusDistributionDTO{" +
            "status=" + status +
            ", count=" + count +
            '}';
    }
}
