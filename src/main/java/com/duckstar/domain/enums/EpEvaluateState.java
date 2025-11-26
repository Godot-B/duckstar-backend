package com.duckstar.domain.enums;

/**
 * 에피소드 평가 상태
 */
public enum EpEvaluateState {

    // 휴방인 경우
    // NULL

    /**
     * 투표 전
     */
    CLOSED,

    /**
     * 투표 진행 중
     */
    // 실시간 투표
    // 비로그인 투표 허용, 후기 필수 아님
    VOTING_WINDOW,
    // 시간 외 투표
    // 로그인 유저만, 5자 이상 후기 필수
    LOGIN_REQUIRED,  // 주차 마감을 기다리는 상태

    /**
     * 투표 및 주차 마감
     */
    ALWAYS_OPEN,  // 로그인 유저만, 후기 필수 아님
}
