# ШКОЛА 

### REST API проект на Java, фреймворк Spring, Maven. 
 
 	
1. Реализовать сложный GET-запрос с фильтрацией по вложенной сущности с использованием @Query (JPQL).
2. Реализовать аналогичный запрос через native query.
3. Добавить пагинацию (Pageable).
4. Реализовать in-memory индекс на основе HashMap<K, V> для ранее запрошенных данных. Ключ должен формироваться из параметров запроса (составной ключ). Обеспечить корректную работу индекса за счёт правильной реализации equals() и hashCode().
5. Реализовать инвалидацию индекса при изменении данных.

1. Реализовать глобальную обработку ошибок через @ControllerAdvice.
2. Добавить валидацию входных данных через @Valid.
3. Реализовать единый формат ошибки для всех endpoint.
4. Настроить логирование через logback:
- уровни логирования
- ротация логов
5. Реализовать аспект (AOP) для логирования времени выполнения сервисных методов.
6. Подключить Swagger/OpenAPI с описанием endpoint и DTO.

[Сонар](https://sonarcloud.io/project/overview?id=tecris-unk_school-project)


```mermaid
erDiagram
    SCHOOL_CLASSES ||--o{ STUDENTS : has
    TEACHERS o|--o{ SUBJECTS : teaches
    STUDENTS ||--o{ GRADES : receives
    SUBJECTS ||--o{ GRADES : includes
    SCHOOL_CLASSES ||--o{ SCHOOL_CLASSES_SUBJECTS : contains
    SUBJECTS ||--o{ SCHOOL_CLASSES_SUBJECTS : assigned_to

    SCHOOL_CLASSES {
        BIGINT id PK
        INT grade
        VARCHAR letter
    }

    STUDENTS {
        BIGINT id PK
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR gender
        VARCHAR email UK
        BIGINT school_class_id FK
    }

    SCHOOL_CLASSES_SUBJECTS {
        BIGINT school_class_id PK, FK
        BIGINT subject_id PK, FK
    }

    TEACHERS {
        BIGINT id PK
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR email UK
    }

    SUBJECTS {
        BIGINT id PK
        VARCHAR name
        VARCHAR description
        BIGINT teacher_id FK
    }

    GRADES {
        BIGINT id PK
        BIGINT student_id FK
        BIGINT subject_id FK
        INT score
        DATE date
    }

    
```
