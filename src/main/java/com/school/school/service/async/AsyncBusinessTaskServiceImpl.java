package com.school.school.service.async;

import com.school.school.exceptions.NotFoundException;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncBusinessTaskServiceImpl implements AsyncBusinessTaskService {

    private final Map<String, TaskStatus> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger completedTaskCounter = new AtomicInteger();
    private final AsyncBusinessOperationWorker worker;

    @Override
    public String startTask() {
        String taskId = UUID.randomUUID().toString();

        TaskStatus pendingStatus = TaskStatus.builder()
                .taskId(taskId)
                .state(TaskState.PENDING)
                .message("Задача поставлена в очередь")
                .build();
        tasks.put(taskId, pendingStatus);

        CompletableFuture.runAsync(() -> tasks.put(taskId, TaskStatus.builder()
                .taskId(taskId)
                .state(TaskState.RUNNING)
                .message("Бизнес-операция выполняется")
                .build())).thenCompose(ignored -> worker.runBusinessOperation())
                .thenRun(() -> {
                    int done = completedTaskCounter.incrementAndGet();
                    tasks.put(taskId, TaskStatus.builder()
                            .taskId(taskId)
                            .state(TaskState.COMPLETED)
                            .message("Готово. Всего выполнено задач: " + done)
                            .build());
                })
                .exceptionally(exception -> {
                    tasks.put(taskId, TaskStatus.builder()
                            .taskId(taskId)
                            .state(TaskState.FAILED)
                            .message("Задача завершилась с ошибкой")
                            .build());
                    log.error("Ошибка выполнения асинхронной задачи {}", taskId, exception);
                    return null;
                });

        return taskId;
    }

    @Override
    public TaskStatus getTaskStatus(final String taskId) {
        TaskStatus status = tasks.get(taskId);
        if (status == null) {
            throw new NotFoundException("Задача с id " + taskId + " не найдена");
        }
        return status;
    }
}