package com.duckstar.apiPayload.code.status;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // 분기 관련
    QUARTER_NOT_FOUND(HttpStatus.BAD_REQUEST, "QUARTER4001", "존재하지 않는 분기입니다."),

    // 주 관련
    WEEK_NOT_FOUND(HttpStatus.BAD_REQUEST, "WEEK4001", "존재하지 않는 주입니다."),

    // 애니메이션
    ANIME_NOT_FOUND(HttpStatus.BAD_REQUEST, "ANIME4001", "존재하지 않는 애니메이션입니다."),

    // 투표
    VOTE_CLOSED(HttpStatus.BAD_REQUEST, "VOTE4001", "현재 닫힌 투표입니다."),
    ANIME_CANDIDATE_NOT_FOUND(HttpStatus.BAD_REQUEST, "VOTE4002", "존재하지 않는 애니메이션 후보입니다."),
    ALREADY_VOTED(HttpStatus.BAD_REQUEST, "VOTE4003", "이미 참여한 투표입니다."),
    EMPTY_BALLOTS(HttpStatus.BAD_REQUEST, "VOTE4004", "투표지가 비어있습니다."),
    DUPLICATE_CANDIDATE_INCLUDED(HttpStatus.BAD_REQUEST, "VOTE4005", "중복된 후보가 요청에 포함되어있습니다."),
    INVALID_CANDIDATE_INCLUDED(HttpStatus.BAD_REQUEST, "VOTE4006", "이번 주 후보가 아닌 대상이 포함되어 있습니다."),
    VOTE_LIMIT_SURPASSED(HttpStatus.BAD_REQUEST, "VOTE4007", "기본 투표 제한 수를 넘어섰습니다."),
    NOT_VOTED_YET(HttpStatus.BAD_REQUEST, "VOTE4008", "아직 투표에 참여하지 않았습니다."),

    VOTE_AUTH_REQUIRED(HttpStatus.CONFLICT, "VOTE4090", "투표 인증 정보가 없습니다."),

    // 인증 관련
    UNSUPPORTED_OAUTH_TYPE(HttpStatus.BAD_REQUEST, "AUTH4001", "지원하지 않는 소셜 로그인 타입입니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
