package com.school.school.service;

import com.school.school.service.dto.response.TaskCreatedResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TaskCreatedResponseTest {

    @Test
    void record_shouldExposeTaskIdAndSupportValueEquality() {
        TaskCreatedResponse first = new TaskCreatedResponse("task-123");
        TaskCreatedResponse second = new TaskCreatedResponse("task-123");

        assertEquals("task-123", first.taskId());
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}