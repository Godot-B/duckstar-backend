package com.duckstar.validation.validator;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.domain.enums.BallotType;
import com.duckstar.validation.annotation.AnimeVoteConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

import static com.duckstar.web.dto.VoteRequestDto.*;

public class AnimeVoteConstraintValidator implements ConstraintValidator<AnimeVoteConstraint, AnimeVoteRequest> {

    @Override
    public void initialize(AnimeVoteConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(AnimeVoteRequest animeVoteRequest, ConstraintValidatorContext context) {
        List<AnimeBallotDto> ballotDtos = animeVoteRequest.getBallotDtos();

        boolean isEmptyBallots = ballotDtos == null || ballotDtos.isEmpty();
        if (isEmptyBallots) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorStatus.EMPTY_BALLOTS.getCode())
                    .addPropertyNode("ballotDtos")
                    .addConstraintViolation();
            return false;
        }

        int normalCount = (int) ballotDtos.stream()
                .filter(dto -> dto.getBallotType() == BallotType.NORMAL)
                .count();

        String errorCode = null;
        if (normalCount == 0) errorCode = ErrorStatus.NORMAL_VOTE_REQUIRED.getCode();
        else if (normalCount > 10) errorCode = ErrorStatus.NORMAL_VOTE_LIMIT_SURPASSED.getCode();

        if (errorCode != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorCode)
                    .addPropertyNode("ballotDtos")
                    .addConstraintViolation();
        }

        return true;
    }
}
