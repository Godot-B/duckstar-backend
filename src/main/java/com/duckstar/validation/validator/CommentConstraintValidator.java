package com.duckstar.validation.validator;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.validation.annotation.CommentConstraint;
import com.duckstar.web.dto.WriteRequestDto.CommentRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CommentConstraintValidator implements ConstraintValidator<CommentConstraint, CommentRequestDto> {

    @Override
    public void initialize(CommentConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(CommentRequestDto commentRequestDto, ConstraintValidatorContext context) {
        String image = commentRequestDto.getAttachedImageUrl();
        String body = commentRequestDto.getBody();
        boolean hasImage = image != null && !image.isBlank();
        boolean hasBody = body != null && !body.isBlank();

        if (!hasBody && !hasImage) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.COMMENT_CONTENT_REQUIRED.getCode())
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
