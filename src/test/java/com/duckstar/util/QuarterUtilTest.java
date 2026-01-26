package com.duckstar.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.duckstar.util.QuarterUtil.*;
import static org.assertj.core.api.Assertions.*;

public class QuarterUtilTest {
    ///  한 주는 월요일 ~ 일요일 이지만
    ///  실제로는 [월요일 18시, 월요일 18시) 이며 [포함, 배제) 이다.
    ///
    ///  ex1) [23-09-25 (월 18시), 23-10-02 (월 18시)) 은 23년 4분기 1주차 이며 (다음 분기 하루 포함),
    ///       [23-10-02 (월 18시), 23-10-09 (월 18시)) 은 23년 4분기 2주차 이다.
    ///
    ///  ex2) [23-12-25 (월 18시), 24-01-01 (월 18시)) 은 23년 4분기 14주차 이며 (다음 분기 하루도 포함하지 않음),
    ///       [24-01-01 (월 18시), 24-01-08 (월 18시)) 은 24년 1분기 1주차 이다.
    @Test
    public void 주_시작일_경계_테스트() {
        LocalDateTime monday18H = LocalDateTime
                .of(2023, 12, 25, 18, 0);

        // 월요일 18시가 주 시작일이 맞는지 테스트
        LocalDateTime thisWeekStartedAt = getThisWeekStartedAt(monday18H);
        assertThat(thisWeekStartedAt).isEqualTo(monday18H);

        // 1분이라도 앞선 시간은 이전 주 시작일
        LocalDateTime lastWeekStartedAt = getThisWeekStartedAt(monday18H.minusMinutes(1));
        assertThat(lastWeekStartedAt).isEqualTo(monday18H.minusWeeks(1));
    }

    @Test
    public void 레코드_경계_테스트() {
        // 23-12-25 (월) 17:59
        //  -> 23년 4분기 13주차
        YQWRecord r1 = getThisWeekRecord(LocalDateTime.of(2023,12,25,17,59));
        assertThat(r1.yearValue()).isEqualTo(2023);
        assertThat(r1.quarterValue()).isEqualTo(4);
        assertThat(r1.weekValue()).isEqualTo(13);

        // 23-12-25 (월) 18:00 ~ 24-01-01 (월) 17:59
        //  -> 같은 23년 4분기 14주차
        YQWRecord r2 = getThisWeekRecord(LocalDateTime.of(2023,12,25,18,0));
        YQWRecord r3 = getThisWeekRecord(LocalDateTime.of(2024,1,1,17,59));
        assertThat(r3.yearValue()).isEqualTo(r2.yearValue());
        assertThat(r3.quarterValue()).isEqualTo(r2.quarterValue());
        assertThat(r3.weekValue()).isEqualTo(r2.weekValue());
        assertThat(r2.yearValue()).isEqualTo(2023);
        assertThat(r2.quarterValue()).isEqualTo(4);
        assertThat(r2.weekValue()).isEqualTo(14);

        // 24-01-01 (월) 18:00
        //  -> 24년 1분기 1주차
        YQWRecord r4 = getThisWeekRecord(LocalDateTime.of(2024,1,1,18,0));
        assertThat(r4.yearValue()).isEqualTo(2024);
        assertThat(r4.quarterValue()).isEqualTo(1);
        assertThat(r4.weekValue()).isEqualTo(1);
    }
}
