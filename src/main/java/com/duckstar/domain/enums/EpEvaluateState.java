package com.duckstar.domain.enums;

/**
 * 에피소드 평가 상태
 */
public enum EpEvaluateState {

    // 휴방인 경우
    // NULL

    // 투표 전
    CLOSED,

    // 투표
    VOTING_WINDOW,  // 투표 가능 시간 (현 36시간 정책)
    LOGIN_REQUIRED,  // 로그인 유저만, 5자 이상 후기 필수

    // 평가
    ALWAYS_OPEN,
}
