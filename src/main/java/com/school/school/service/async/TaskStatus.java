package com.school.school.service.async;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TaskStatus {
    String taskId;
    TaskState state;
    String message;
}