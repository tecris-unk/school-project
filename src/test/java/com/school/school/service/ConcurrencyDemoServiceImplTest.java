package com.school.school.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.school.school.service.dto.response.RaceConditionDemoResponse;
import org.junit.jupiter.api.Test;

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
}
