package com.school.school.service.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RaceConditionDemoResponse {
    int threads;
    int incrementsPerThread;
    int expected;
    int unsafeResult;
    int synchronizedResult;
    int atomicResult;
}
