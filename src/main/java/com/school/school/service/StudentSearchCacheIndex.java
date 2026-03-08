package com.school.school.service;

import com.school.school.service.dto.response.StudentResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class StudentSearchCacheIndex {

    private final Map<StudentSearchCacheKey, Page<StudentResponse>> cache = new HashMap<>();

    public synchronized Page<StudentResponse> get(final StudentSearchCacheKey key) {
        return cache.get(key);
    }

    public synchronized void put(final StudentSearchCacheKey key, final Page<StudentResponse> value) {
        cache.put(key, value);
    }

    public synchronized void clear() {
        cache.clear();
    }

    @AllArgsConstructor
    public static final class StudentSearchCacheKey {

        private final String teacherEmail;
        private final String subjectName;
        private final Integer minScore;
        private final int pageNumber;
        private final int pageSize;
        private final String sort;
        private final StudentSearchQueryType queryType;

        public static StudentSearchCacheKey of(
                final String teacherEmail,
                final String subjectName,
                final Integer minScore,
                final Pageable pageable,
                final StudentSearchQueryType queryType
        ) {
            return new StudentSearchCacheKey(
                    teacherEmail,
                    subjectName,
                    minScore,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    pageable.getSort().toString(),
                    queryType
            );
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            StudentSearchCacheKey that = (StudentSearchCacheKey) o;
            return pageNumber == that.pageNumber
                    && pageSize == that.pageSize
                    && Objects.equals(teacherEmail, that.teacherEmail)
                    && Objects.equals(subjectName, that.subjectName)
                    && Objects.equals(minScore, that.minScore)
                    && Objects.equals(sort, that.sort)
                    && queryType == that.queryType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(teacherEmail, subjectName, minScore, pageNumber, pageSize, sort, queryType);
        }
    }
}
