package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.repository.Week.WeekRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeekService {

    private final WeekRepository weekRepository;

    public Long getWeekIdByYQW(Integer year, Integer quarter, Integer week) {
        return weekRepository.findWeekIdByYQW(year, quarter, week)
                .orElseThrow(() -> new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));
    }
}
