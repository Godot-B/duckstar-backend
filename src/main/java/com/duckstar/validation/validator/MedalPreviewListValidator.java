package com.duckstar.validation.validator;

import com.duckstar.domain.enums.MedalType;
import com.duckstar.validation.annotation.MedalTypeSubset;
import com.duckstar.web.dto.MedalDto.MedalPreviewDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MedalPreviewListValidator implements ConstraintValidator<MedalTypeSubset, List<MedalPreviewDto>> {

    private Set<MedalType> allowedTypes;

    @Override
    public void initialize(MedalTypeSubset constraintAnnotation) {
        this.allowedTypes = Set.of(constraintAnnotation.anyOf());
    }

    @Override
    public boolean isValid(List<MedalPreviewDto> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return value.stream()
                .filter(Objects::nonNull)
                .allMatch(dto ->
                        allowedTypes.contains(dto.getType()));
    }
}
