package com.duckstar.web.dto;

import com.duckstar.validation.annotation.CommentConstraint;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@CommentConstraint
public class CommentRequestDto {
    String attachedImageUrl;

    @Size(max = 1000)
    String body;
}
