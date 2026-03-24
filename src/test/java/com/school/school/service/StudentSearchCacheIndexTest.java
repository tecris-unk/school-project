package com.school.school.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.school.school.service.StudentSearchCacheIndex.StudentSearchCacheKey;
import com.school.school.service.dto.response.StudentResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class StudentSearchCacheIndexTest {

    private final StudentSearchCacheIndex cacheIndex = new StudentSearchCacheIndex();

    @Test
    void cacheKeyOf_shouldBuildEqualKeysForSamePageableAndQueryType() {
        StudentSearchCacheKey first = StudentSearchCacheKey.of(
                "teacher@example.com",
                "math",
                8,
                PageRequest.of(0, 10),
                StudentSearchQueryType.JPQL
        );
        StudentSearchCacheKey second = StudentSearchCacheKey.of(
                "teacher@example.com",
                "math",
                8,
                PageRequest.of(0, 10),
                StudentSearchQueryType.JPQL
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void putAndClear_shouldStoreAndRemovePages() {
        StudentSearchCacheKey key = StudentSearchCacheKey.of(
                null,
                null,
                null,
                PageRequest.of(1, 5),
                StudentSearchQueryType.NATIVE
        );
        StudentResponse response = new StudentResponse();
        response.setId(12L);
        Page<StudentResponse> page = new PageImpl<>(List.of(response), PageRequest.of(1, 5), 1);

        cacheIndex.put(key, page);

        assertEquals(page, cacheIndex.get(key));

        cacheIndex.clear();

        assertNull(cacheIndex.get(key));
    }

    @Test
    void cacheKeyEquals_shouldHandleDifferentTypesAndValues() {
        StudentSearchCacheKey first = StudentSearchCacheKey.of(
                "teacher@example.com",
                "math",
                8,
                PageRequest.of(0, 10),
                StudentSearchQueryType.JPQL
        );
        StudentSearchCacheKey second = StudentSearchCacheKey.of(
                "teacher@example.com",
                "math",
                9,
                PageRequest.of(0, 10),
                StudentSearchQueryType.JPQL
        );

        assertNotEquals(first, second);
        assertNotEquals(null, first);
        assertNotEquals("wrong-type", first);
    }
}
