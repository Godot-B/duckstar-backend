package com.duckstar.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Disabled("로컬 개발용 테스트")
@ActiveProfiles("test-db")
public class QuarterUtilTest {

    @Test
    void testQ1Anchor() {
        // 2022 Q4 시작 anchor = 9/30 19:00
        LocalDateTime anchor = QuarterUtil.resolveAnchor(LocalDateTime.of(2023,1,1,0,0))
                .anchorStart();
        assertThat(anchor).isEqualTo(LocalDateTime.of(2022,9,30,19,0));
    }

    @Test
    void testQ2Anchor() {
        // 2023 Q2 시작 anchor = 3/31 19:00
        LocalDateTime anchor = QuarterUtil.resolveAnchor(LocalDateTime.of(2023,4,1,0,0))
                .anchorStart();
        assertThat(anchor).isEqualTo(LocalDateTime.of(2023,3,31,19,0));
    }

    @Test
    void testBoundary() {
        // 3/31 18:59 → Q1 마지막 주
        var r1 = QuarterUtil.getThisWeekRecord(LocalDateTime.of(2023,3,31,18,59));
        assertThat(r1.quarterValue()).isEqualTo(1);

        // 3/31 19:00 → Q2 첫째 주
        var r2 = QuarterUtil.getThisWeekRecord(LocalDateTime.of(2023,3,31,19,0));
        assertThat(r2.quarterValue()).isEqualTo(2);
        assertThat(r2.weekValue()).isEqualTo(1);
    }

    @Test
    void testWeekProgression() {
        // Q1 anchor = 1/6 19:00 → week 1
        var r1 = QuarterUtil.getThisWeekRecord(LocalDateTime.of(2023,1,6,19,0));
        assertThat(r1.weekValue()).isEqualTo(1);

        // +7일 → week 2
        var r2 = QuarterUtil.getThisWeekRecord(LocalDateTime.of(2023,1,13,20,0));
        assertThat(r2.weekValue()).isEqualTo(2);
    }
}
