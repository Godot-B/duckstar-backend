package com.duckstar.validation.validator;

import com.duckstar.apiPayload.code.status.ErrorStatus;
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
        allowedTypes = Set.of(constraintAnnotation.anyOf());
    }

    @Override
    public boolean isValid(List<MedalPreviewDto> value, ConstraintValidatorContext context) {

        boolean isValid = value.stream()
                .filter(Objects::nonNull)
                .allMatch(dto ->
                        allowedTypes.contains(dto.getType()));

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.MEDAL_TYPE_NOT_VALID.getCode())
                    .addConstraintViolation();
        }

        return isValid;
    }
}
