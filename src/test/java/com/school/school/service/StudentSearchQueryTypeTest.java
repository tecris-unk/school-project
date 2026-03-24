package com.school.school.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StudentSearchQueryTypeTest {

    @Test
    void valueOf_shouldResolveAllSupportedQueryTypes() {
        assertEquals(StudentSearchQueryType.JPQL, StudentSearchQueryType.valueOf("JPQL"));
        assertEquals(StudentSearchQueryType.NATIVE, StudentSearchQueryType.valueOf("NATIVE"));
    }

    @Test
    void values_shouldKeepExpectedOrder() {
        assertArrayEquals(
                new StudentSearchQueryType[]{StudentSearchQueryType.JPQL, StudentSearchQueryType.NATIVE},
                StudentSearchQueryType.values()
        );
    }
}