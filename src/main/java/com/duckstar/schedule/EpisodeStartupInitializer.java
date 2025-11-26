package com.duckstar.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class EpisodeStartupInitializer {

    private final NamedParameterJdbcTemplate jdbc;

    @EventListener(ApplicationReadyEvent.class)
    public void updateEvaluateStateOnce() {
        LocalDateTime now = LocalDateTime.now();

        String sql = """
                UPDATE episode
                SET evaluate_state = CASE
                     WHEN is_break = TRUE THEN NULL
                     WHEN scheduled_at > NOW() THEN 'CLOSED'
                     WHEN scheduled_at <= NOW()
                          AND DATE_ADD(scheduled_at, INTERVAL 36 HOUR) > NOW()
                         THEN 'VOTING_WINDOW'
                     WHEN DATE_ADD(scheduled_at, INTERVAL 36 HOUR) <= NOW()
                          AND scheduled_at >= '2025-11-17 18:00:00'
                         THEN 'LOGIN_REQUIRED'
                     WHEN DATE_ADD(scheduled_at, INTERVAL 36 HOUR) <= NOW()
                          AND scheduled_at < '2025-11-17 18:00:00'
                         THEN 'ALWAYS_OPEN'
                END;
                """;

        jdbc.update(sql, new HashMap<>());
    }
}