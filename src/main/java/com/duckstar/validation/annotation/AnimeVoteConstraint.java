package com.duckstar.validation.annotation;

import com.duckstar.validation.validator.AnimeVoteConstraintValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AnimeVoteConstraintValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AnimeVoteConstraint {
    String message() default "일반 투표는 1표 이상, 10표 이하여야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
