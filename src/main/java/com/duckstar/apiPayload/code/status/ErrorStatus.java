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

    // 순위 정보, 메달
    MEDAL_TYPE_NOT_VALID(HttpStatus.BAD_REQUEST, "MEDAL4001", "허용되지 않는 메달 타입입니다."),

    // 애니메이션
    ANIME_NOT_FOUND(HttpStatus.BAD_REQUEST, "ANIME4001", "존재하지 않는 애니메이션입니다."),

    // 에피소드
    EPISODE_NOT_FOUND(HttpStatus.BAD_REQUEST, "EPISODE4001", "존재하지 않는 에피소드입니다."),

    // 투표
    VOTE_CLOSED(HttpStatus.BAD_REQUEST, "VOTE4001", "현재 닫힌 투표입니다."),
    ANIME_CANDIDATE_NOT_FOUND(HttpStatus.BAD_REQUEST, "VOTE4002", "존재하지 않는 애니메이션 후보입니다."),
    ALREADY_VOTED(HttpStatus.BAD_REQUEST, "VOTE4003", "이미 참여한 투표입니다."),
    EMPTY_BALLOTS(HttpStatus.BAD_REQUEST, "VOTE4004", "투표지가 비어있습니다."),
    DUPLICATE_CANDIDATE_INCLUDED(HttpStatus.BAD_REQUEST, "VOTE4005", "중복된 후보가 요청에 포함되어 있습니다."),
    INVALID_CANDIDATE_INCLUDED(HttpStatus.BAD_REQUEST, "VOTE4006", "이번 주 후보가 아닌 대상이 포함되어 있습니다."),
    NORMAL_VOTE_LIMIT_SURPASSED(HttpStatus.BAD_REQUEST, "VOTE4007", "기본 투표 제한 수를 넘어섰습니다."),
    NORMAL_VOTE_REQUIRED(HttpStatus.BAD_REQUEST, "VOTE4008", "기본 투표는 1표 이상이어야 합니다."),
    NOT_ENOUGH_NORMAL_VOTE(HttpStatus.BAD_REQUEST, "VOTE4009", "보너스 투표 사용을 위해선 기본 투표가 10개 있어야 합니다."),
    NOT_VOTED_YET(HttpStatus.BAD_REQUEST, "VOTE4010", "아직 투표에 참여하지 않았습니다."),
    VOTER_GENDER_REQUIRED(HttpStatus.BAD_REQUEST, "VOTE40011", "투표에서 성별 정보는 필수입니다."),

    VOTE_HISTORY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "VOTE4031", "다른 사람의 투표 내역은 확인할 수 없습니다."),

    VOTE_AUTH_REQUIRED(HttpStatus.CONFLICT, "VOTE4091", "투표 인증 정보가 존재하지 않습니다."),

    // 인증 관련
    UNSUPPORTED_OAUTH_TYPE(HttpStatus.BAD_REQUEST, "AUTH4001", "지원하지 않는 소셜 로그인 제공자입니다."),
    MEMBER_TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH4002", "회원의 JWT 토큰 데이터가 존재하지 않습니다."),
    REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST, "AUTH4003", "리프레시 토큰이 요청에 포함되어 있지 않습니다.."),
    OAUTH_ACCOUNT_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH4004", "소셜 로그인 계정이 존재하지 않습니다."),

    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "AUTH4011", "이미 사용된 리프레시 토큰입니다. 다시 로그인해 주세요."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4012", "리프레시 토큰이 만료되었습니다. 다시 로그인해 주세요."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4013", "유효하지 않은 토큰입니다."),

    // 회원 관련
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "존재하지 않는 회원입니다."),
    PRINCIPAL_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4002", "로그인 중인 id로 회원을 찾을 수 없습니다."),

    // 댓글 관련
    COMMENT_CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "COMMENT4001", "댓글 작성 시 사진이나 글 중 하나는 있어야 합니다."),
    COMMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "COMMENT4002", "댓글이 존재하지 않습니다."),
    CANNOT_POST_BEFORE_EPISODE_START(HttpStatus.BAD_REQUEST, "COMMENT4003", "아직 방영하지 않은 에피소드에는 댓글을 달 수 없습니다."),

    POST_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMENT4011", "댓글/답글 작성 권한이 없습니다."),
    DELETE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMENT4012", "댓글/답글 삭제 권한이 없습니다."),

    // 답글 관련
    REPLY_NOT_FOUND(HttpStatus.BAD_REQUEST, "REPLY4001", "답글이 존재하지 않습니다."),

    // 이미지 관련
    INVALID_IMAGE_FILE(HttpStatus.BAD_REQUEST, "IMAGE4001", "유효하지 않은 이미지 파일입니다."),
    INVALID_S3_IMAGE_URL(HttpStatus.BAD_REQUEST, "IMAGE4002", "유효하지 않은 S3 URL 형식입니다."),

    UNSUPPORTED_IMAGE_EXTENSION(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "IMAGE4151", "지원하지 않는 이미지 확장자입니다."),

    S3_FILE_UPLOAD_FAILURE(HttpStatus.BAD_GATEWAY, "IMAGE5021", "S3 업로드에 실패했습니다"),
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
