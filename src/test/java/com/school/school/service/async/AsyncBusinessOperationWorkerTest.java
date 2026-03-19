package com.school.school.service.async;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class AsyncBusinessOperationWorkerTest {

    private final AsyncBusinessOperationWorker worker = new AsyncBusinessOperationWorker();

    @Test
    void runBusinessOperation_shouldCompleteSuccessfully() {
        CompletableFuture<Void> future = worker.runBusinessOperation();

        assertDoesNotThrow(future::join);
    }
}