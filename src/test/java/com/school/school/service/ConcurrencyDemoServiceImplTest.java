package com.school.school.service;

import com.school.school.service.dto.response.RaceConditionDemoResponse;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConcurrencyDemoServiceImplTest {

    private final ConcurrencyDemoServiceImpl service = new ConcurrencyDemoServiceImpl();

    @Test
    void runRaceConditionDemo_shouldUseMinimumThreadCountAndKeepSafeCountersAccurate() {
        RaceConditionDemoResponse response = service.runRaceConditionDemo(10, 20);

        assertEquals(50, response.getThreads());
        assertEquals(20, response.getIncrementsPerThread());
        assertEquals(1000, response.getExpected());
        assertEquals(1000, response.getSynchronizedResult());
        assertEquals(1000, response.getAtomicResult());
        assertTrue(response.getUnsafeResult() <= response.getExpected());
    }

    @Test
    void runRaceConditionDemo_shouldRespectProvidedThreadCountAboveMinimum() {
        RaceConditionDemoResponse response = service.runRaceConditionDemo(60, 5);

        assertEquals(60, response.getThreads());
        assertEquals(300, response.getExpected());
        assertEquals(300, response.getSynchronizedResult());
        assertEquals(300, response.getAtomicResult());
    }

    @Test
    void runRaceConditionDemo_shouldWrapInterruptedException() {
        ConcurrencyDemoServiceImpl interruptedService = new ConcurrencyDemoServiceImpl() {
            @Override
            void waitForTasks(final ExecutorService executorService, final List<Callable<Void>> tasks)
                    throws InterruptedException {
                throw new InterruptedException("boom");
            }
        };

        assertThrows(IllegalStateException.class, () -> interruptedService.runRaceConditionDemo(50, 1));
    }

    @Test
    void runRaceConditionDemo_shouldWrapExecutionException() {
        ConcurrencyDemoServiceImpl interruptedService = new ConcurrencyDemoServiceImpl() {
            @Override
            ExecutorService createExecutorService(final int actualThreads) {
                return Executors.newSingleThreadExecutor();
            }

            @Override
            void waitForTasks(final ExecutorService executorService, final List<Callable<Void>> tasks)
                    throws ExecutionException {
                throw new ExecutionException(new RuntimeException("boom"));
            }
        };

        assertThrows(IllegalStateException.class, () -> interruptedService.runRaceConditionDemo(50, 1));
    }

    @Test
    void runRaceConditionDemo_shouldForceShutdownNowWhenPoolDoesNotTerminate() throws InterruptedException {
        ExecutorService executorService = mock(ExecutorService.class);
        when(executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)).thenReturn(false);

        ConcurrencyDemoServiceImpl demoService = new ConcurrencyDemoServiceImpl() {
            @Override
            ExecutorService createExecutorService(final int actualThreads) {
                return executorService;
            }

            @Override
            void waitForTasks(final ExecutorService providedExecutor, final List<Callable<Void>> tasks) {
                //no-op
            }
        };

        demoService.runRaceConditionDemo(50, 0);

        verify(executorService).shutdown();
        verify(executorService).shutdownNow();
    }

    @Test
    void runRaceConditionDemo_shouldForceShutdownNowWhenAwaitIsInterrupted() throws InterruptedException {
        ExecutorService executorService = mock(ExecutorService.class);
        when(executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS))
                .thenThrow(new InterruptedException("interrupted"));

        ConcurrencyDemoServiceImpl demoService = new ConcurrencyDemoServiceImpl() {
            @Override
            ExecutorService createExecutorService(final int actualThreads) {
                return executorService;
            }

            @Override
            void waitForTasks(final ExecutorService providedExecutor, final List<Callable<Void>> tasks) {
                //no-op
            }
        };

        demoService.runRaceConditionDemo(50, 0);

        verify(executorService).shutdown();
        verify(executorService).shutdownNow();
    }

}
