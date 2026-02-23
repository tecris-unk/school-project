package com.school.school.service.mapper;

import com.school.school.model.SchoolClass;
import com.school.school.service.dto.SchoolClassDto;
import org.springframework.stereotype.Component;

@Component
public final class SchoolClassMapper {

    public SchoolClassDto toDto(final SchoolClass schoolClass) {
        if (schoolClass == null) {
            return null;
        }

        return new SchoolClassDto(
                schoolClass.getId(),
                schoolClass.getGrade(),
                schoolClass.getLetter()
        );
    }

    public SchoolClass toEntity(final SchoolClassDto dto) {
        if (dto == null) {
            return null;
        }

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setGrade(dto.getGrade());
        schoolClass.setLetter(dto.getLetter());
        return schoolClass;
    }

    public void updateEntity(
            final SchoolClass schoolClass,
            final SchoolClassDto dto) {
        schoolClass.setGrade(dto.getGrade());
        schoolClass.setLetter(dto.getLetter());
    }
}
