package com.duckstar.util;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import jakarta.validation.ConstraintValidatorContext;

public class UtilClass {

    public static boolean addViolationAndFalse(
            ConstraintValidatorContext context,
            ErrorStatus errorStatus
    ) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(errorStatus.getCode())
                .addConstraintViolation();
        return false;
    }

    public static boolean addViolationAndFalse(
            ConstraintValidatorContext context,
            ErrorStatus errorStatus,
            String fieldName
    ) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(errorStatus.getCode())
                .addPropertyNode(fieldName)
                .addConstraintViolation();
        return false;
    }


}
