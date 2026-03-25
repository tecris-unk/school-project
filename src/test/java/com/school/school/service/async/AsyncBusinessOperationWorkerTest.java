package com.school.school.service.async;

import java.util.concurrent.CompletionException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class AsyncBusinessOperationWorkerTest {

    private final AsyncBusinessOperationWorker worker = new AsyncBusinessOperationWorker();

    @Test
    void runBusinessOperation_shouldCompleteSuccessfully() {
        CompletableFuture<Void> future = worker.runBusinessOperation();

        assertDoesNotThrow(future::join);
    }

    @Test
    void runBusinessOperation_shouldFailWhenThreadWasInterrupted() {
        Thread.currentThread().interrupt();

        try {
            CompletableFuture<Void> future = worker.runBusinessOperation();
            assertThrows(CompletionException.class, future::join);
        } finally {
            Thread.interrupted();
        }
    }
}