package com.protaskicy.service.dto;

import java.io.Serializable;
import java.util.Objects;

public class TaskStatusDistributionDTO implements Serializable {

    private String status;
    private Long count;

    public TaskStatusDistributionDTO() {
        // Empty constructor needed for Jackson
    }

    public TaskStatusDistributionDTO(String status, Long count) {
        this.status = status;
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskStatusDistributionDTO that = (TaskStatusDistributionDTO) o;
        return Objects.equals(status, that.status) &&
               Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, count);
    }

    @Override
    public String toString() {
        return "TaskStatusDistributionDTO{" +
               "status='" + status + '\'' +
               ", count=" + count +
               '}';
    }
}
