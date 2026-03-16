package com.school.school.service;

import com.school.school.service.dto.response.RaceConditionDemoResponse;

public interface ConcurrencyDemoService {
    RaceConditionDemoResponse runRaceConditionDemo(int threads, int incrementsPerThread);
}
