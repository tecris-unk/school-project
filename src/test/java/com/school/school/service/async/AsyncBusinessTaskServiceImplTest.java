package com.school.school.service.async;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.school.school.exceptions.NotFoundException;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AsyncBusinessTaskServiceImplTest {

    @Mock
    private AsyncBusinessOperationWorker worker;

    @InjectMocks
    private AsyncBusinessTaskServiceImpl service;

    @Test
    void startTask_shouldEventuallyCompleteAndIncrementCounter() throws Exception {
        when(worker.runBusinessOperation()).thenReturn(CompletableFuture.completedFuture(null));

        String taskId = service.startTask();

        TaskStatus finalStatus = awaitTerminalState(taskId);

        assertNotNull(taskId);
        assertEquals(taskId, finalStatus.getTaskId());
        assertEquals(TaskState.COMPLETED, finalStatus.getState());
        assertEquals("Готово. Всего выполнено задач: 1", finalStatus.getMessage());
    }

    @Test
    void startTask_shouldMarkTaskAsFailedWhenWorkerFails() throws Exception {
        CompletableFuture<Void> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new IllegalStateException("boom"));
        when(worker.runBusinessOperation()).thenReturn(failedFuture);

        String taskId = service.startTask();

        TaskStatus finalStatus = awaitTerminalState(taskId);

        assertEquals(TaskState.FAILED, finalStatus.getState());
        assertEquals("Задача завершилась с ошибкой", finalStatus.getMessage());
    }

    @Test
    void getTaskStatus_shouldThrowWhenTaskIdUnknown() {
        assertThrows(NotFoundException.class, () -> service.getTaskStatus("missing-id"));
    }

    private TaskStatus awaitTerminalState(final String taskId) throws InterruptedException {
        for (int attempt = 0; attempt < 50; attempt++) {
            TaskStatus status = service.getTaskStatus(taskId);
            if (status.getState() == TaskState.COMPLETED || status.getState() == TaskState.FAILED) {
                return status;
            }
            Thread.sleep(20);
        }
        return service.getTaskStatus(taskId);
    }
}