package com.duckstar.domain.enums;

/**
 * 관리자 로그에 기록되는 작업 종류
 */
public enum AdminTaskType {
    BAN,
    UNBAN,
    WITHDRAW,
    UNDO_WITHDRAW,

    EPISODE_BREAK,
    EPISODE_RESCHEDULE,
    EPISODE_CREATE,
    FUTURE_EPISODE_DELETE,
    EPISODE_MODIFY_NUMBER,

    // 애니메이션 정보 관리
    ANIME_CREATE,
    ANIME_INFO_UPDATE,
    ANIME_STATUS_UPDATE,
    ANIME_DIRECTION_UPDATE,
    ANIME_EPISODE_TOTAL_COUNT
}
