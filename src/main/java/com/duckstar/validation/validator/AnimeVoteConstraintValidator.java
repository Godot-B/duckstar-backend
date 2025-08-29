package com.duckstar.validation.validator;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.domain.enums.BallotType;
import com.duckstar.domain.enums.Gender;
import com.duckstar.validation.annotation.AnimeVoteConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

import static com.duckstar.util.UtilClass.addViolationAndFalse;
import static com.duckstar.web.dto.VoteRequestDto.*;

public class AnimeVoteConstraintValidator implements ConstraintValidator<AnimeVoteConstraint, AnimeVoteRequest> {

    @Override
    public void initialize(AnimeVoteConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(AnimeVoteRequest animeVoteRequest, ConstraintValidatorContext context) {
        List<BallotRequestDto> ballotDtos = animeVoteRequest.getBallotRequests();

        boolean isEmptyBallots = ballotDtos == null || ballotDtos.isEmpty();
        if (isEmptyBallots) {
            return addViolationAndFalse(
                    context,
                    ErrorStatus.EMPTY_BALLOTS,
                    "ballotDtos"
            );
        }

        Gender gender = animeVoteRequest.getGender();
        if (gender == Gender.NONE) {
            return addViolationAndFalse(
                    context,
                    ErrorStatus.VOTER_GENDER_REQUIRED,
                    "gender"
            );
        }

        int normalCount = (int) ballotDtos.stream()
                .filter(dto -> dto.getBallotType() == BallotType.NORMAL)
                .count();

        int bonusCount = ballotDtos.size() - normalCount;

        ErrorStatus errorStatus = null;
        if (normalCount == 0) errorStatus = ErrorStatus.NORMAL_VOTE_REQUIRED;
        else if (normalCount > 10) errorStatus = ErrorStatus.NORMAL_VOTE_LIMIT_SURPASSED;
        else if (normalCount < 10 && bonusCount >= 1) errorStatus = ErrorStatus.NOT_ENOUGH_NORMAL_VOTE;

        if (errorStatus != null) {
            return addViolationAndFalse(
                    context,
                    errorStatus,
                    "ballotDtos"
            );
        }

        return true;
    }
}
