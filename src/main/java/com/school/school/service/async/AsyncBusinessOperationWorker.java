package com.school.school.service.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class AsyncBusinessOperationWorker {

    @Async("businessTaskExecutor")
    public CompletableFuture<Void> runBusinessOperation() {
        try {
            Thread.sleep(2000);
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(exception);
            return failed;
        }
    }
}