package com.school.school.service.async;

public interface AsyncBusinessTaskService {
    String startTask();

    TaskStatus getTaskStatus(String taskId);
}
