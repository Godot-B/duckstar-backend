package com.duckstar.validation.annotation;

import com.duckstar.domain.enums.MedalType;
import com.duckstar.validation.validator.MedalPreviewListValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MedalPreviewListValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MedalTypeSubset {
    MedalType[] anyOf();
    String message() default "허용되지 않는 메달 타입입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
